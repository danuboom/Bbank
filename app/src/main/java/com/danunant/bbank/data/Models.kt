package com.danunant.bbank.data

import java.time.Instant

enum class AccountType { CHECKING, SAVINGS, CREDIT }

data class User(
    val id: String, // Added
    val username: String,
    val displayName: String
)

data class Account(
    val id: String,
    val ownerId: String, // Added: Links to User.id
    val name: String,
    val type: AccountType,
    val number: String,
    val balanceSatang: Long
)

data class Txn(
    val id: String,
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