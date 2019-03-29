package com.bitcoin.slpwallet.address

import com.bitcoin.slpwallet.Network
import com.bitcoin.slpwallet.bitcoinj.AddressCashUtil

/**
 * @author akibabu
 */
class AddressSLP internal constructor(network: Network, version: Int, bytes: ByteArray) :
    Address(network, version, bytes) {

    private val type: AddressType = AddressType.fromVersion(version.toByte())

    override fun toSlp(): AddressSLP {
        return this
    }

    override fun toString(): String {
        return AddressCashUtil.encodeCashAddress(prefix(network), AddressCashUtil.packAddressData(bytes, type.byte))
    }

    companion object {

        fun parse(network: Network, address: String): AddressSLP {
            val parts = address.split(":")
            val prefix = prefix(network)
            if (parts.size != 2 || parts[0] != prefix) {
                throw AddressFormatException("Not SLP address $address")
            }
            val bytes = AddressCashUtil.decode(prefix, address)
            return AddressSLP(network, bytes.version.toInt(), bytes.bytes)
        }

        internal fun prefix(network: Network): String {
            return when (network) {
                Network.MAIN -> "simpleledger"
                Network.TEST -> "slptest"
            }
        }
    }

}
