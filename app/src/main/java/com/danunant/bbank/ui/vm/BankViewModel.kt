package com.danunant.bbank.ui.vm


import androidx.lifecycle.ViewModel
import com.danunant.bbank.data.BbankRepository
import com.danunant.bbank.data.TransferResult
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID


class BankViewModel(private val repo: BbankRepository) : ViewModel() {
    val accounts = repo.accounts
    val txns = repo.txns


    fun transfer(fromId: String, toId: String, amountSatang: Long, desc: String): TransferResult {
        val token = UUID.randomUUID().toString()
        return repo.transfer(token, fromId, toId, amountSatang, desc)
    }
}