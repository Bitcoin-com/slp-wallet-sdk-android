package com.bitcoin.slpwallet.tx

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.bitcoin.slpwallet.encoding.ByteUtils

/**
 * @author akibabu
 */
@Entity(tableName = "utxo", primaryKeys = ["tx_id", "index"])
internal data class Utxo(
    @ColumnInfo(name = "tx_id") val txId: String,
    @ColumnInfo(name = "index") val index: Int,
    @ColumnInfo(name = "cash_address") val cashAddress: String,
    @ColumnInfo(name = "script_hex") val scriptHex: String,
    @ColumnInfo(name = "satoshi") val satoshi: Long
) {

    val scriptBytes: ByteArray
        get() = ByteUtils.Hex.decode(scriptHex)

}
