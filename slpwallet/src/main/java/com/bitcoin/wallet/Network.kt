package com.bitcoin.wallet

import com.bitcoin.wallet.bitcoinj.NetworkInstance

/**
 * @author akibabu
 */
enum class Network {

    MAIN,
    TEST;

    internal val instance: NetworkInstance by lazy { NetworkInstance(this) }

}
