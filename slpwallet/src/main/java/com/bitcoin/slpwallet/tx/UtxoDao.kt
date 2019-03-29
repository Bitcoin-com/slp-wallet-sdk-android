package com.bitcoin.slpwallet.tx

import androidx.room.Dao
import androidx.room.Query
import com.bitcoin.slpwallet.persistence.DaoBase

/**
 * @author akibabu
 */
@Dao
internal interface UtxoDao : DaoBase<Utxo> {

    @Query("DELETE FROM utxo WHERE tx_id = :txId AND `index` = :index")
    fun delete(txId: String, index: Int)

    @Query("SELECT * FROM utxo")
    fun findAll(): List<Utxo>

    @Query("SELECT tx_id FROM utxo")
    fun findAllTxIds(): List<String>

}