package com.danunant.bbank.ui.vm

import androidx.lifecycle.ViewModel
import com.danunant.bbank.data.BbankRepository
import kotlinx.coroutines.flow.StateFlow


class AuthViewModel(private val repo: BbankRepository) : ViewModel() {
    val user: StateFlow<com.danunant.bbank.data.User?> = repo.currentUser
    fun login(username: String, pin: String) = repo.login(username, pin)
    fun logout() = repo.logout()
}