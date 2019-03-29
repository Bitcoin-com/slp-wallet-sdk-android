package com.bitcoin.slpwallet.bitcoinj

import com.bitcoin.slpwallet.Network
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params

/**
 * @author akibabu
 */
internal class NetworkInstance(val network: Network) {

    val parameters: NetworkParameters = if (network == Network.MAIN) MainNetParams.get() else TestNet3Params.get()

}