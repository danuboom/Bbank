package com.danunant.bbank.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BbankDao {

    // --- User ---
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun doesUserExist(username: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT) // Abort if username exists
    suspend fun insertUser(user: User)

    // --- Accounts ---
    @Query("SELECT * FROM accounts WHERE ownerId = :userId")
    fun getAccounts(userId: String): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): Account?

    @Query("SELECT * FROM accounts WHERE number = :number")
    suspend fun getAccountByNumber(number: String): Account?

    // --- Transactions ---
    @Query("SELECT * FROM transactions WHERE fromAccountId IN (:accountIds) OR toAccountId IN (:accountIds) ORDER BY at DESC")
    fun getTransactions(accountIds: Set<String>): Flow<List<Txn>>

    // --- Transfer (All or Nothing) ---
    @Transaction
    suspend fun performTransfer(from: Account, to: Account, txn: Txn) {
        updateAccount(from)
        updateAccount(to)
        insertTxn(txn)
    }

    // --- ADDED FOR CRUD ---
    @Insert
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query("DELETE FROM accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: String)
    // --- END ADD ---

    @Insert
    suspend fun insertTxn(txn: Txn)

    // --- Seeding ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccounts(accounts: List<Account>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTxns(txns: List<Txn>)
}