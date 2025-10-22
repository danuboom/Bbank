package com.danunant.bbank.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.util.UUID

class BbankRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser


    private val _accounts = MutableStateFlow<List<Account>>(seedAccounts())
    val accounts: StateFlow<List<Account>> = _accounts


    private val _txns = MutableStateFlow<List<Txn>>(seedTxns())
    val txns: StateFlow<List<Txn>> = _txns


    // simple duplicate prevention
    private var lastTransferToken: String? = null


    fun login(username: String, pin: String): Boolean {
        val ok = username.isNotBlank() && pin == "1234"
        _currentUser.value = if (ok) User(username, "Danunant") else null
        return ok
    }


    fun logout() { _currentUser.value = null }


    fun transfer(token: String, fromId: String, toId: String, amountSatang: Long, desc: String): TransferResult {
        if (token.isBlank()) return TransferResult.Error("bad_token", "Invalid request token")
        if (lastTransferToken == token) return TransferResult.Error("duplicate", "This transfer was already processed.")
        if (fromId == toId) return TransferResult.Error("same_account", "Choose a different recipient.")
        if (amountSatang <= 0) return TransferResult.Error("amount", "Amount must be greater than 0")


        val from = _accounts.value.find { it.id == fromId }
        val to = _accounts.value.find { it.id == toId }
        if (from == null || to == null) return TransferResult.Error("not_found", "Account not found")
        if (from.balanceSatang < amountSatang) return TransferResult.Error("insufficient", "Insufficient funds")


// apply
        _accounts.update { list ->
            list.map {
                when (it.id) {
                    fromId -> it.copy(balanceSatang = it.balanceSatang - amountSatang)
                    toId -> it.copy(balanceSatang = it.balanceSatang + amountSatang)
                    else -> it
                }
            }
        }
        val txn = Txn(
            id = UUID.randomUUID().toString(),
            fromAccountId = fromId,
            toAccountId = toId,
            amountSatang = amountSatang,
            description = desc.ifBlank { "Transfer" },
            at = Instant.now()
        )
        _txns.update { it + txn }
        lastTransferToken = token
        return TransferResult.Success(txn)
    }


    private fun seedAccounts(): List<Account> = listOf(
        Account("A1", "Main Checking", AccountType.CHECKING, "123-4-56789-0", 25_000_00),
        Account("A2", "Savings", AccountType.SAVINGS, "987-6-54321-0", 75_500_00),
        Account("A3", "Credit Card", AccountType.CREDIT, "5555-88XX-XXXX-1234", -5_200_00),
    )


    private fun seedTxns(): List<Txn> = listOf(
        Txn(UUID.randomUUID().toString(), null, "A1", 5_000_00, "Initial deposit", Instant.now().minusSeconds(86400*3)),
        Txn(UUID.randomUUID().toString(), "A1", null, 1_200_00, "Groceries", Instant.now().minusSeconds(86400*2)),
    )
}