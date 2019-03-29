package com.bitcoin.slpwallet.slp

import com.bitcoin.slpwallet.encoding.ByteUtils
import com.bitcoin.slpwallet.slp.SlpOpReturn.BatonAndMint

/**
 * @author akibabu
 */
internal class SlpOpReturnMint(
    tokenType: SlpTokenType,
    tokenId: SlpTokenId,
    override val batonVout: UInt?,
    override val mintedAmount: ULong
) : SlpOpReturn(tokenType, SlpTransactionType.SEND, tokenId), BatonAndMint {

    companion object {

        fun create(tokenType: SlpTokenType, tokenId: SlpTokenId, chunks: List<ByteArray?>): SlpOpReturnMint? {
            val batonByte: Byte? = chunks[4]?.let { it[0] } ?: return null
            val mintedAmount = chunks[5]?.let { it }.let { ByteUtils.toULong(it) } ?: return null
            return SlpOpReturnMint(tokenType, tokenId, batonByte?.toUInt(), mintedAmount)
        }
    }

}

