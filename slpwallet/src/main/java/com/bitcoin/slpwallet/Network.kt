package com.bitcoin.slpwallet

import com.bitcoin.slpwallet.bitcoinj.NetworkInstance

/**
 * @author akibabu
 */
enum class Network {

    MAIN,
    TEST;

    internal val instance: NetworkInstance by lazy { NetworkInstance(this) }

}
