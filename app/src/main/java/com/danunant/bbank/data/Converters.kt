package com.danunant.bbank.data

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun fromAccountType(value: AccountType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAccountType(value: String?): AccountType? {
        return value?.let { AccountType.valueOf(it) }
    }
}