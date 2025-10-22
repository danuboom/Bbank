package com.danunant.bbank.ui.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danunant.bbank.core.formatTHB
import com.danunant.bbank.data.Account


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    accounts: List<Account>,
    onViewTransactions: () -> Unit,
    onTransfer: () -> Unit,
    onAbout: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            accounts.forEach { acc ->
                Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(acc.name, style = MaterialTheme.typography.titleMedium)
                        Text(acc.number, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Text(formatTHB(acc.balanceSatang), style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onViewTransactions, modifier = Modifier.weight(1f)) { Text("Transactions") }
                Button(onClick = onTransfer, modifier = Modifier.weight(1f)) { Text("Transfer") }
            }
            Text(
                "About / Developer",
                modifier = Modifier.padding(top = 16.dp).clickable { onAbout() },
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}