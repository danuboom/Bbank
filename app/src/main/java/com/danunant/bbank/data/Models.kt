package com.danunant.bbank.data

import java.time.Instant

enum class AccountType { CHECKING, SAVINGS, CREDIT }


data class Account(
    val id: String,
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


data class User(val username: String, val displayName: String)


sealed class TransferResult {
    data class Success(val txn: Txn) : TransferResult()
    data class Error(val code: String, val message: String) : TransferResult()
}