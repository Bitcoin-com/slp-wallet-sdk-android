package com.bitcoin.slpwallet

import com.bitcoin.slpwallet.rest.BitcoinRestClientMock
import com.bitcoin.slpwallet.rest.BitcoinRestClientMockAarAngBch
import com.bitcoin.slpwallet.rest.BitcoinRestClientMockAarBch
import com.bitcoin.slpwallet.slp.SlpTokenDetails
import com.bitcoin.slpwallet.slp.SlpTokenId
import com.bitcoin.slpwallet.tx.TxBuilder
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * @author akibabu
 */
class WalletServiceImplTest {

    @Test
    fun testSameBalanceAsExplorer() {
        val client = BitcoinRestClientMock()
        val service = WalletServiceImpl(client.wallet, client, WalletDatabaseInMemory())
        val tokenBalances = client.tokenBalances

        // Run 3 times to test the UTXOStore of corruption
        repeat(3) {
            val (bch, tokens) = service.refreshBalance().blockingGet()
            // explorer.bitcoin.com includes the dust limit for a token which we don't
            //assertEquals(client.bchBalance, bch + TxBuilder.DUST_LIMIT) // TODO calculate what we expect on bch path

            assertEquals(3, tokens.size)
            tokens.forEach { (details, balance) ->
                run {
                    assertEquals(tokenBalances[details.tokenId.hex], balance)
                }
            }
        }
    }

    @Test
    fun sendToken_insufficientBalance() {
        val client = BitcoinRestClientMock()
        val service = WalletServiceImpl(client.wallet, client, WalletDatabaseInMemory())
        val xrpToken = SlpTokenId("263ca75dd8ab35e699808896255212b374f2fb185fb0389297a11f63d8d41f7e")
        val tokenDetails = SlpTokenDetails(xrpToken, "XRP", "Ripple", 6)
        val tokenBalance = service.refreshBalance().blockingGet().tokenBalance[tokenDetails]

        service.sendTokenUtxoSelection(xrpToken, tokenBalance!!.plus(BigDecimal.ONE))
            .subscribe({ Assert.fail("succeeded") }, {
                assertTrue(it.message!!.contains("insufficient"))

            })
    }

    @Test
    fun sendToken_sendEntireBalance() {
        val client = BitcoinRestClientMock()
        val service = WalletServiceImpl(client.wallet, client, WalletDatabaseInMemory())
        val xrpToken = SlpTokenId("263ca75dd8ab35e699808896255212b374f2fb185fb0389297a11f63d8d41f7e")
        val tokenDetails = SlpTokenDetails(xrpToken, "XRP", "Ripple", 6)
        val tokenBalance = service.refreshBalance().blockingGet().tokenBalance[tokenDetails]

        service.sendTokenUtxoSelection(xrpToken, tokenBalance!!)
            .subscribe({  }, {
                it.printStackTrace()
                Assert.fail("couldn't send")
            })
    }

    @Test
    fun sendToken_assertSelection() {
        val client = BitcoinRestClientMock()
        val service = WalletServiceImpl(client.wallet, client, WalletDatabaseInMemory())
        val xrpToken = SlpTokenId("263ca75dd8ab35e699808896255212b374f2fb185fb0389297a11f63d8d41f7e")
        service.refreshBalance().blockingGet()
        val tokenDetails = SlpTokenDetails(xrpToken, "XRP", "Ripple", 6)
        val tokenBalance = service.refreshBalance().blockingGet().tokenBalance[tokenDetails]
        val sendAmount = BigDecimal("25.5")

        val selection = service.sendTokenUtxoSelection(xrpToken, sendAmount).blockingGet()
        // Check token amount to send and change
        assertEquals(selection.quantities[0], tokenDetails.toRawAmount(sendAmount))
        assertEquals(selection.quantities[1], tokenDetails.toRawAmount(tokenBalance!!.minus(sendAmount)))

        // Sanity check BCH cost is reasonable
        val selectedSatoshis = selection.selectedUtxos.map { it.satoshi }.sum()
        val netLossBch = selectedSatoshis - selection.changeSatoshi
        assertTrue(netLossBch > TxBuilder.DUST_LIMIT + 200)
        assertTrue(netLossBch < TxBuilder.DUST_LIMIT + 2000)
    }

    @Test
    fun givenSlpUtxoNearDustLimit_whenSendToken_thenHaveOutputForTokenChange() {
        // GIVEN
        val client = BitcoinRestClientMockAarBch()
        val service = WalletServiceImpl(client.wallet, client, WalletDatabaseInMemory())
        val tokenId = SlpTokenId("b75d9a2f2251deea547f80358158817e791671b865a3f1a80da840e4a9893772")

        // WHEN
        val sendAmount = BigDecimal("1.1")
        val selection: WalletServiceImpl.SendTokenUtxoSelection = service.sendTokenUtxoSelection(tokenId, sendAmount).blockingGet()

        // THEN
        // Check token amount to send and change
        assertEquals(110.toULong(), selection.quantities[0])
        assertEquals(572.toULong(), selection.quantities[1])

        assertEquals(2, selection.selectedUtxos.size)
        // Sanity check BCH cost is reasonable
        val selectedSatoshis = selection.selectedUtxos.map { it.satoshi }.sum()
        val netLossBch = selectedSatoshis - selection.changeSatoshi
        assertTrue(netLossBch > TxBuilder.DUST_LIMIT + 200)
        assertTrue(netLossBch < TxBuilder.DUST_LIMIT + 2000)
    }

    @Test
    fun givenSlpUtxoNearDustLimitAndBchAndOtherTokenUtxo_whenSendToken_thenBchUsedAndOtherTokenRemains() {
        // GIVEN
        val client = BitcoinRestClientMockAarAngBch()
        val service = WalletServiceImpl(client.wallet, client, WalletDatabaseInMemory())
        val tokenId = SlpTokenId("b75d9a2f2251deea547f80358158817e791671b865a3f1a80da840e4a9893772")

        // WHEN
        val sendAmount = BigDecimal("1.1")
        val selection: WalletServiceImpl.SendTokenUtxoSelection = service.sendTokenUtxoSelection(tokenId, sendAmount).blockingGet()

        // THEN
        // Check token amount to send and change
        assertEquals(110.toULong(), selection.quantities[0])
        assertEquals(572.toULong(), selection.quantities[1])

        val availableUxtos = mapOf(
            "AAR" to mapOf(
                "txid" to "71c81c6904d517d9057ec76b5b8188401df479ff7673a3c3ea7128480f54ca44",
                "vout" to 2
            ),
            "ANG" to mapOf(
                "txid" to "2460c85e7f782bd3b7c9816687c6c2736900367fd1c6efee60953c3a23f010ac",
                "vout" to 2
            ),
            "BCH" to mapOf(
                "txid" to "4e9a367ef6a1692a6a76e670d131ee13c3e5810b2373bc20a0e91d6710479a18",
                "vout" to 1
            )
        )

        assertEquals(2, selection.selectedUtxos.size)

        assertEquals(availableUxtos["AAR"]?.get("txid") as String, selection.selectedUtxos[0].txId)
        assertEquals(availableUxtos["AAR"]?.get("vout") as Int, selection.selectedUtxos[0].index)

        assertEquals(availableUxtos["BCH"]?.get("txid") as String, selection.selectedUtxos[1].txId)
        assertEquals(availableUxtos["BCH"]?.get("vout") as Int, selection.selectedUtxos[1].index)

        // Sanity check BCH cost is reasonable
        val selectedSatoshis = selection.selectedUtxos.map { it.satoshi }.sum()
        val netLossBch = selectedSatoshis - selection.changeSatoshi
        assertTrue(netLossBch > TxBuilder.DUST_LIMIT + 200)
        assertTrue(netLossBch < TxBuilder.DUST_LIMIT + 2000)
    }

}