package com.bitcoin.wallet.slp

import androidx.room.Dao
import androidx.room.Query
import com.bitcoin.wallet.persistence.DaoBase

/**
 * @author akibabu
 */
@Dao
internal interface SlpUtxoDao : DaoBase<SlpUtxo> {

    @Query("DELETE FROM slp_utxo WHERE tx_id = :txId AND `index` = :index")
    fun delete(txId: String, index: Int)

    @Query("SELECT * FROM slp_utxo")
    fun findAll(): List<SlpUtxo>

    @Query("SELECT tx_id FROM slp_utxo")
    fun findAllTxIds(): List<String>

}