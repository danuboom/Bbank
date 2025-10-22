package com.danunant.bbank.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.Instant.now
import java.util.UUID

class BbankRepository {

    // --- Master Data (Simulates a database) ---
    private val allUsers = seedUsers()
    private val allAccounts = MutableStateFlow(seedAccounts())
    private val allTxns = MutableStateFlow(seedTxns())


    // --- User-Facing State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // These flows now show data ONLY for the logged-in user
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    private val _txns = MutableStateFlow<List<Txn>>(emptyList())
    val txns: StateFlow<List<Txn>> = _txns


    // simple duplicate prevention
    private var lastTransferToken: String? = null


    fun login(username: String, pin: String): Boolean {
        // Find user in the master list
        val user = allUsers.find { it.username.equals(username, ignoreCase = true) && pin == "1234" }
        _currentUser.value = user

        if (user != null) {
            // Load this user's data
            loadUserdata(user.id)
        } else {
            // Clear data on failed login
            logout()
        }
        return user != null
    }

    fun logout() {
        _currentUser.value = null
        _accounts.value = emptyList()
        _txns.value = emptyList()
    }

    private fun loadUserdata(userId: String) {
        // Filter master list for this user's accounts
        val userAccounts = allAccounts.value.filter { it.ownerId == userId }
        _accounts.value = userAccounts

        // Filter master list for this user's transactions
        val userAccountIds = userAccounts.map { it.id }.toSet()
        _txns.value = allTxns.value.filter {
            userAccountIds.contains(it.fromAccountId) || userAccountIds.contains(it.toAccountId)
        }.sortedByDescending { it.at }
    }

    // --- UPDATED FUNCTION ---
    fun transfer(token: String, fromId: String, toAccountNumber: String, amountSatang: Long, desc: String): TransferResult {
        if (token.isBlank()) return TransferResult.Error("bad_token", "Invalid request token")
        if (lastTransferToken == token) return TransferResult.Error("duplicate", "This transfer was already processed.")
        if (amountSatang <= 0) return TransferResult.Error("amount", "Amount must be greater than 0")

        // Find accounts in the MASTER list
        val from = allAccounts.value.find { it.id == fromId }
        // --- THIS IS THE CHANGE ---
        val to = allAccounts.value.find { it.number == toAccountNumber }

        if (from == null) return TransferResult.Error("auth", "Your account was not found.")
        if (to == null) return TransferResult.Error("not_found", "Recipient account number not found.")

        // Check for same account
        if (from.id == to.id) return TransferResult.Error("same_account", "Choose a different recipient.")

        val currentUserId = _currentUser.value?.id ?: return TransferResult.Error("auth", "Not logged in")
        if (from.ownerId != currentUserId) return TransferResult.Error("auth", "Permission denied")
        if (from.balanceSatang < amountSatang) return TransferResult.Error("insufficient", "Insufficient funds")

        // Apply changes to the MASTER list
        allAccounts.update { list ->
            list.map {
                when (it.id) {
                    from.id -> it.copy(balanceSatang = it.balanceSatang - amountSatang)
                    to.id -> it.copy(balanceSatang = it.balanceSatang + amountSatang) // Use to.id
                    else -> it
                }
            }
        }
        val txn = Txn(
            id = UUID.randomUUID().toString(),
            fromAccountId = from.id, // Use from.id
            toAccountId = to.id,     // Use to.id
            amountSatang = amountSatang,
            description = desc.ifBlank { "Transfer" },
            at = now()
        )
        allTxns.update { it + txn }
        lastTransferToken = token
        loadUserdata(currentUserId)
        return TransferResult.Success(txn)
    }


    // --- Seed Data (All users) ---

    private fun seedUsers(): List<User> = listOf(
        User("U1", "alice", "Alice Smith"),
        User("U2", "bob", "Bob Johnson"),
        User("U3", "test", "Test User")
    )

    private fun seedAccounts(): List<Account> = listOf(
        // Alice's Accounts
        Account("A1", "U1", "Main Checking", AccountType.CHECKING, "123-4-56789-0", 25_000_00),
        Account("A2", "U1", "Savings", AccountType.SAVINGS, "987-6-54321-0", 75_500_00),
        Account("A3", "U1", "Credit Card", AccountType.CREDIT, "5555-88XX-XXXX-1234", -5_200_00),

        // Bob's Accounts
        Account("B1", "U2", "Primary", AccountType.CHECKING, "111-2-33333-4", 10_000_00),
        Account("B2", "U2", "Vacation Fund", AccountType.SAVINGS, "222-3-44444-5", 1_500_00),

        // Test User's Account
        Account("T1", "U3", "Test Account", AccountType.CHECKING, "000-0-00000-0", 1_00),
    )

    private fun seedTxns(): List<Txn> = listOf(
        // Alice's Txns
        Txn(UUID.randomUUID().toString(), null, "A1", 5_000_00, "Initial deposit", now().minusSeconds(86400 * 3)),
        Txn(UUID.randomUUID().toString(), "A1", "A2", 1_200_00, "Save for rain", now().minusSeconds(86400 * 2)),
        Txn(UUID.randomUUID().toString(), "A1", null, 300_00, "Coffee shop", now().minusSeconds(86400 * 1)),

        // Bob's Txns
        Txn(UUID.randomUUID().toString(), null, "B1", 10_000_00, "Paycheck", now().minusSeconds(86400 * 2)),
        Txn(UUID.randomUUID().toString(), "B1", "B2", 500_00, "Move to vacation", now().minusSeconds(86400 * 1)),
    )
}