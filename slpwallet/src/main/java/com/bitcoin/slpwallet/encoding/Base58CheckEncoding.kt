package com.bitcoin.slpwallet.encoding

/**
 * https://github.com/bitcoinbook/bitcoinbook/blob/develop/images/mbc2_0406.png
 *
 * @author akibabu
 */
open class Base58CheckEncoding(protected val version: Int, protected val bytes: ByteArray) {

    fun toBase58(): String {
        val result = ByteArray(versionLength + bytes.size + checksumLength)
        result[0] = version.toByte()
        System.arraycopy(bytes, 0, result, 1, bytes.size)
        val checksum = Sha256Hash.hashTwice(result, 0, versionLength + this.bytes.size)
        System.arraycopy(checksum, 0, result, versionLength + bytes.size, checksumLength)
        return ByteUtils.Base58.encode(result)
    }

    companion object {
        private const val versionLength = 1
        private const val checksumLength = 4
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Base58CheckEncoding

        if (version != other.version) return false
        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version
        result = 31 * result + bytes.contentHashCode()
        return result
    }

}
