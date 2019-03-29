package com.bitcoin.slpwallet.slp

import com.bitcoin.slpwallet.encoding.ByteUtils
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

/**
 * @author akibabu
 */
class SlpOpReturnTest {

    // Taken from tx id "3963cb42b65afa4965c80dd265b3a75249681e18ef053a4d244b5ffbd4958303"
    private val bchLiveSlpSendOpReturn =
        "6a04534c500001010453454e4420263ca75dd8ab35e699808896255212b374f2fb185fb0389297a11f63d8d41f7e08000000ba43b7400008016343d2b750c580"

    private val bchLiveSlpTokenId = SlpTokenId("263ca75dd8ab35e699808896255212b374f2fb185fb0389297a11f63d8d41f7e")
    private val bchLiveNetGenesis =
        "6a04534c500001010747454e455349530358525006526970706c654c004c0001064c0008016345785d8a0000"

    @Test
    fun tryParse_fromHex() {
        assertNotNull(SlpOpReturn.tryParse("", bchLiveSlpSendOpReturn))
    }

    @Test
    fun tryParse_fromScript() {
        assertNotNull(SlpOpReturn.tryParse("", bchLiveSlpSendOpReturn))
    }

    @Test
    fun tryParse_genesis() {
        val genesis = SlpOpReturn.tryParse("asdf", bchLiveNetGenesis)!! as SlpOpReturnGenesis
        assertEquals("asdf", genesis.tokenId.hex)
        assertEquals(SlpTokenType.PERMISSIONLESS, genesis.tokenType)
        assertEquals("XRP", genesis.ticker)
        assertEquals("Ripple", genesis.name)
        assertEquals(6, genesis.decimals)
        assertEquals(BigDecimal(100000000000000000).toLong().toULong(), genesis.mintedAmount)
        assertNull(genesis.batonVout)
    }

    @Test
    fun isSendTrue() {
        assertTrue(SlpOpReturn.tryParse("", bchLiveSlpSendOpReturn) is SlpOpReturnSend)
    }

    @Test
    fun send_createScript() {
        val scriptBytes = SlpOpReturn.send(
            bchLiveSlpTokenId, listOf(
                800000000000u,
                99998189030000000u
            )
        ).createScript().program
        assertEquals(bchLiveSlpSendOpReturn, ByteUtils.Hex.encode(scriptBytes))
    }

    @Test(expected = IllegalArgumentException::class)
    fun send_requireAtLeastOneQuantitiy() {
        SlpOpReturn.send(bchLiveSlpTokenId, listOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun send_requireAllPositiveQuantities() {
        SlpOpReturn.send(bchLiveSlpTokenId, listOf(1u, 2u, 3u, 0u))
    }

    @Test
    fun getTokenType() {
        assertEquals(SlpTokenType.PERMISSIONLESS, SlpOpReturn.tryParse("", bchLiveSlpSendOpReturn)!!.tokenType)
    }

    @Test
    fun getTransactionType_send() {
        assertEquals(SlpTransactionType.SEND, SlpOpReturn.tryParse("", bchLiveSlpSendOpReturn)!!.transactionType)
    }

    @Test
    fun getTokenId() {
        assertEquals(bchLiveSlpTokenId, SlpOpReturn.tryParse("", bchLiveSlpSendOpReturn)!!.tokenId)
    }

    @Test
    fun getQuantities() {
        val quantities = listOf(800000000000u, 99998189030000000u)
        val send = SlpOpReturn.send(bchLiveSlpTokenId, quantities)
        assertEquals(quantities, send.quantities)
    }
}