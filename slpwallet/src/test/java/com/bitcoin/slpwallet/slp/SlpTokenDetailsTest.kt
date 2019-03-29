package com.bitcoin.slpwallet.slp

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class SlpTokenDetailsTest {

    @Test
    fun toRawAmount_happy() {
        val decimals = 6
        val details = SlpTokenDetails(SlpTokenId(""), "", "", decimals)
        val expectedAmount : ULong = 12530000u
        assertEquals(expectedAmount, details.toRawAmount(BigDecimal("12.53")))
    }

    @Test
    fun toRawAmount_zeroDecimals() {
        val decimals = 0
        val details = SlpTokenDetails(SlpTokenId(""), "", "", decimals)
        val expectedAmount : ULong = 12u
        assertEquals(expectedAmount, details.toRawAmount(BigDecimal("12")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun toRawAmount_tooManyDecimals() {
        val details = SlpTokenDetails(SlpTokenId(""), "", "", 0)
        details.toRawAmount(BigDecimal("23.1"))
    }

    @Test
    fun toRawAmount_supportUnsigned8Bytes() {
        val decimals = 5
        val details = SlpTokenDetails(SlpTokenId(""), "", "", decimals)
        val expectedAmount = ULong.MAX_VALUE - 1000u
        assertEquals(expectedAmount, details.toRawAmount(BigDecimal("184467440737095.50615")))
    }

    @Test
    fun toReadableAmount_supportUnsigned8Bytes() {
        val decimals = 6
        val details = SlpTokenDetails(SlpTokenId(""), "", "", decimals)
        val expectedAmount = BigDecimal("18446744073709.550615")
        assertEquals(expectedAmount, details.toReadableAmount(BigDecimal("18446744073709550615")))
    }

    @Test
    fun toReadableAmount_happy() {
        val decimals = 6
        val details = SlpTokenDetails(SlpTokenId(""), "", "", decimals)
        val expectedAmount = BigDecimal("12.53")
        assertEquals(expectedAmount, details.toReadableAmount(BigDecimal("12530000")))
    }

    @Test
    fun toReadableAmount_zeroDecimals() {
        val decimals = 0
        val details = SlpTokenDetails(SlpTokenId(""), "", "", decimals)
        val expectedAmount = BigDecimal("12")
        assertEquals(expectedAmount, details.toReadableAmount(BigDecimal("12")))
    }

}
