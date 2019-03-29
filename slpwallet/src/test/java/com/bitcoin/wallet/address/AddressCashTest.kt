package com.bitcoin.wallet.address

import com.bitcoin.wallet.Network
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * @author akibabu
 */
class AddressCashTest {

    private val network = Network.MAIN
    private val addressSlp = "simpleledger:qrkn34tllug35tfs655e649asx4udw4dcc3lrh0wxa"
    private val addressCash = "bitcoincash:qrkn34tllug35tfs655e649asx4udw4dccaygv6wcr"
    private val addressLegacy = "1NdKEMnDXhUeyCLERGkJxZMEQeKG7CsUd9"

    @Test(expected = AddressFormatException::class)
    fun parseSLPAddressFails() {
        AddressCash.parse(network, addressSlp)
    }

    @Test(expected = AddressFormatException::class)
    fun parseLegacyAddressFails() {
        AddressCash.parse(network, addressLegacy)
    }

    @Test
    fun parseAddressCashFromAddress() {
        val address = Address.parse(network, addressCash)
        assertTrue(address is AddressCash)
        assertEquals(addressCash, address.toString())
    }

    @Test
    fun parseAddressCash() {
        val address = AddressCash.parse(network, addressCash)
        assertEquals(addressCash, address.toString())
    }

    @Test
    fun addressCashToAddressLegacy() {
        val address = AddressCash.parse(network, addressCash).toLegacy()
        assertEquals(addressLegacy, address.toString())
        assertEquals(addressLegacy, address.toBase58())
    }

    @Test
    fun addressCashToAddressSlp() {
        val address = AddressCash.parse(network, addressCash).toSlp()
        assertEquals(addressSlp, address.toString())
        assertEquals(addressLegacy, address.toBase58())
    }

}