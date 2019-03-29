package com.bitcoin.slpwallet.bitcoinj

import com.bitcoin.slpwallet.Network
import com.bitcoin.slpwallet.address.Address
import com.bitcoin.slpwallet.address.KeyAddressPair
import org.bitcoinj.crypto.ChildNumber
import org.bitcoinj.crypto.HDKeyDerivation
import org.bitcoinj.crypto.MnemonicCode
import org.bitcoinj.wallet.DeterministicSeed
import java.security.SecureRandom

/**
 * @author akibabu
 */
internal class Mnemonic(val mnemonic: List<String>) {

    init {
        if (mnemonic.size != 12) {
            throw IllegalArgumentException("mnemonic should be 12 words long")
        }
    }

    // Master key without password protection
    private val masterKey = HDKeyDerivation.createMasterPrivateKey(MnemonicCode.toSeed(mnemonic, ""))

    companion object {
        private val mnemonic = MnemonicCode()
        private val random = SecureRandom()

        fun generate(): Mnemonic {
            val words = mnemonic.toMnemonic(
                random.generateSeed(DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8)
            )
            return Mnemonic(words)
        }
    }

    fun getAddress(network: Network, vararg hardenedPath: Int): KeyAddressPair {
        var key = masterKey
        for (path in hardenedPath) {
            key = key.derive(path)
        }
        key = HDKeyDerivation.deriveChildKey(HDKeyDerivation.deriveChildKey(
            key, ChildNumber(0, false)), ChildNumber(0, false))
        val address = Address.parse(network, key.toAddress(network.instance.parameters).toBase58())
        return KeyAddressPair(address, key.privKey)
    }

}