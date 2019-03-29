package com.bitcoin.wallet.bitcoinj

import com.bitcoin.wallet.Network
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params

/**
 * @author akibabu
 */
internal class NetworkInstance(val network: Network) {

    val parameters: NetworkParameters = if (network == Network.MAIN) MainNetParams.get() else TestNet3Params.get()

}