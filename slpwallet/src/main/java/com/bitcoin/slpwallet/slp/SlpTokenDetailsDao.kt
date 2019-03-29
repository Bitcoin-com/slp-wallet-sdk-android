package com.bitcoin.slpwallet.slp

import androidx.room.Dao
import androidx.room.Query
import com.bitcoin.slpwallet.persistence.DaoBase

/**
 * @author akibabu
 */
@Dao
internal interface SlpTokenDetailsDao : DaoBase<SlpTokenDetails> {

    @Query("SELECT * FROM token_details where token_id in (:tokenIds)")
    fun findByIds(tokenIds: Set<SlpTokenId>): List<SlpTokenDetails>

}