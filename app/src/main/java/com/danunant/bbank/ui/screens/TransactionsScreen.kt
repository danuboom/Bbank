package com.danunant.bbank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danunant.bbank.core.formatTHB
import com.danunant.bbank.core.formatThaiDateTime
import com.danunant.bbank.data.Txn
import com.danunant.bbank.vm.BankViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: BankViewModel,
    onShowLogout: () -> Unit, // Added
    onBack: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    // Collect state from ViewModel
    val txns by viewModel.txns.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    // Create a set of the user's account IDs to check against
    val userAccountIds by remember(accounts) {
        derivedStateOf { accounts.map { it.id }.toSet() }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Transactions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                // --- ADDED LOGOUT BUTTON ---
                actions = {
                    IconButton(onClick = onShowLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        if (txns.isEmpty()) {
            EmptyState(padding)
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(0.dp), // ListItem handles its own padding
        ) {
            items(txns) { txn ->
                TransactionItem(
                    txn = txn,
                    userAccountIds = userAccountIds // Pass the set
                )
                Divider(Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun TransactionItem(
    txn: Txn,
    userAccountIds: Set<String> // Added
) {
    // --- CORRECTED LOGIC ---
    val isDebit = txn.fromAccountId in userAccountIds
    val isCredit = txn.toAccountId in userAccountIds

    // Determine icon, color, and prefix based on transfer type
    val (icon, color, prefix) = when {
        // User's account -> External (Debit/Withdrawal)
        isDebit && !isCredit -> Triple(
            Icons.Filled.ArrowUpward,
            MaterialTheme.colorScheme.error,
            "-"
        )
        // External -> User's account (Credit/Deposit)
        !isDebit && isCredit -> Triple(
            Icons.Filled.ArrowDownward,
            Color(0xFF2E7D32), // Dark Green
            "+"
        )
        // User's account -> User's account (Internal Transfer)
        else -> Triple(
            Icons.AutoMirrored.Filled.ArrowForward,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "" // Neutral
        )
    }

    val description = when {
        isDebit && !isCredit -> "To: ${txn.toOwnerName ?: "Unknown"}"
        !isDebit && isCredit -> "From: ${txn.fromOwnerName ?: "Unknown"}"
        else -> "Transfer"
    }

    // --- END LOGIC ---

    // Use ListItem for better semantics and standard Material layout
    ListItem(
        headlineContent = {
            Text(
                text = description,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = {
            Text(
                text = formatThaiDateTime(txn.at),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
            )
        },
        trailingContent = {
            Text(
                text = "$prefix${formatTHB(txn.amountSatang)}",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    )
}

@Composable
private fun EmptyState(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("No Transactions Found", style = MaterialTheme.typography.titleLarge)
        Text(
            "Your account history is currently empty.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
