package com.bitcoin.wallet.address

import com.bitcoin.wallet.Network
import com.bitcoin.wallet.bitcoinj.AddressCashUtil

/**
 * @author akibabu
 */
class AddressCash internal constructor(network: Network, version: Int, bytes: ByteArray) :
    Address(network, version, bytes) {

    private val type: AddressType = AddressType.fromVersion(version.toByte())

    override fun toCash(): AddressCash {
        return this
    }

    override fun toString(): String {
        return AddressCashUtil.encodeCashAddress(prefix(network), AddressCashUtil.packAddressData(bytes, type.byte))
    }

    companion object {

        fun parse(network: Network, address: String): AddressCash {
            val parts = address.split(":")
            val prefix = prefix(network)
            if (parts.size != 2 || parts[0] != prefix) {
                throw AddressFormatException("Not cash address $address")
            }
            val bytes = AddressCashUtil.decode(prefix, address)
            return AddressCash(network, bytes.version.toInt(), bytes.bytes)
        }

        internal fun prefix(network: Network): String {
            return when (network) {
                Network.MAIN -> "bitcoincash"
                Network.TEST -> "bchtest"
            }
        }
    }

}
