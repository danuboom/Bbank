package com.danunant.bbank.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danunant.bbank.core.formatTHB
import com.danunant.bbank.core.formatThaiDateTime
import com.danunant.bbank.data.Txn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(txns: List<Txn>, onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Transactions") }) }) { padding ->
        LazyColumn(Modifier.padding(padding)) {
            items(txns) { t ->
                ListItem(
                    headlineContent = { Text(t.description) },
                    supportingContent = { Text(formatThaiDateTime(t.at)) },
                    trailingContent = { Text(formatTHB(t.amountSatang)) }
                )
                Divider(Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}