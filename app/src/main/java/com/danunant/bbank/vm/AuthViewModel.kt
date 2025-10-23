package com.danunant.bbank.vm

import android.util.Log
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

    // Define a tag for logging
    private val TAG = "AuthViewModel"

    val currentUser: StateFlow<User?> = repository.currentUser

    // --- Login State ---
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // --- Registration State ---
    private val _registrationError = MutableStateFlow<String?>(null)
    val registrationError: StateFlow<String?> = _registrationError
    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess

    fun login(username: String, pin: String) {
        Log.d(TAG, "login called with U: $username")
        viewModelScope.launch {
            try {
                Log.d(TAG, "Calling repository.login...")
                val success = repository.login(username, pin)
                Log.d(TAG, "repository.login returned: $success")
                if (!success) {
                    Log.d(TAG, "Setting login error state")
                    _error.value = "Invalid username or PIN"
                } else {
                    Log.d(TAG, "Clearing login error state")
                    _error.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login: ${e.message}", e)
                _error.value = "An error occurred during login."
            }
        }
    }

    fun clearError() {
        Log.d(TAG, "clearError called") // Log entry
        _error.value = null
        _registrationError.value = null
    }

    fun register(username: String, displayName: String, pin: String, confirmPin: String) {
        // --- Log entry ---
        Log.d(TAG, "register called with U: $username, DN: $displayName")
        // ---
        viewModelScope.launch { // Correctly uses viewModelScope
            clearError() // This will log itself
            _registrationSuccess.value = false

            if (pin != confirmPin) {
                Log.d(TAG, "Registration failed: PINs do not match") // Log validation failure
                _registrationError.value = "PINs do not match."
                return@launch
            }
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                Log.d(TAG, "Registration failed: Invalid PIN format") // Log validation failure
                _registrationError.value = "PIN must be 4 digits."
                return@launch
            }

            // --- Log before repo call ---
            Log.d(TAG, "Calling repository.registerUser...")
            // ---
            // Call the suspend function from repository inside launch
            val result = repository.registerUser(username, displayName, pin)
            // --- Log result ---
            Log.d(TAG, "repository.registerUser returned: ${result.isSuccess}")
            // ---

            result.onSuccess {
                Log.d(TAG, "Registration SUCCESS - Setting success state") // Log state change
                _registrationSuccess.value = true
            }.onFailure { exception ->
                Log.d(TAG, "Registration FAILED - Setting error state: ${exception.message}") // Log state change
                _registrationError.value = exception.message ?: "Registration failed."
            }
        }
    }

    fun resetRegistrationSuccess() {
        Log.d(TAG, "resetRegistrationSuccess called") // Log entry
        _registrationSuccess.value = false
    }
}