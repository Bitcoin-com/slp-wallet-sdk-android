package com.bitcoin.wallet.slp

import com.bitcoin.wallet.encoding.ByteUtils

/**
 * @author akibabu
 */
data class SlpTokenId(val hex: String) {

    companion object {
        fun tryParse(bytes: ByteArray): SlpTokenId? {
            if (bytes.size != 32) {
                return null
            }
            return SlpTokenId(ByteUtils.Hex.encode(bytes))
        }
    }

    override fun toString(): String {
        return hex
    }

    val bytes: ByteArray by lazy { ByteUtils.Hex.decode(hex) }

}