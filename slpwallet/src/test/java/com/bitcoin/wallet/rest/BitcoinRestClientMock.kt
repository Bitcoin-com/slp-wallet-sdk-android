package com.bitcoin.wallet.rest

import com.bitcoin.wallet.Network
import com.bitcoin.wallet.WalletDatabaseInMemory
import com.bitcoin.wallet.address.Address
import com.bitcoin.wallet.bitcoinj.Mnemonic
import com.bitcoin.wallet.slp.SLPWalletImpl
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Single
import java.math.BigDecimal

/**
 * Snapshot of and address with expected balances
 *
 * @author akibabu
 */
internal class BitcoinRestClientMock : BitcoinRestClient {

    val mnemonic = "poem ranch right divorce orphan swim join bleak theme punch feature place".split(" ")

    val address = Address.parse(Network.MAIN, "bitcoincash:qpfsv39800lr75qzhwqjzqtqlqp9r4pk4spq58nxdk")

    // Taken from explorer.bitcoin.com at the time
    val tokenBalances = mapOf(
        "3257135d7c351f8b2f46ab2b5e610620beb7a957f3885ce1787cffa90582f503" to BigDecimal("16"),
        "8fff3e288b53d49f7469159c3f535f500eeeb3d01d6b7474497a7658d6a0193d" to BigDecimal("23456"),
        "263ca75dd8ab35e699808896255212b374f2fb185fb0389297a11f63d8d41f7e" to BigDecimal("799956")
    )
    val bchBalance = BigDecimal("0.01067926").scaleByPowerOfTen(8).toLong()

    val wallet = SLPWalletImpl(Network.MAIN, Mnemonic(mnemonic), WalletDatabaseInMemory())
    private val gson = Gson()


    override fun getUtxos(request: AddressUtxosRequest): Single<List<AddressUtxosResponse>> {
        val json = resource("rest_utxos.json")
        return Single.just(gson.fromJson<List<AddressUtxosResponse>>(json, object: TypeToken<List<AddressUtxosResponse>>() {}.type))
    }

    override fun sendRawTransaction(hex: String): Single<String> {
        return Single.just("f0bb52d0023e3a81d6d95c4772ae003541554da6f2a962ddd77f540e6181d9cd")
    }

    override fun getTransactions(request: TxDetailsRequest): Single<List<TxResponse>> {
        val json = resource("rest_txdetails.json")
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


