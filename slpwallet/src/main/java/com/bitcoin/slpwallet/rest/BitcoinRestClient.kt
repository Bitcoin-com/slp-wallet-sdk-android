package com.bitcoin.slpwallet.rest

import com.bitcoin.slpwallet.Network
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * @author akibabu
 */
internal interface BitcoinRestClient {

    @POST("address/utxo")
    fun getUtxos(@Body request: AddressUtxosRequest): Single<List<AddressUtxosResponse>>

    @GET("rawtransactions/sendRawTransaction/{hex}")
    fun sendRawTransaction(@Path("hex") hex: String): Single<String>

    @POST("transaction/details")
    fun getTransactions(@Body request: TxDetailsRequest): Single<List<TxResponse>>

    @POST("slp/validateTxid")
    fun validateSlpTxs(@Body request: SlpValidateTxRequest): Single<List<SlpValidateTxResponse>>


    companion object {
        private val clients : ConcurrentHashMap<Network, BitcoinRestClient> = ConcurrentHashMap()

        @Synchronized
        fun getInstance(network: Network) : BitcoinRestClient {
            // computeIfAbsent not present in API 21. getOrPut is not thread safe so the method is synchronized
            return clients.getOrPut(network) { Retrofit.getInstance(network).create(BitcoinRestClient::class.java) }
        }

    }
}
