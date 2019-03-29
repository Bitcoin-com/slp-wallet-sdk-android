package com.bitcoin.wallet

import com.bitcoin.wallet.address.AddressSLP
import com.bitcoin.wallet.persistence.WalletDatabase
import com.bitcoin.wallet.rest.BitcoinRestClient
import com.bitcoin.wallet.slp.*
import com.bitcoin.wallet.tx.TxBuilder
import com.bitcoin.wallet.tx.Utxo
import com.bitcoin.wallet.tx.UtxoFacade
import com.google.gson.JsonParser
import io.reactivex.Single
import timber.log.Timber
import java.math.BigDecimal

/**
 * @author akibabu
 */
internal class WalletServiceImpl(private val wallet: SLPWalletImpl, private val bitcoinClient: BitcoinRestClient,
                                 database: WalletDatabase) : WalletService {

    constructor(wallet: SLPWalletImpl, database: WalletDatabase) : this(wallet,
        BitcoinRestClient.getInstance(wallet.network), database)

    private val txBuilder = TxBuilder(wallet)

    private val tokenDetailsFacade = SlpTokenDetailsFacade(database.slpTokenDetailsDao(), bitcoinClient)
    private val utxoFacade = UtxoFacade(wallet, bitcoinClient, database.utxoDao(), database.slpUtxoDao(),
        database.slpValidTxDao())

    override fun refreshBalance(): Single<WalletBalance> {
        return Single.fromCallable { // Wrap for now to protect against blocking non reactive calls
            val utxos = utxoFacade.getUtxos()
            val slpGenesisTxIds = utxos.slpUtxos
                .map { it.tokenId }
                .toSet()

            val tokenDetails = tokenDetailsFacade.getTokenDetails(slpGenesisTxIds).blockingGet()
                .groupBy { it.tokenId }
                .mapValues { it.value[0] }

            val nativeBalance = utxos.bchUtxos.map { it.satoshi }.sum()
            val slpBalance = utxos.slpUtxos
                .groupBy { it.tokenId }
                .mapKeys { tokenDetails.getValue(it.key) }
                .mapValues {
                    val balance = it.value.map { it.numTokensRaw }.reduce(BigDecimal::add)
                    it.key.toReadableAmount(balance)
                }
            WalletBalance(nativeBalance, slpBalance)
        }
    }

    internal fun sendTokenUtxoSelection(tokenId: SlpTokenId, numTokens: BigDecimal): Single<SendTokenUtxoSelection> {
        return Single.fromCallable { // Wrap for now to protect against blocking non reactive calls
            val tokenDetailsList: List<SlpTokenDetails> =  tokenDetailsFacade.getTokenDetails(setOf(tokenId))
                .blockingGet()
            val tokenDetails: SlpTokenDetails = tokenDetailsList[0]
            val sendTokensRaw =  tokenDetails.toRawAmount(numTokens)
            var sendSatoshi = TxBuilder.DUST_LIMIT // At least one dust limit output to the token receiver

            val utxos = utxoFacade.getUtxos()

            // First select enough token utxo's and just take what we get in terms of BCH
            var inputTokensRaw = ULong.MIN_VALUE
            var inputSatoshi = 0L
            val selectedUtxos = utxos.slpUtxos
                .filter { it.tokenId == tokenId }
                .sortedBy { it.numTokensRaw }
                .takeWhile {
                    val amountTooLow = inputTokensRaw < sendTokensRaw
                    if (amountTooLow) {
                        inputTokensRaw += it.numTokensRaw.toLong().toULong()
                        inputSatoshi += (it.utxo.satoshi - 148) // Deduct input fee
                    }
                    amountTooLow
                }
                .map { it.utxo }
                .toMutableList()
            if (inputTokensRaw < sendTokensRaw) {
                throw RuntimeException("insufficient token balance=$inputTokensRaw")
            } else if (inputTokensRaw > sendTokensRaw) {
                // If there's token change we need at least another dust limit worth of BCH
                sendSatoshi += TxBuilder.DUST_LIMIT
            }

            val propagationExtraFee = 50 // When too close 1sat/byte tx's don't propagate well
            val numOutputs = 3 // Assume three outputs in addition to the op return.
            val numQuanitites = 2 // Assume one token receiver and the token receiver
            val fee = TxBuilder.outputFee(numOutputs) + SlpOpReturn.sizeInBytes(numQuanitites) + propagationExtraFee

            // If we can not yet afford the fee + dust limit to send, use pure BCH utxo's
            selectedUtxos.addAll(utxos.bchUtxos
                .sortedBy { it.satoshi }
                .takeWhile {
                    val amountTooLow = inputSatoshi <= (sendSatoshi + fee)
                    if (amountTooLow) {
                        inputSatoshi += (it.satoshi - 148) // Deduct input fee
                    }
                    amountTooLow
                })

            val changeSatoshi = inputSatoshi - sendSatoshi - fee
            if (changeSatoshi < 0) {
                throw IllegalArgumentException("Insufficient BCH balance=$inputSatoshi required $sendSatoshi + fees")
            }

            // We have enough tokens and BCH. Create the transaction
            val quantities = mutableListOf(sendTokensRaw)
            val changeTokens = inputTokensRaw - sendTokensRaw
            if (changeTokens > 0u) {
                quantities.add(changeTokens)
            }

            SendTokenUtxoSelection(tokenId, quantities, changeSatoshi, selectedUtxos)
        }
    }

    internal data class SendTokenUtxoSelection(
        val tokenId: SlpTokenId, val quantities: List<ULong>, val changeSatoshi: Long,
        val selectedUtxos: List<Utxo>
    )

    override fun sendTokenRx(tokenId: SlpTokenId, amount: BigDecimal, toAddress: String): Single<String> {
        return sendTokenUtxoSelection(tokenId, amount)
            .map {
                val toAddress = AddressSLP.parse(wallet.network, toAddress) // Validate as SLP address

                // Add OP RETURN and receiver output
                val tx = txBuilder.Builder()
                tx
                    .addOutput(SlpOpReturn.send(it.tokenId, it.quantities))
                    .addOutput(TxBuilder.DUST_LIMIT, toAddress)

                // Send our token change back to our SLP address
                if (it.quantities.size == 2) {
                    tx.addOutput(TxBuilder.DUST_LIMIT, wallet.slpKeyAddress.address)
                }

                // Send our BCH change back to our BCH address
                if (it.changeSatoshi >= TxBuilder.DUST_LIMIT) {
                    tx.addOutput(it.changeSatoshi, wallet.bchAddressKey.address)
                }

                it.selectedUtxos.forEach { tx.addInput(it) }

                val hex = tx.serialize()
                Timber.d("Broadcasting serialized tx $hex")
                hex
            }
            .flatMap {
                sendTx(it)
                    .doOnError { Timber.e(it) }
                    .doOnSuccess { Timber.d("Broadcasted txid=$it") }
            }
    }

    private val jsonParser = JsonParser()

    /**
     * Returns the txid as a string if present, other responses throws an exception
     */
    private fun sendTx(hex: String): Single<String> {
        return bitcoinClient.sendRawTransaction(hex)
            .map {
                val json = jsonParser.parse(it)
                if (json.isJsonPrimitive && json.asString.length == 64) {
                    return@map json.asString
                }
                throw RuntimeException(json.toString())
            }
    }

}