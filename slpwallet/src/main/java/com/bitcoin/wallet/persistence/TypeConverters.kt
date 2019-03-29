package com.bitcoin.wallet.persistence

import androidx.room.TypeConverter
import com.bitcoin.wallet.slp.SlpTokenId
import java.math.BigDecimal

class TypeConverters {

    @TypeConverter
    fun tokenIdReadConverter(value: SlpTokenId): String {
        return value.hex
    }

    @TypeConverter
    fun tokenIdWriteConverter(value: String): SlpTokenId {
        return SlpTokenId(value)
    }

    @TypeConverter
    fun bigDecimalFromString(value: String?): BigDecimal? {
        return if (value !== null) {
            BigDecimal(value)
        } else {
            null
        }
    }

    @TypeConverter
    fun stringFromBigDecimal(number: BigDecimal?): String? {
        return number?.toPlainString()
    }
}
