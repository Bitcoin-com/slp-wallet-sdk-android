package com.bitcoin.slpwallet

import android.content.Context
import androidx.lifecycle.LiveData
import com.bitcoin.securepreferences.SecurePreferences
import com.bitcoin.slpwallet.bitcoinj.Mnemonic
import com.bitcoin.slpwallet.persistence.WalletDatabaseImpl
import com.bitcoin.slpwallet.presentation.BalanceInfo
import com.bitcoin.slpwallet.presentation.ProgressTask
import com.bitcoin.slpwallet.slp.SLPWalletImpl
import io.reactivex.Single
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

/**
 * @author akibabu
 */
interface SLPWallet {

    val bchAddress: String
    val slpAddress: String
    val mnemonic: List<String>
    val balance: LiveData<List<BalanceInfo>>
    val sendStatus: LiveData<ProgressTask<String?>>

    fun clearSendStatus()
    fun refreshBalance()
    fun sendToken(tokenId: String, amount: BigDecimal, toAddress: String): Single<String>

    companion object {
        private const val PREFS_NAMESPACE: String = "com.bitcoin.wallet.SLPWallet"
        private const val PREFS_KEY_MNEMONIC: String = "mnemonic"
        private const val WALLET_KEY = true

        // computeIfAbsent is not available in api 21 so we need extra synchronization below. See code for getOrPut
        private val slpWallet: ConcurrentHashMap<Boolean, SLPWallet> = ConcurrentHashMap()

        fun fromMnemonic(context: Context, network: Network, mnemonic: String, isNew: Boolean = true): SLPWallet {
            return fromMnemonic(context, network, mnemonic.split(" "), isNew)
        }

        fun fromMnemonic(context: Context, network: Network, mnemonic: List<String>, isNew: Boolean = true): SLPWallet {

            // TODO: Check if mnemonic is valid

            if (isNew) {
                val mnemonicPhrase: String = mnemonic.joinToString(" ")
                val securePrefs = SecurePreferences(context, PREFS_NAMESPACE)
                val prefsEditor: SecurePreferences.Editor = securePrefs.edit()
                prefsEditor.putString(PREFS_KEY_MNEMONIC, mnemonicPhrase)
                if (!prefsEditor.commit()) {
                    throw Exception("Failed to save mnemonic.")
                }
            }

            synchronized(slpWallet) {
                return if (isNew) {
                    slpWallet[WALLET_KEY] = newWallet(context, network, mnemonic).clearDatabase()
                    slpWallet[WALLET_KEY]!!
                } else {
                    slpWallet.getOrPut(WALLET_KEY) { newWallet(context, network, mnemonic) }
                }
            }
        }

        private fun newWallet(context: Context, network: Network, mnemonic: List<String>): SLPWalletImpl {
            return SLPWalletImpl(network, Mnemonic(mnemonic), WalletDatabaseImpl.getInstance(context))
        }

        fun getInstance(context: Context, network: Network = Network.MAIN): SLPWallet {
            synchronized(slpWallet) {
                return slpWallet.getOrPut(WALLET_KEY) { loadOrCreate(context, network) }
            }
        }

        private fun loadOrCreate(context: Context, network: Network): SLPWallet {
            var isNew = false
            val securePrefs = SecurePreferences(context, PREFS_NAMESPACE)
            var mnemonicPhrase: String? = null
            try {
                mnemonicPhrase = securePrefs.getString(PREFS_KEY_MNEMONIC)
            } catch (e: Exception) {
                Timber.e(e, "Exception when loading mnemonic.")
            }

            if (mnemonicPhrase == null) {
                val mnemonicList: List<String> = Mnemonic.generate().mnemonic
                mnemonicPhrase = mnemonicList.joinToString(" ")
                isNew = true
            }
            return fromMnemonic(context, network, mnemonicPhrase, isNew)
        }
    }

}
