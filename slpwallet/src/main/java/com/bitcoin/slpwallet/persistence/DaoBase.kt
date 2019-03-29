package com.bitcoin.slpwallet.persistence

import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * @author akibabu
 */
interface DaoBase<T> {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(vararg values: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(values: Collection<T>)

}