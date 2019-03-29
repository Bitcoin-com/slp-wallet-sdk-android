package com.bitcoin.wallet.slp

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import com.bitcoin.wallet.tx.Utxo
import java.math.BigDecimal

/**
 * @author akibabu
 */
@Entity(tableName = "slp_utxo", primaryKeys = ["tx_id", "index"])
internal data class SlpUtxo(
    @ColumnInfo(name = "token_id") val tokenId: SlpTokenId,
    @ColumnInfo(name = "num_tokens") val numTokensRaw: BigDecimal,
    @Embedded val utxo: Utxo)
