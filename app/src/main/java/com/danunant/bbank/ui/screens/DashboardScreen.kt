package com.danunant.bbank.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.danunant.bbank.core.formatTHB
import com.danunant.bbank.data.Account
import com.danunant.bbank.vm.BankViewModel
import com.danunant.bbank.core.formatAccountNumber
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BankViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onLogout: () -> Unit,
    // --- ADDED FOR CRUD ---
    onNavigateToUpsert: (String?) -> Unit // Null for create, ID for edit
) {
    val accounts by viewModel.accounts.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    // --- ADDED: State for Delete Dialog ---
    val accountToDelete by viewModel.accountToDelete.collectAsState()
    if (accountToDelete != null) {
        DeleteAccountDialog(
            account = accountToDelete!!,
            onConfirm = viewModel::onAccountDeleteConfirm,
            onDismiss = viewModel::onAccountDeleteDismiss
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(user?.displayName ?: "Dashboard") },
                actions = {
                    IconButton(onClick = onNavigateToAbout) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About App"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        // --- ADDED FAB ---
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToUpsert(null) } // null ID means "Create"
            ) {
                Icon(Icons.Default.Add, "Create Account")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            DashboardActions(
                onViewTransactions = onNavigateToTransactions,
                onTransfer = onNavigateToTransfer
            )
            Spacer(Modifier.height(16.dp))

            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No accounts found. Tap '+' to add one.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts) { acc ->
                        AccountCard(
                            account = acc,
                            // --- ADDED ---
                            onEdit = { onNavigateToUpsert(acc.id) },
                            onDelete = { viewModel.onAccountDeleteRequest(acc) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = account.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            // --- USE FORMATTER ---
            Text(
                text = formatAccountNumber(account.number), // Use formatter
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = formatTHB(account.balanceSatang),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 12.dp)
            )

            // --- ADDED BUTTONS ---
            // Only show buttons if balance is 0 (safer for a demo)
            if (account.balanceSatang == 0L) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteAccountDialog(
    account: Account,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account") },
        text = { Text("Are you sure you want to delete '${account.name}'? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DashboardActions(
    onViewTransactions: () -> Unit,
    onTransfer: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onViewTransactions,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = "View Transactions")
            Spacer(Modifier.width(8.dp))
            Text("Transactions")
        }
        Button(
            onClick = onTransfer,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Send, contentDescription = "Transfer Funds")
            Spacer(Modifier.width(8.dp))
            Text("Transfer")
        }
    }
}