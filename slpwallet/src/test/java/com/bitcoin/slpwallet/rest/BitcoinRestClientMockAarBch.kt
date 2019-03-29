package com.bitcoin.slpwallet.rest

import com.bitcoin.slpwallet.Network
import com.bitcoin.slpwallet.WalletDatabaseInMemory
import com.bitcoin.slpwallet.bitcoinj.Mnemonic
import com.bitcoin.slpwallet.slp.SLPWalletImpl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single

/**
 * Snapshot of and address with expected balances
 *
 * Has a low amount of Satoshis in SLP utxos
 *
 * @author akibabu
 */
internal class BitcoinRestClientMockAarBch : BitcoinRestClient {

    val mnemonic = "fix ranch escape toe brave return amazing average torch faculty vote guard".split(" ")

    val wallet = SLPWalletImpl(Network.MAIN, Mnemonic(mnemonic), WalletDatabaseInMemory())
    private val gson = Gson()

    override fun getUtxos(request: AddressUtxosRequest): Single<List<AddressUtxosResponse>> {
        val json = resource("rest_utxos_aar_bch.json")
        return Single.just(gson.fromJson<List<AddressUtxosResponse>>(json, object: TypeToken<List<AddressUtxosResponse>>() {}.type))
    }

    override fun sendRawTransaction(hex: String): Single<String> {
        return Single.just("f0bb52d0023e3a81d6d95c4772ae003541554da6f2a962ddd77f540e6181d9cd")
    }

    override fun getTransactions(request: TxDetailsRequest): Single<List<TxResponse>> {
        var json = resource("rest_txdetails_aar_bch.json")
        // Check for AAR genesis
        if (request.txids.count() == 1 && request.txids[0] == "b75d9a2f2251deea547f80358158817e791671b865a3f1a80da840e4a9893772") {
            json = resource("rest_txdetails_aar_genesis.json")
        }
        val response = gson.fromJson<List<TxResponse>>(json, object : TypeToken<List<TxResponse>>() {}.type)
        return Single.just(response)
    }

    override fun validateSlpTxs(request: SlpValidateTxRequest): Single<List<SlpValidateTxResponse>> {
        val response = request.txids
            .map { SlpValidateTxResponse(it, true) }
        return Single.just(response)
    }

    private fun resource(filename: String) = this.javaClass.classLoader!!.getResource(filename).readText()

}


