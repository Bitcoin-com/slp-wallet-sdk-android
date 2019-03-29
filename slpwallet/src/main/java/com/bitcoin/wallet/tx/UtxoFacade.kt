package com.bitcoin.wallet.tx

import com.bitcoin.wallet.rest.*
import com.bitcoin.wallet.slp.*
import org.bitcoinj.core.Coin
import timber.log.Timber
import java.math.BigDecimal

/**
 * @author akibabu
 */
internal class UtxoFacade(private val wallet: SLPWalletImpl, private val bitcoinClient: BitcoinRestClient,
                          private val utxoDao: UtxoDao, private val slpUtxoDao: SlpUtxoDao,
                          private val validTxDao: SlpValidTxDao
) {

    private val addressUtxosRequest = AddressUtxosRequest(listOf(
        wallet.bchAddressKey.address.toCash(),
        wallet.slpKeyAddress.address.toCash()
    ))

    internal data class Utxos(val bchUtxos: List<Utxo>, val slpUtxos: List<SlpUtxo>)

    private fun getUtxosOfWalletAddresses(): List<AddressUtxosResponse.UtxoResponse> {
        return bitcoinClient.getUtxos(addressUtxosRequest)
            .retry(3)
            .blockingGet()
            // Put address on each UTXO so we can easier distinguish between BCH and SLP
            .flatMap { it.flatMapEnrichWithAddress() }
    }

    private fun getUnseenTxDetails(txIds: Set<String>): List<TxResponse> {
        val seenTxIds = utxoDao.findAllTxIds().plus(slpUtxoDao.findAllTxIds()).toSet()
        return txIds
            .filter { !seenTxIds.contains(it) }
            .chunked(20)
            .map { TxDetailsRequest(it) }
            .flatMap {
                bitcoinClient.getTransactions(it)
                    .retry(3)
                    .blockingGet()
            }
    }

    @Synchronized
    fun getUtxos(): Utxos {
        // Get all UTXO's for our 2 addresses
        val utxosRaw: List<AddressUtxosResponse.UtxoResponse> = getUtxosOfWalletAddresses()

        val currentUtxos = utxoDao.findAll()
        val currentSlpUtxos = slpUtxoDao.findAll()

        // Later we are forced by the API to fetch the entire TX, so we need to know what UTXO's in each TX are ours
        val voutsInTx = utxosRaw
            .groupBy { it.txid }
            .mapValues {
                it.value
                    .map { it.vout }
                    .toSet()
            }

        currentUtxos.forEach {
            if (!(voutsInTx[it.txId] ?: emptySet()).contains(it.index)) {
                utxoDao.delete(it.txId, it.index)
            }
        }

        currentSlpUtxos.forEach {
            if (!(voutsInTx[it.utxo.txId] ?: emptySet()).contains(it.utxo.index)) {
                slpUtxoDao.delete(it.utxo.txId, it.utxo.index)
            }
        }

        /*
         * TX details doesn't come with a reliable output address so we need a lookup from (txid, output index) -> address
         * Output index is included to account for the theoretical possibility of having both our addresses present in the same TX
         */
        val txIdToVoutToCashAddressLookup = utxosRaw
            .groupBy { it.txid }
            .mapValues {
                it.value
                    .groupBy { it.vout }
                    .mapValues {
                        if (it.value.size != 1) {
                            throw IllegalStateException("Expecting exactly one UTXO per (txid,vout) key")
                        }
                        it.value[0].cashAddress!!
                    }
            }

        // Ignore all tx's we already stored the utxo's for
        val txs = getUnseenTxDetails(voutsInTx.keys)

        val utxos = txs.flatMap { tx ->
            tx.vout
                .filter {
                    // Keep our utxo's plus any valid SLP op returns
                    val isOurUtxo = voutsInTx[tx.txid]?.contains(it.n) ?: false
                    val isSlpOpReturn = SlpOpReturn.tryParse(tx.txid, it.scriptPubKey.hex) is SlpOpReturnSend
                    isOurUtxo || isSlpOpReturn
                }
                .map { output ->
                    val address = txIdToVoutToCashAddressLookup.getValue(tx.txid).getOrElse(output.n) {
                        // When we reach an SLP op return we don't have an address, so we assume the SLP address
                        // TODO a full validation could happen here, i.e ensure output 1 is sent to the SLP address...
                        if (output.n != 0) {
                            throw IllegalStateException("txid=${tx.txid} output=${output.n} without known address")
                        }
                        wallet.slpAddressAsCash
                    }
                    Utxo(tx.txid, output.n, address, output.scriptPubKey.hex, Coin.parseCoin(output.value).value)
                }
        }

        val parsedUtxos = parseUtxos(utxos)
        utxoDao.save(parsedUtxos.bchUtxos)
        slpUtxoDao.save(parsedUtxos.slpUtxos)
        return parsedUtxos
    }

    private fun parseUtxos(utxos: List<Utxo>): Utxos {
        val nativeUtxos = utxoDao.findAll().toMutableList()
        val tokenUtxos = slpUtxoDao.findAll().toMutableList()

        val utxoEntries = utxos
            .groupBy { it.txId }
            .mapValues { it.value
                .groupBy { it.index }
                .mapValues { it.value[0] }
            }

        utxoEntries.forEach {
            val slpScript = it.value[0]?.scriptHex?.let { script -> SlpOpReturn.tryParse(it.key, script) }

            if (slpScript == null) {
                // Because the store only holds our utxo's and SLP scripts, these are all our utxo's
                nativeUtxos.addAll(it.value.values)
            } else {
                if (slpScript.transactionType == SlpTransactionType.SEND) {

                    // Add all SLP send quantities that are sent to us (likely 1 but in theory several or 0)
                    val sendOutput: SlpOpReturnSend = slpScript as SlpOpReturnSend
                    val tokenUtxosInTx = sendOutput.quantities
                        // TODO ignoring non existing utxos which is a protocol breach
                        .mapIndexedNotNull { index, numTokensRaw ->
                            // +1 skip OP_RETURN
                            it.value[index + 1]?.let { utxo ->
                                SlpUtxo(slpScript.tokenId, BigDecimal(numTokensRaw.toString()), utxo)
                            }
                        }
                    tokenUtxos.addAll(tokenUtxosInTx)

                    // Add utxo's coming after the token quantity utxos
                    it.value.values
                        .filter { utxo: Utxo ->
                            val isMyAddress = wallet.isMyCashAddress(utxo.cashAddress)
                            val exceedsSlpUtxosSpecified: Boolean = utxo.index > sendOutput.quantities.size
                            isMyAddress && exceedsSlpUtxosSpecified
                        }
                        .forEach { nativeUtxo: Utxo ->
                            nativeUtxos.add(nativeUtxo)
                        }
                } else if (slpScript is SlpOpReturn.BatonAndMint) { // Handle GENESIS and MINT the same way
                    val batonAndMint = slpScript as SlpOpReturn.BatonAndMint
                    // We are the receiver of the minted value which is always vout[1]. Add it as a token utxo
                    it.value[1]?.let { utxo ->
                        SlpUtxo(slpScript.tokenId, BigDecimal(batonAndMint.mintedAmount.toString()), utxo)
                    }?.let { tokenUtxos.add(it) }

                    // Add any potential non SLP utxos, most importantly excluding the baton if it was not destroyed
                    it.value
                        .filterKeys { it > 1 && it.toUInt() != batonAndMint.batonVout }
                        .forEach { nativeUtxos.add(it.value) }
                }
            }
        }
        return Utxos(nativeUtxos, filterValidSlpUtxos(tokenUtxos))
    }

    private fun filterValidSlpUtxos(utxos: List<SlpUtxo>): List<SlpUtxo> {
        val txIds = utxos.map { it.utxo.txId }
            .toSet()

        val txIdsValidity = validTxDao.findByIds(txIds)
            .groupBy { it.txId }
            .mapValues { it.value[0] } // txId is primary key
            .toMutableMap()

        val newTxIdsValidity = txIds
            .filter { !txIdsValidity.contains(it) }
            .chunked(20)
            .flatMap {
                bitcoinClient.validateSlpTxs(SlpValidateTxRequest(it))
                    .retry(3)
                    .blockingGet()
            }
            .groupBy { it.txid }
            .mapValues { SlpValidTx(it.key, it.value[0].valid) }

        validTxDao.save(newTxIdsValidity.values)

        txIdsValidity.putAll(newTxIdsValidity)
        return utxos
            .filter {
                if (txIdsValidity[it.utxo.txId]?.valid == true) {
                    return@filter true
                } else {
                    // Ignore invalid tx's, using them as BCH balance is too uncertain
                    Timber.w("Invalid SLP txid=${it.utxo.txId}")
                    return@filter  false
                }
            }
    }

}