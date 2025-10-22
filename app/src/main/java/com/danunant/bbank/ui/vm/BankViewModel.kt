package com.danunant.bbank.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danunant.bbank.data.BbankRepository
import com.danunant.bbank.data.Account
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

    // --- ADDED FOR LOGOUT ---
    private val _isLogoutDialogVisible = MutableStateFlow(false)
    val isLogoutDialogVisible: StateFlow<Boolean> = _isLogoutDialogVisible

    fun showLogoutDialog() {
        _isLogoutDialogVisible.value = true
    }

    fun dismissLogoutDialog() {
        _isLogoutDialogVisible.value = false
    }
    // ------------------------

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            dismissLogoutDialog() // Ensure dialog is hidden
        }
    }

    // --- UPDATED FUNCTION ---
    fun onTransfer(fromId: String, toAccountNumber: String, amountSatang: Long, desc: String) {
        viewModelScope.launch {
            if (currentUser.value == null) return@launch
            val token = UUID.randomUUID().toString()
            // Pass account number
            _transferResult.value = repository.transfer(token, fromId, toAccountNumber, amountSatang, desc)
        }
    }

    fun clearTransferResult() {
        _transferResult.value = null
    }
}