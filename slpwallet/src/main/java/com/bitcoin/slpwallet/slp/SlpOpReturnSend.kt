package com.bitcoin.slpwallet.slp

import com.bitcoin.slpwallet.encoding.ByteUtils
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptChunk
import org.bitcoinj.script.ScriptOpCodes
import timber.log.Timber
import java.nio.ByteBuffer

/**
 * @author akibabu
 */
internal class SlpOpReturnSend(tokenType: SlpTokenType, tokenId: SlpTokenId, val quantities: List<ULong>) :
    SlpOpReturn(tokenType, SlpTransactionType.SEND, tokenId) {

    init {
        if (quantities.isEmpty() || quantities.size > MAX_QUANTITIES) {
            throw IllegalArgumentException("SLP SEND with ${quantities.size} quantities")
        }
        quantities
            .filter { it == ULong.MIN_VALUE }
            .firstOrNull { throw IllegalArgumentException("0 quantity") }
    }

    companion object {
        private const val MAX_CHUNKS_SEND = MAX_QUANTITIES + 5

        fun create(tokenType: SlpTokenType, tokenId: SlpTokenId, chunks: List<ByteArray?>): SlpOpReturnSend? {
            if (chunks.size > MAX_CHUNKS_SEND) {
                Timber.w("SLP SEND with more than $MAX_QUANTITIES quantities. Forced to ignore")
                return null
            }
            val quantities = chunks
                .filter { chunk -> chunk?.size == ULong.SIZE_BYTES }
                .map { ByteUtils.toULong(it!!) }

            return try {
                SlpOpReturnSend(tokenType, tokenId, quantities)
            } catch (e: IllegalArgumentException) {
                Timber.e(e)
                null
            }
        }
    }

    fun createScript(): org.bitcoinj.script.Script {
        val builder = ScriptBuilder()
            .op(ScriptOpCodes.OP_RETURN)
            .data(LOKAD)
            // .data() would not figure out this is 1 byte push-data
            .addChunk(ScriptChunk(tokenType.bytes.size, tokenType.bytes))
            .data(transactionType.bytes)
            .data(tokenId.bytes)
        quantities.forEach { builder.data(ByteBuffer.allocate(Long.SIZE_BYTES).putLong(it.toLong()).array()) }
        return builder.build()
    }

}

