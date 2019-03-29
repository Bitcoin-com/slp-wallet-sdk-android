package com.bitcoin.wallet.slp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.bitcoin.wallet.*
import com.bitcoin.wallet.bitcoinj.Mnemonic
import com.bitcoin.wallet.persistence.SlpTokenBalance
import com.bitcoin.wallet.persistence.WalletDatabase
import com.bitcoin.wallet.presentation.BalanceInfo
import com.bitcoin.wallet.presentation.BalanceInfoImpl
import com.bitcoin.wallet.presentation.ProgressTask
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author akibabu
 */
internal class SLPWalletImpl(val network: Network, m: Mnemonic, private val database: WalletDatabase) : SLPWallet {

    internal val bchAddressKey = m.getAddress(network, 44, 145, 0)
    internal val slpKeyAddress = m.getAddress(network, 44, 245, 0)

    override val bchAddress = bchAddressKey.address.toCash().toString()
    override val slpAddress = slpKeyAddress.address.toSlp().toString()

    private val bchAddressAsCash = bchAddressKey.address.toCash().toString()
    // UTXO's come with cash address not SLP
    internal val slpAddressAsCash = slpKeyAddress.address.toCash().toString()

    override val mnemonic: List<String> = m.mnemonic
    override val balance: LiveData<List<BalanceInfo>>
        get() = getBalanceInfo()

    internal val service: WalletService = WalletServiceImpl(this, database)

    private val scheduler: Scheduler = DefaultScheduler

    private val mSendStatus: MutableLiveData<ProgressTask<String?>> = MutableLiveData<ProgressTask<String?>>()
        .apply { ProgressTask.idle<String?>() }
    override val sendStatus: LiveData<ProgressTask<String?>> = mSendStatus

    private val balanceIsBeingRefreshed: AtomicBoolean = AtomicBoolean(false)

    private val balanceDao = database.tokenBalanceDao()

    fun isMyCashAddress(address: String): Boolean {
        return address == bchAddressAsCash || address == slpAddressAsCash
    }

    private fun getBalanceInfo(): LiveData<List<BalanceInfo>> {
        return Transformations.map(balanceDao.getBalancesLive()) { data ->
            val newList: MutableList<BalanceInfo> = mutableListOf()
            for (slpTokenBalance: SlpTokenBalance in data) {
                newList.add(BalanceInfoImpl.fromSlpTokenBalance(slpTokenBalance))
            }
            newList
        }
    }

    override fun clearSendStatus() {
        mSendStatus.postValue(ProgressTask.idle())
    }

    @Synchronized
    override fun refreshBalance() {
        if (!database.awaitReady()) {
            return
        }
        if (balanceIsBeingRefreshed.compareAndSet(false, true)) {
            Timber.d("Starting balance refresh.")

            scheduler.execute {
                service.refreshBalance()
                    .observeOn(Schedulers.io())
                    .subscribe(
                    { walletBalance: WalletBalance ->
                        Timber.d("Received balance, now processing.")

                        val existingBalances: List<SlpTokenBalance> = balanceDao.getBalances()
                        val oldBalanceInNewData: MutableMap<String, Boolean> = mutableMapOf()
                        for (existingBalance: SlpTokenBalance in existingBalances) {
                            oldBalanceInNewData.set(existingBalance.tokenId, false)
                        }


                        val tokenBalances: MutableList<SlpTokenBalance> = mutableListOf()
                        for (balance: Map.Entry<SlpTokenDetails, BigDecimal> in walletBalance.tokenBalance.entries) {
                            val tokenDetails: SlpTokenDetails = balance.key
                            val tokenBalance = SlpTokenBalance(
                                tokenDetails.tokenId.hex,
                                balance.value,
                                tokenDetails.ticker,
                                tokenDetails.name,
                                tokenDetails.decimals
                            )
                            tokenBalances.add(tokenBalance)
                            oldBalanceInNewData[tokenDetails.tokenId.hex] = true
                        }

                        var bchBalance = SlpTokenBalance(
                            "",
                            BigDecimal(walletBalance.nativeBalance).divide(BigDecimal(1e8)),
                            "BCH",
                            "Bitcoin Cash",
                            8
                        )
                        tokenBalances.add(bchBalance)

                        val tokensToRemove: MutableList<String> = mutableListOf()
                        for (tokenId: String in oldBalanceInNewData.keys) {
                            if (tokenId.isNotEmpty() && oldBalanceInNewData[tokenId] == false) {
                                Timber.d("Adding token to remove with key: \"$tokenId\"")
                                tokensToRemove.add(tokenId)
                            }
                        }

                        balanceDao.upsertBalance(*tokenBalances.toTypedArray())
                        if (tokensToRemove.count() > 0) {
                            // I should not need to do this individually, but in one circumstance running on a physical
                            // phone, it would crash if I did not. More investigation required.
                            for (tokenIdToRemove: String in tokensToRemove) {
                                Timber.d("Removing token ID \"$tokenIdToRemove\"")
                                val tokensRemoved: Int = balanceDao.delete(tokenIdToRemove)
                                Timber.d("tokensRemoved: $tokensRemoved")
                            }
                        }
                        Timber.d("Finished storing new balance.")

                        balanceIsBeingRefreshed.set(false)

                    },
                    { error: Throwable ->
                        Timber.e("Error refreshing balance. $error")
                        balanceIsBeingRefreshed.set(false)
                    }

                )
            }

        } else {
            Timber.d("Skipped refreshing balance because it is already in progress.")
        }
    }

    override fun sendToken(tokenId: String, amount: BigDecimal, toAddress: String): Single<String>
    {
        Timber.d("sendToken()")
        return Single.fromCallable {
                Timber.d("sendToken() checking db.")
                if (!database.awaitReady()) {
                    throw Exception("Database not ready.")
                }
            }
            .observeOn(Schedulers.io())
            .flatMap {
                Timber.d("sendToken() setting status to be underway.")
                mSendStatus.postValue(ProgressTask.underway(null))

                service.sendTokenRx(SlpTokenId(tokenId), amount, toAddress)
            }
            .doOnSuccess { txid: String? ->
                Timber.d("sendToken() completed successfully with txid: $txid.")
                mSendStatus.postValue(ProgressTask.success(txid))
                refreshBalance()
            }
            .doOnError { e: Throwable? ->
                Timber.e("Error when sending. $e")
                mSendStatus.postValue(ProgressTask.error(e?.message ?: ""))
                refreshBalance()
            }
    }


    fun clearDatabase(): SLPWallet {
        database.newWalletClear()
        return this
    }

}
