package com.bitcoin.slpwallet.rest

import com.bitcoin.slpwallet.Network
import com.bitcoin.slpwallet.SLPWalletConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap

internal object Retrofit {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor {
                var request = it.request()
                SLPWalletConfig.restAPIKey?.let {
                    request = request.newBuilder().addHeader("Authorization", "BITBOX:$it").build()
                }
                it.proceed(request)
            }
            .build()
    }

    private val retrofit : ConcurrentHashMap<Network, Retrofit> = ConcurrentHashMap()

    @Synchronized
    fun getInstance(network: Network): Retrofit {
        val baseUrl = when (network) {
            Network.MAIN -> "https://rest.bitcoin.com/v2/"
            Network.TEST -> "https://trest.bitcoin.com/v2/"
        }
        // computeIfAbsent not present in API 21. getOrPut is not thread safe so the method is synchronized
        return retrofit.getOrPut(network) { retrofit(baseUrl)}
    }

    private fun retrofit(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }


}