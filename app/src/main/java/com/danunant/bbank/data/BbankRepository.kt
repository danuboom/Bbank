package com.danunant.bbank.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant.now
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BbankRepository @Inject constructor(
    private val dao: BbankDao
) {
    // Scope for repository-level coroutines
    private val scope = CoroutineScope(Dispatchers.IO)

    // --- User-Facing State ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // These flows are now powered by the DB and react to the user logging in
    val accounts: StateFlow<List<Account>> = _currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList()) // No user, empty list
        } else {
            dao.getAccounts(user.id) // Get accounts flow from DB
        }
    }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    val txns: StateFlow<List<Txn>> = accounts.flatMapLatest { accts ->
        if (accts.isEmpty()) {
            flowOf(emptyList()) // No accounts, empty list
        } else {
            val accountIds = accts.map { it.id }.toSet()
            dao.getTransactions(accountIds) // Get txns flow from DB
        }
    }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())


    // simple duplicate prevention
    private var lastTransferToken: String? = null


    // --- THIS FUNCTION IS NOW SUSPEND AND CORRECT ---
    suspend fun login(username: String, pin: String): Boolean {
        if (username.isBlank() || pin.length != 4) { // Basic check
            _currentUser.value = null
            return false
        }

        val user = dao.getUserByUsername(username)

        // Check against stored PIN
        return if (user != null && user.pin == pin) {
            _currentUser.value = user
            true
        } else {
            _currentUser.value = null
            false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- ADDED: Registration Function ---
    suspend fun registerUser(username: String, displayName: String, pin: String): Result<User> {
        // Basic validation
        if (username.isBlank() || displayName.isBlank() || pin.length != 4 || !pin.all { it.isDigit() }) { // Basic PIN check
            return Result.failure(IllegalArgumentException("Invalid input. PIN must be 4 digits."))
        }
        if (dao.doesUserExist(username)) {
            return Result.failure(IllegalArgumentException("Username already taken."))
        }

        // Create new user WITH PIN
        val newUser = User(
            id = UUID.randomUUID().toString(),
            username = username,
            displayName = displayName,
            pin = pin // Store the PIN (Hashing needed for real app)
        )

        return try {
            dao.insertUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- ADDED FOR CRUD ---
    suspend fun getAccountById(id: String): Account? {
        return dao.getAccountById(id)
    }

    suspend fun saveAccount(account: Account) {
        // This is an "Upsert" (Update/Insert)
        if (dao.getAccountById(account.id) == null) {
            dao.insertAccount(account)
        } else {
            dao.updateAccount(account)
        }
    }

    suspend fun deleteAccount(accountId: String) {
        // You might add logic here to check if balance is 0
        dao.deleteAccountById(accountId)
    }
    // --- END ADD ---

    // --- UPDATED to use DAO ---
    suspend fun transfer(token: String, fromId: String, toAccountNumber: String, amountSatang: Long, desc: String): TransferResult {
        if (token.isBlank()) return TransferResult.Error("bad_token", "Invalid request token")
        if (lastTransferToken == token) return TransferResult.Error("duplicate", "This transfer was already processed.")
        if (amountSatang <= 0) return TransferResult.Error("amount", "Amount must be greater than 0")

        val from = dao.getAccountById(fromId)
        val to = dao.getAccountByNumber(toAccountNumber)

        if (from == null) return TransferResult.Error("auth", "Your account was not found.")
        if (to == null) return TransferResult.Error("not_found", "Recipient account number not found.")
        if (from.id == to.id) return TransferResult.Error("same_account", "Choose a different recipient.")
        if (from.ownerId != _currentUser.value?.id) return TransferResult.Error("auth", "Permission denied")
        if (from.balanceSatang < amountSatang) return TransferResult.Error("insufficient", "Insufficient funds")

        // Apply changes
        val updatedFrom = from.copy(balanceSatang = from.balanceSatang - amountSatang)
        val updatedTo = to.copy(balanceSatang = to.balanceSatang + amountSatang)
        val txn = Txn(
            id = UUID.randomUUID().toString(),
            fromAccountId = from.id,
            toAccountId = to.id,
            amountSatang = amountSatang,
            description = desc.ifBlank { "Transfer" },
            at = now()
        )

        // Use the DAO's @Transaction
        dao.performTransfer(updatedFrom, updatedTo, txn)

        lastTransferToken = token
        return TransferResult.Success(txn)
    }
}