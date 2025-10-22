package com.danunant.bbank.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danunant.bbank.data.Account


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    accounts: List<Account>,
    onSubmit: (fromId: String, toId: String, amountSatang: Long, desc: String) -> Unit,
    lastError: String?,
) {
    var fromIdx by remember { mutableStateOf(0) }
    var toIdx by remember { mutableStateOf(1.coerceAtMost(accounts.lastIndex)) }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }


    Scaffold(topBar = { TopAppBar(title = { Text("Transfer") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("From account")
            ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                OutlinedTextField(value = accounts[fromIdx].name, onValueChange = {}, readOnly = true)
            }
            Text("To account")
            ExposedDropdownMenuBox(expanded = false, onExpandedChange = {}) {
                OutlinedTextField(value = accounts[toIdx].name, onValueChange = {}, readOnly = true)
            }
            OutlinedTextField(value = amount, onValueChange = { amount = it.filter { c -> c.isDigit() } }, label = { Text("Amount (THB)") })
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") })
            Button(onClick = {
                val satang = (amount.toLongOrNull() ?: 0L) * 100
                onSubmit(accounts[fromIdx].id, accounts[toIdx].id, satang, desc)
            }, enabled = amount.isNotBlank()) { Text("Confirm") }
            if (!lastError.isNullOrBlank()) Text(lastError, color = MaterialTheme.colorScheme.error)
        }
    }
}