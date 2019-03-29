package com.bitcoin.slpwallet.encoding

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * @author akibabu
 */
internal object Sha256Hash {

    private val sha256: MessageDigest
        get() {
            try {
                return MessageDigest.getInstance("SHA-256")
            } catch (e: NoSuchAlgorithmException) {
                throw RuntimeException(e)
            }

        }

    fun hashTwice(bytes: ByteArray, offset: Int, length: Int): ByteArray {
        val sha256 = sha256
        sha256.update(bytes, offset, length)
        return sha256.digest(sha256.digest())
    }

}
