package com.bitcoin.wallet.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bitcoin.wallet.slp.*
import com.bitcoin.wallet.tx.Utxo
import com.bitcoin.wallet.tx.UtxoDao
import com.bitcoin.wallet.util.SingletonHolder
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

@Database(entities = [SlpTokenBalance::class, Utxo::class, SlpUtxo::class, SlpTokenDetails::class, SlpValidTx::class],
    version = 5)
@TypeConverters(com.bitcoin.wallet.persistence.TypeConverters::class)
internal abstract class WalletDatabaseImpl : RoomDatabase(), WalletDatabase {

    private var initialized = AtomicBoolean(true)
    abstract override fun tokenBalanceDao(): SlpTokenBalanceDao
    abstract override fun utxoDao(): UtxoDao
    abstract override fun slpUtxoDao(): SlpUtxoDao
    abstract override fun slpTokenDetailsDao(): SlpTokenDetailsDao
    abstract override fun slpValidTxDao(): SlpValidTxDao

    override fun newWalletClear() {
        initialized.set(false)
        val start = System.currentTimeMillis()
        Schedulers.io().scheduleDirect {
            clearAllTables()
            Timber.d("Cleared all database tables in ${System.currentTimeMillis() - start} ms")
            initialized.set(true)
        }
    }

    override fun awaitReady(): Boolean {
        for (i in 1..100) {
            if (initialized.get()) {
                return true
            }
            Thread.sleep(30)
        }
        Timber.e("timeout waiting for database to initialize")
        return false
    }

    companion object : SingletonHolder<WalletDatabaseImpl, Context>({
        Room.databaseBuilder(
            it.applicationContext, WalletDatabaseImpl::class.java,
            "com.bitcoin.wallet.slp.SLPWallet-db"
        ).fallbackToDestructiveMigration().build()
    })

}

