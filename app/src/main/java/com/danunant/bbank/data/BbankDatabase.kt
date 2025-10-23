package com.danunant.bbank.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [User::class, Account::class, Txn::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class BbankDatabase : RoomDatabase() {
    abstract fun bbankDao(): BbankDao
}