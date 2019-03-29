package com.bitcoin.wallet.slp

import com.bitcoin.wallet.encoding.ByteUtils
import com.bitcoin.wallet.tx.Script
import java.util.*

/**
 * @author akibabu
 */
internal open class SlpOpReturn(
    open val tokenType: SlpTokenType,
    val transactionType: SlpTransactionType,
    open val tokenId: SlpTokenId) {

    // Interface of GENESIS and MINT transaction
    internal interface BatonAndMint {
        val batonVout: UInt? // May be null in which case the baton is destroyed
        val mintedAmount: ULong // The minted amount received by vout[1] of this transaction
    }

    companion object {

        internal val LOKAD: ByteArray = byteArrayOf(83, 76, 80, 0) // "SLP" 4 byte zero padding
        private const val MIN_CHUNKS: Int = 6 // Number of required script chunks for smallest (SEND) SLP transaction
        internal const val MAX_QUANTITIES: Int = 19

        private const val OP_RETURN_NUM_BYTES_BASE = 55 // OP return script with lokad, tokentype, 'SEND', token id, satoshi value 0
        private const val QUANTITY_NUM_BYTES = 9 // 8 unsigned bytes + size byte
        fun tryParse(txId: String, scriptHex: String): SlpOpReturn? {
            return tryParse(txId, Script(ByteUtils.Hex.decode(scriptHex)))
        }

        private fun tryParse(txId: String, script: Script): SlpOpReturn? {
            if (!script.isOpReturn) {
                return null
            }
            val chunks = script.chunks
            if (chunks.size < MIN_CHUNKS || !Arrays.equals(LOKAD, chunks[1])) {
                return null
            }
            val tokenType = chunks[2]?.let { SlpTokenType.tryParse(it) } ?: return null
            val transactionType = chunks[3]?.let { SlpTransactionType.tryParse(it) } ?: return null
            val tokenId = if (transactionType == SlpTransactionType.GENESIS) {
                SlpTokenId(txId)
            } else {
                chunks[4]?.let { SlpTokenId.tryParse(it) } ?: return null
            }
            return when (transactionType) {
                SlpTransactionType.SEND -> SlpOpReturnSend.create(tokenType, tokenId, chunks)
                SlpTransactionType.MINT -> SlpOpReturnMint.create(tokenType, tokenId, chunks)
                SlpTransactionType.GENESIS -> SlpOpReturnGenesis.create(tokenType, tokenId, chunks)
            }
        }

        fun sizeInBytes(numQuantities: Int): Int {
            return OP_RETURN_NUM_BYTES_BASE + numQuantities * QUANTITY_NUM_BYTES
        }

        fun send(tokenId: SlpTokenId, quantities: List<ULong>): SlpOpReturnSend {
            return SlpOpReturnSend(SlpTokenType.PERMISSIONLESS, tokenId, quantities)
        }

    }

}
