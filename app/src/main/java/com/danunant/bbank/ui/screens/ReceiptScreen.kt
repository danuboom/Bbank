package com.danunant.bbank.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danunant.bbank.core.formatTHB
import com.danunant.bbank.core.formatThaiDateTime
import com.danunant.bbank.data.Txn


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(txn: Txn?, onShare: (Txn) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Transfer Slip") }) }) { padding ->
        if (txn == null) {
            Column(Modifier.padding(padding).padding(24.dp)) { Text("Slip not found") }
            return@Scaffold
        }
        Column(Modifier.padding(padding).padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Bbank Receipt", style = MaterialTheme.typography.titleLarge)
            Text("Txn ID: ${'$'}{txn.id.take(8)}…")
            Text("Date: ${'$'}{formatThaiDateTime(txn.at)}")
            Text("Amount: ${'$'}{formatTHB(txn.amountSatang)}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { onShare(txn) }) { Text("Share Slip") }
        }
    }
}