package com.danunant.bbank.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

enum class AccountType { CHECKING, SAVINGS, CREDIT }

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey val id: String,
    val username: String,
    val displayName: String,
    // --- UPDATED FIELDS ---
    val pinHash: String, // Store as Base64 String
    val salt: String     // Store as Base64 String
    // --- END UPDATE ---
)

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["ownerId"])]
)
data class Account(
    @PrimaryKey val id: String,
    val ownerId: String,
    val name: String,
    val type: AccountType,
    val number: String,
    val balanceSatang: Long,
)

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["fromAccountId"]),
        Index(value = ["toAccountId"])
    ]
)
data class Txn(
    @PrimaryKey val id: String,
    val fromAccountId: String?,
    val toAccountId: String?,
    val amountSatang: Long,
    val description: String,
    val at: Instant
)

sealed class TransferResult {
    data class Success(val txn: Txn) : TransferResult()
    data class Error(val code: String, val message: String) : TransferResult()
}