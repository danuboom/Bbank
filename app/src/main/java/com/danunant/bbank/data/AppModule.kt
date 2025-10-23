package com.danunant.bbank.data

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant.now
import java.util.UUID
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBbankDatabase(
        app: Application,
        // We use Provider to avoid a circular dependency
        daoProvider: Provider<BbankDao>
    ): BbankDatabase {
        return Room.databaseBuilder(app, BbankDatabase::class.java, "bbank_db")
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Pre-populate the DB on first create
                    CoroutineScope(Dispatchers.IO).launch {
                        daoProvider.get().insertUsers(seedUsers())
                        daoProvider.get().insertAccounts(seedAccounts())
                        daoProvider.get().insertTxns(seedTxns())
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideBbankDao(db: BbankDatabase): BbankDao {
        return db.bbankDao()
    }

    // --- You DO NOT need a provider for BbankRepository ---
    // Hilt knows how to create it because of @Inject constructor


    // --- Seed Data Functions ---
    private fun seedUsers(): List<User> = listOf(
        User("U1", "alice", "Alice Smith", "1234"),
        User("U2", "bob", "Bob Johnson", "1234"),
        User("U3", "test", "Test User", "1234")
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
        Account("T1", "U3", "Test Account", AccountType.CHECKING, "000-0-00000-0", 1_000_000),
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