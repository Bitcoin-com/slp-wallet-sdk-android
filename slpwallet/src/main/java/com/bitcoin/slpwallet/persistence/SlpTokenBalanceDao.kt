package com.bitcoin.slpwallet.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SlpTokenBalanceDao {
    @Query("Delete FROM SlpTokenBalance WHERE tokenId = :tokenId")
    fun delete(vararg tokenId: String): Int

    @Query("SELECT * FROM SlpTokenBalance ORDER BY name")
    fun getBalances(): List<SlpTokenBalance>

    @Query("SELECT * FROM SlpTokenBalance ORDER BY name")
    fun getBalancesLive(): LiveData<List<SlpTokenBalance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertBalance(vararg balance: SlpTokenBalance)

}