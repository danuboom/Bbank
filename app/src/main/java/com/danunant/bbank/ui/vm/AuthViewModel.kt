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

    // --- Login State ---
    private val _error = MutableStateFlow<String?>(null) // Correct definition
    val error: StateFlow<String?> = _error

    // --- Registration State ---
    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    suspend fun login(username: String, pin: String) {
        val success = repository.login(username, pin)
        if (!success) {
            _error.value = "Invalid username or PIN" // FIX: Use _error
        } else {
            _error.value = null // FIX: Use _error
        }
    }

    fun clearError() {
        _error.value = null // FIX: Use _error
        _registrationError.value = null
    }

    fun register(username: String, displayName: String, pin: String, confirmPin: String) {
        viewModelScope.launch {
            clearError()
            _registrationSuccess.value = false

            if (pin != confirmPin) {
                _registrationError.value = "PINs do not match."
                return@launch
            }
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                _registrationError.value = "PIN must be 4 digits."
                return@launch
            }

            val result = repository.registerUser(username, displayName, pin)

            result.onSuccess {
                _registrationSuccess.value = true
            }.onFailure { exception ->
                _registrationError.value = exception.message ?: "Registration failed."
            }
        }
    }

    fun resetRegistrationSuccess() {
        _registrationSuccess.value = false
    }
}