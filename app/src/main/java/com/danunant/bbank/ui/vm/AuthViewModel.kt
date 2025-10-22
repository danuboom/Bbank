package com.danunant.bbank.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danunant.bbank.data.BbankRepository
import com.danunant.bbank.data.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: BbankRepository
) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUser

    // --- Add error state ---
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(username: String, pin: String) {
        viewModelScope.launch {
            val success = repository.login(username, pin)
            if (!success) {
                _error.value = "Invalid username or PIN"
            }
            // Success is handled by the NavHost observing currentUser
        }
    }

    // --- Add function to clear error ---
    fun clearError() {
        _error.value = null
    }
}