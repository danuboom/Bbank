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
import com.danunant.bbank.core.CryptoUtils

@Singleton
class BbankRepository @Inject constructor(
    private val dao: BbankDao
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    val accounts: StateFlow<List<Account>> = _currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            dao.getAccounts(user.id)
        }
    }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())

    val txns: StateFlow<List<Txn>> = accounts.flatMapLatest { accts ->
        if (accts.isEmpty()) {
            flowOf(emptyList())
        } else {
            val accountIds = accts.map { it.id }.toSet()
            dao.getTransactions(accountIds)
        }
    }.stateIn(scope, kotlinx.coroutines.flow.SharingStarted.Lazily, emptyList())


    private var lastTransferToken: String? = null


    suspend fun registerUser(username: String, displayName: String, pin: String): Result<User> {
        if (username.isBlank() || displayName.isBlank() || pin.length != 4 || !pin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("Invalid input. PIN must be 4 digits."))
        }
        if (dao.doesUserExist(username)) {
            return Result.failure(IllegalArgumentException("Username already taken."))
        }

        val saltBytes = CryptoUtils.generateSalt()
        val hashBytes = CryptoUtils.hashPin(pin.toCharArray(), saltBytes)

        val saltString = CryptoUtils.encodeToBase64(saltBytes)
        val hashString = CryptoUtils.encodeToBase64(hashBytes)

        val newUser = User(
            id = UUID.randomUUID().toString(),
            username = username,
            displayName = displayName,
            pinHash = hashString,
            salt = saltString
        )

        return try {
            dao.insertUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, pin: String): Boolean {
        if (username.isBlank() || pin.length != 4) {
            _currentUser.value = null
            return false
        }

        val user = dao.getUserByUsername(username)

        return if (user != null) {
            val saltBytes = CryptoUtils.decodeFromBase64(user.salt)
            val storedHashBytes = CryptoUtils.decodeFromBase64(user.pinHash)

            if (CryptoUtils.verifyPin(pin.toCharArray(), storedHashBytes, saltBytes)) {
                _currentUser.value = user
                true
            } else {
                _currentUser.value = null
                false
            }
        } else {
            _currentUser.value = null
            false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    suspend fun getAccountById(id: String): Account? {
        return dao.getAccountById(id)
    }

    private suspend fun generateUniqueAccountNumber(): String {
        var accountNumber: String
        do {
            accountNumber = (1..10).map { (0..9).random() }.joinToString("")
        } while (dao.getAccountByNumber(accountNumber) != null)
        return accountNumber
    }

    suspend fun saveAccount(account: Account) {
        val accountToSave = if (account.number.isBlank()) {
            account.copy(number = generateUniqueAccountNumber())
        } else {
            account
        }

        if (dao.getAccountById(accountToSave.id) == null) {
            dao.insertAccount(accountToSave)
        } else {
            dao.updateAccount(accountToSave)
        }
    }

    suspend fun deleteAccount(accountId: String) {
        dao.deleteAccountById(accountId)
    }

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

        val fromOwner = dao.getUserById(from.ownerId)
        val toOwner = dao.getUserById(to.ownerId)

        val updatedFrom = from.copy(balanceSatang = from.balanceSatang - amountSatang)
        val updatedTo = to.copy(balanceSatang = to.balanceSatang + amountSatang)
        val txn = Txn(
            id = UUID.randomUUID().toString(),
            fromAccountId = from.id,
            toAccountId = to.id,
            amountSatang = amountSatang,
            description = desc.ifBlank { "Transfer" },
            at = now(),
            fromOwnerName = fromOwner?.displayName,
            toOwnerName = toOwner?.displayName,
        )

        dao.performTransfer(updatedFrom, updatedTo, txn)

        lastTransferToken = token
        return TransferResult.Success(txn)
    }
}