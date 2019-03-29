package com.bitcoin.wallet.slp

import com.bitcoin.wallet.encoding.ByteUtils
import com.bitcoin.wallet.slp.SlpOpReturn.BatonAndMint

/**
 * Except for containing token details a GENESIS transaction works exactly like MINT
 *
 * @author akibabu
 */
internal data class SlpOpReturnGenesis(
    override val tokenType: SlpTokenType,
    override val tokenId: SlpTokenId,
    val ticker: String,
    val name: String,
    val decimals: Int,
    override val batonVout: UInt?, // May be null in which case the baton is destroyed
    override val mintedAmount: ULong // The minted amount received by vout[1] of this transaction
) :
    SlpOpReturn(tokenType, SlpTransactionType.SEND, tokenId), BatonAndMint {

    val toDetails by lazy { SlpTokenDetails(tokenId, ticker, name, decimals) }

    companion object {

        fun create(tokenType: SlpTokenType, tokenId: SlpTokenId, chunks: List<ByteArray?>): SlpOpReturnGenesis? {
            val ticker = chunks[4]?.let { String(it) } ?: ""
            val name = chunks[5]?.let { String(it) } ?: ""
            val decimals = chunks[8]?.let { ByteUtils.toInt(it) } ?: return null
            val batonByte: Byte? = chunks[9]?.let { it.getOrNull(0) }
            val mintedAmount = chunks[10]?.let { it }.let { ByteUtils.toULong(it) } ?: return null
            return SlpOpReturnGenesis(tokenType, tokenId, ticker, name, decimals, batonByte?.toUInt(), mintedAmount)
        }
    }

}

