package com.danunant.bbank.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danunant.bbank.data.Account
import com.danunant.bbank.data.AccountType
import com.danunant.bbank.data.BbankRepository
import com.danunant.bbank.data.TransferResult
import com.danunant.bbank.data.Txn
import com.danunant.bbank.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BankViewModel @Inject constructor(
    private val repository: BbankRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUser
    val accounts: StateFlow<List<Account>> = repository.accounts
    val txns: StateFlow<List<Txn>> = repository.txns

    private val _transferResult = MutableStateFlow<TransferResult?>(null)
    val transferResult: StateFlow<TransferResult?> = _transferResult

    private val _isLogoutDialogVisible = MutableStateFlow(false)
    val isLogoutDialogVisible: StateFlow<Boolean> = _isLogoutDialogVisible

    private val _accountToDelete = MutableStateFlow<Account?>(null)
    val accountToDelete: StateFlow<Account?> = _accountToDelete


    fun showLogoutDialog() {
        _isLogoutDialogVisible.value = true
    }

    fun dismissLogoutDialog() {
        _isLogoutDialogVisible.value = false
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            dismissLogoutDialog()
        }
    }

    fun onTransfer(fromId: String, toAccountNumber: String, amountSatang: Long, desc: String) {
        viewModelScope.launch {
            if (currentUser.value == null) return@launch
            val token = UUID.randomUUID().toString()
            _transferResult.value = repository.transfer(token, fromId, toAccountNumber, amountSatang, desc)
        }
    }

    fun clearTransferResult() {
        _transferResult.value = null
    }


    fun onAccountDeleteRequest(account: Account) {
        _accountToDelete.value = account
    }

    fun onAccountDeleteConfirm() {
        _accountToDelete.value?.let { account ->
            viewModelScope.launch {
                repository.deleteAccount(account.id)
                _accountToDelete.value = null
            }
        }
    }

    fun onAccountDeleteDismiss() {
        _accountToDelete.value = null
    }

    suspend fun getAccountById(id: String): Account? {
        return repository.getAccountById(id)
    }

    fun saveAccount(id: String?, name: String, type: AccountType) {
        viewModelScope.launch {
            val ownerId = currentUser.value?.id ?: return@launch
            val accountToSave = if (id == null) {
                Account(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    name = name,
                    type = type,
                    number = "",
                    balanceSatang = 0
                )
            } else {
                val existing = repository.getAccountById(id) ?: return@launch
                existing.copy(name = name, type = type)
            }
            repository.saveAccount(accountToSave)
        }
    }

}