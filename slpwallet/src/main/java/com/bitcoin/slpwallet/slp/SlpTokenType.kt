package com.bitcoin.slpwallet.slp

import com.bitcoin.slpwallet.encoding.ByteUtils

/**
 * 1 to 2 byte integer
 *
 * @author akibabu
 */
internal enum class SlpTokenType(private val value: Int) {

    PERMISSIONLESS(1);

    // We will never have to work with the up to 2 bytes in the specification
    val bytes: ByteArray by lazy { byteArrayOf(value.toByte()) }

    companion object {
        private val deserializer = SlpTokenType.values().associateBy({ it.value }, { it })

        fun tryParse(bytes: ByteArray): SlpTokenType? {
            if (bytes.isEmpty() || bytes.size > 2) {
                return null
            }
            return deserializer[ByteUtils.toInt(bytes)]
        }
    }

}
