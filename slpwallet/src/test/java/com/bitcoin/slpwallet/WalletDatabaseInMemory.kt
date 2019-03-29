package com.bitcoin.slpwallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bitcoin.slpwallet.persistence.DaoBase
import com.bitcoin.slpwallet.persistence.SlpTokenBalance
import com.bitcoin.slpwallet.persistence.SlpTokenBalanceDao
import com.bitcoin.slpwallet.persistence.WalletDatabase
import com.bitcoin.slpwallet.slp.*
import com.bitcoin.slpwallet.tx.Utxo
import com.bitcoin.slpwallet.tx.UtxoDao
import java.util.concurrent.ConcurrentHashMap

/**
 * @author akibabu
 */
internal class WalletDatabaseInMemory : WalletDatabase {

    override fun slpValidTxDao(): SlpValidTxDao {
        return SlpValidTxDaoInMemory()
    }

    override fun tokenBalanceDao(): SlpTokenBalanceDao {
        return TokenBalanceDaoInMemory()
    }

    override fun utxoDao(): UtxoDao {
        return UtxoDaoInMemory()
    }

    override fun slpUtxoDao(): SlpUtxoDao {
        return SlpUtxoDaoInMemory()
    }

    override fun slpTokenDetailsDao(): SlpTokenDetailsDao {
        return SlpTokenDetailsDaoInMemory()
    }

    override fun newWalletClear() {

    }

    override fun awaitReady(): Boolean {
        return true
    }
}

internal abstract class DaoInMemoryBase<T> : DaoBase<T> {

    override fun save(vararg values: T) {
        save(values.toList())
    }
}

internal class SlpValidTxDaoInMemory : SlpValidTxDao, DaoInMemoryBase<SlpValidTx>() {

    private val repo: MutableMap<String, SlpValidTx> = ConcurrentHashMap()

    override fun findByIds(txIds: Set<String>): List<SlpValidTx> {
        return txIds.mapNotNull { repo[it] }
    }

    override fun save(values: Collection<SlpValidTx>) {
        values.forEach { repo[it.txId] = it }
    }

}

internal class SlpTokenDetailsDaoInMemory : SlpTokenDetailsDao, DaoInMemoryBase<SlpTokenDetails>()  {

    private val repo: MutableMap<SlpTokenId, SlpTokenDetails> = ConcurrentHashMap()

    override fun findByIds(tokenIds: Set<SlpTokenId>): List<SlpTokenDetails> {
        return tokenIds
            .mapNotNull { repo[it] }
    }

    override fun save(details: Collection<SlpTokenDetails>) {
        details.forEach {
            repo[it.tokenId] = it
        }
    }
}

internal class SlpUtxoDaoInMemory : SlpUtxoDao, DaoInMemoryBase<SlpUtxo>()  {

    private val repo: MutableMap<Pair<String, Int>, SlpUtxo> = ConcurrentHashMap()

    override fun delete(txId: String, index: Int) {
        repo.remove(Pair(txId, index))
    }

    override fun findAll(): List<SlpUtxo> {
        return repo.values.toList()
    }

    override fun findAllTxIds(): List<String> {
        return repo.values.map { it.utxo.txId }
    }

    override fun save(utxos: Collection<SlpUtxo>) {
        utxos.forEach {
            repo[Pair(it.utxo.txId, it.utxo.index)] = it
        }
    }
}

internal class UtxoDaoInMemory : UtxoDao, DaoInMemoryBase<Utxo>() {

    private val repo: MutableMap<Pair<String, Int>, Utxo> = ConcurrentHashMap()

    override fun delete(txId: String, index: Int) {
        repo.remove(Pair(txId, index))
    }

    override fun findAll(): List<Utxo> {
        return repo.values.toList()
    }

    override fun findAllTxIds(): List<String> {
        return repo.values.map { it.txId }
    }

    override fun save(utxos: Collection<Utxo>) {
        utxos.forEach {
            repo[Pair(it.txId, it.index)] = it
        }
    }
}

internal class TokenBalanceDaoInMemory : SlpTokenBalanceDao {

    override fun delete(vararg tokenId: String): Int {
        return 0
    }

    override fun getBalances(): List<SlpTokenBalance> {
        return listOf()
    }

    override fun getBalancesLive(): LiveData<List<SlpTokenBalance>> {
        return MutableLiveData()
    }

    override fun upsertBalance(vararg balance: SlpTokenBalance) {
    }

}