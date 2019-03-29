package com.bitcoin.slpwallet.address

import com.bitcoin.slpwallet.Network

/**
 * @author akibabu
 */
class AddressLegacy internal constructor(network: Network, version: Int, bytes: ByteArray) :
    Address(network, version, bytes) {

    override fun toLegacy(): AddressLegacy {
        return this
    }

    override fun toString(): String {
        return toBase58()
    }

    companion object {
        fun parse(network: Network, base58: String): AddressLegacy {
            try {
                val address = org.bitcoinj.core.Address.fromBase58(network.instance.parameters, base58)
                return AddressLegacy(network, address.version, address.hash160)
            } catch (e: org.bitcoinj.core.AddressFormatException) {
                throw AddressFormatException(e.message.toString())
            }
        }
    }
}
