package com.bitcoin.slpwallet.bitcoinj

import com.bitcoin.slpwallet.Network
import com.bitcoin.slpwallet.address.Address
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class MnemonicTest {

    @Test
    fun generate() {
        assertEquals(12, Mnemonic.generate().mnemonic.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun requireExactly12Words() {
        Mnemonic(listOf("poem", "ranch"))
    }

    @Test
    fun toPrivatePublicPair() {
        val words = "poem ranch right divorce orphan swim join bleak theme punch feature place".split(" ")
        // private key known to work on livenet
        val expectedPrivateKey = BigInteger("1177172391896708055110291093735331024855236963476189947481813722291187889324")
        val expectedAddress = Address.parse(Network.MAIN, "bitcoincash:qpfsv39800lr75qzhwqjzqtqlqp9r4pk4spq58nxdk")

        val bchAddress = Mnemonic(words).getAddress(Network.MAIN, 44, 245, 0)

        assertEquals(expectedAddress.toSlp(), bchAddress.address.toSlp())
        assertEquals(expectedPrivateKey, bchAddress.privateKey)
    }
}