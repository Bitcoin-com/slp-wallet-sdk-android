package com.bitcoin.slpwallet.address


import com.bitcoin.slpwallet.Network
import com.bitcoin.slpwallet.encoding.Base58CheckEncoding

/**
 * @author akibabu
 */
abstract class Address(val network: Network, version: Int, bytes: ByteArray) :
    Base58CheckEncoding(assertVersion(network, version), assertLength(bytes)) {

    open fun toSlp(): AddressSLP {
        return AddressSLP(network, version, bytes)
    }

    open fun toCash(): AddressCash {
        return AddressCash(network, version, bytes)
    }

    open fun toLegacy(): AddressLegacy {
        return AddressLegacy(network, version, bytes)
    }

    companion object {
        private const val length = 20

        private fun assertVersion(network: Network, version: Int): Int {
            if (network.instance.parameters.acceptableAddressCodes.any { it == version }) {
                return version
            }
            throw AddressFormatException("Bad version $version")
        }

        private fun assertLength(bytes: ByteArray): ByteArray {
            if (bytes.size != length) {
                throw AddressFormatException("Bad address length ${bytes.size}")
            }
            return bytes
        }

        fun parse(network: Network, address: String): Address {
            if (address.startsWith(AddressCash.prefix(network))) {
                return AddressCash.parse(network, address)
            } else if (address.startsWith(AddressSLP.prefix(network))) {
                return AddressSLP.parse(network, address)
            }
            return AddressLegacy.parse(network, address)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as Address

        if (network != other.network) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + network.hashCode()
        return result
    }

}
