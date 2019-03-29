package com.bitcoin.wallet.slp

/**
 * Minimum 4 bytes ASCII
 *
 * @author akibabu
 */
internal enum class SlpTransactionType(private val text: String) {

    SEND("SEND"),
    MINT("MINT"),
    GENESIS("GENESIS");

    val bytes: ByteArray by lazy { text.toByteArray(Charsets.US_ASCII) }

    companion object {
        private val deserializer = SlpTransactionType.values().associateBy({ it.text }, { it })

        fun tryParse(bytes: ByteArray): SlpTransactionType? {
            return deserializer[bytes.toString(Charsets.US_ASCII)]
        }
    }

}


