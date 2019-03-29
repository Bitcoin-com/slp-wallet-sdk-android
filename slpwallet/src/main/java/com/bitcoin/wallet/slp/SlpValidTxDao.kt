package com.bitcoin.wallet.slp

import androidx.room.Dao
import androidx.room.Query
import com.bitcoin.wallet.persistence.DaoBase

/**
 * @author akibabu
 */
@Dao
internal interface SlpValidTxDao : DaoBase<SlpValidTx> {

    @Query("SELECT * FROM slp_valid_tx where tx_id in (:txIds)")
    fun findByIds(txIds: Set<String>): List<SlpValidTx>

}