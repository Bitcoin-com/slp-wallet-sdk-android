package com.bitcoin.slpwallet.slp

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/**
 * @author akibabu
 */
@Entity(tableName = "token_details")
data class SlpTokenDetails(
    @PrimaryKey @ColumnInfo(name = "token_id") val tokenId: SlpTokenId,
    @ColumnInfo(name = "ticker") val ticker: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "decimals") val decimals: Int) {

    /**
     * Example with 6 decimals: 12.53 -> 12530000
     */
    fun toRawAmount(amount: BigDecimal): ULong {
        if (amount > maxRawAmount) {
            throw IllegalArgumentException("amount larger than 8 unsigned bytes")
        } else if (amount.scale() > decimals) {
            throw IllegalArgumentException("$ticker supports maximum $decimals decimals but amount is $amount")
        }
        return amount.scaleByPowerOfTen(decimals).toLong().toULong()
    }

    /**
     * Example with 6 decimals: 12530000 -> 12.53
     */
    fun toReadableAmount(rawAmount: BigDecimal): BigDecimal {
        return rawAmount.scaleByPowerOfTen(-decimals).stripTrailingZeros()
    }

    companion object {
        val maxRawAmount = BigDecimal(ULong.MAX_VALUE.toString())
    }

}