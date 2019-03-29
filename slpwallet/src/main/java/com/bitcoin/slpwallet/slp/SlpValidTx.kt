package com.bitcoin.slpwallet.slp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @author akibabu
 */
@Entity(tableName = "slp_valid_tx")
data class SlpValidTx(
    @PrimaryKey @ColumnInfo(name = "tx_id") val txId: String,
    @ColumnInfo(name = "valid") val valid: Boolean)


