package com.bitcoin.wallet.address

import com.bitcoin.wallet.Network
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author akibabu
 */
class AddressSLPTest {

    private val network = Network.MAIN
    private val addressSlp = "simpleledger:qrkn34tllug35tfs655e649asx4udw4dcc3lrh0wxa"
    private val addressCash = "bitcoincash:qrkn34tllug35tfs655e649asx4udw4dccaygv6wcr"
    private val addressLegacy = "1NdKEMnDXhUeyCLERGkJxZMEQeKG7CsUd9"

    @Test(expected = AddressFormatException::class)
    fun parseCashAddressFails() {
        AddressSLP.parse(network, addressCash)
    }

    @Test(expected = AddressFormatException::class)
    fun parseLegacyAddressFails() {
        AddressSLP.parse(network, addressLegacy)
    }

    @Test
    fun parseAddressSlpFromAddress() {
        val address = Address.parse(network, addressSlp)
        assertTrue(address is AddressSLP)
        assertEquals(addressSlp, address.toString())
        assertEquals(addressLegacy, address.toBase58())
    }

    @Test
    fun parseAddressSlp() {
        val address = AddressSLP.parse(network, addressSlp)
        assertEquals(addressSlp, address.toString())
        assertEquals(addressLegacy, address.toBase58())
    }

    @Test
    fun addressSlpToAddressLegacy() {
        val legacy = AddressSLP.parse(network, addressSlp).toLegacy()
        assertEquals(addressLegacy, legacy.toString())
        assertEquals(addressLegacy, legacy.toBase58())
    }

    @Test
    fun addressSlpToAddressCash() {
        val cash = AddressSLP.parse(network, addressSlp).toCash()
        assertEquals(addressCash, cash.toString())
        assertEquals(addressLegacy, cash.toBase58())
    }

}
