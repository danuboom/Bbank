package com.danunant.bbank.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.danunant.bbank.data.Account
import com.danunant.bbank.data.AccountType
import com.danunant.bbank.data.TransferResult
import com.danunant.bbank.vm.BankViewModel
import java.util.Locale
import com.danunant.bbank.core.formatAccountNumber
import com.danunant.bbank.core.unformatAccountNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: BankViewModel,
    onTransferSuccess: (txnId: String) -> Unit,
    onShowLogout: () -> Unit,
    onBack: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val transferResult by viewModel.transferResult.collectAsState()
    val lastError = (transferResult as? TransferResult.Error)?.message

    val fromAccounts = remember(accounts) {
        accounts.filter { it.type != AccountType.CREDIT }
    }

    var fromExpanded by remember { mutableStateOf(false) }
    var fromAccountId by remember { mutableStateOf<String?>(fromAccounts.firstOrNull()?.id) }
    var toAccountNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    val selectedFromAccount = fromAccounts.find { it.id == fromAccountId }

    val isAmountValid = (amount.toDoubleOrNull() ?: 0.0) > 0
    val plainToAccountNumber = unformatAccountNumber(toAccountNumber)
    val isToAccountValid = plainToAccountNumber.length == 10 && plainToAccountNumber.all { it.isDigit() }
    val isSameAccount = selectedFromAccount?.number == plainToAccountNumber

    val isFormValid = isAmountValid && isToAccountValid && !isSameAccount && fromAccountId != null

    LaunchedEffect(transferResult) {
        if (transferResult is TransferResult.Success) {
            val txnId = (transferResult as TransferResult.Success).txn.id
            onTransferSuccess(txnId)
            viewModel.clearTransferResult()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Transfer Funds") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShowLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (fromAccounts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No transferable accounts available.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!lastError.isNullOrBlank()) {
                ErrorCard(text = lastError)
            }
            if (isSameAccount) {
                ErrorCard(text = "Cannot transfer to the same account.")
            }

            AccountDropdown(
                label = "From Account",
                selectedAccount = selectedFromAccount,
                accountList = fromAccounts,
                isExpanded = fromExpanded,
                onExpandedChange = { fromExpanded = it },
                onSelect = { account ->
                    fromAccountId = account.id
                    fromExpanded = false
                    viewModel.clearTransferResult()
                }
            )

            OutlinedTextField(
                value = toAccountNumber,
                onValueChange = { newValue ->
                    val plainNumber = newValue.filter { it.isDigit() }
                    if (plainNumber.length <= 10) {
                        toAccountNumber = formatAccountNumber(plainNumber)
                        viewModel.clearTransferResult()
                    }
                },
                label = { Text("To Account Number (10 digits)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                isError = toAccountNumber.isNotBlank() && !isToAccountValid,
                supportingText = {
                    if (toAccountNumber.isNotBlank() && !isToAccountValid) {
                        Text("Must be 10 digits.", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { newValue ->
                    val filtered = newValue.filter { c -> c.isDigit() || c == '.' }
                    if (filtered.count { it == '.' } <= 1) {
                        amount = filtered
                        viewModel.clearTransferResult()
                    }
                },
                label = { Text("Amount (THB)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = amount.isNotBlank() && !isAmountValid
            )

            OutlinedTextField(
                value = desc,
                onValueChange = {
                    desc = it
                    viewModel.clearTransferResult()
                },
                label = { Text("Description (Optional)") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val amountInThb = amount.toDoubleOrNull() ?: 0.0
                    val satang = (amountInThb * 100).toLong()
                    val plainAccountNumberSubmit = unformatAccountNumber(toAccountNumber)
                    viewModel.onTransfer(fromAccountId!!, plainAccountNumberSubmit, satang, desc.trim())
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Confirm Transfer")
            }
        }
    }
}

@Composable
private fun ErrorCard(text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Text(
            text = text.uppercase(Locale.ROOT),
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp).fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDropdown(
    label: String,
    selectedAccount: Account?,
    accountList: List<Account>,
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (Account) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedAccount?.let { "${it.name} (${formatAccountNumber(it.number)})" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            textStyle = MaterialTheme.typography.titleMedium
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            accountList.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(account.name, style = MaterialTheme.typography.titleSmall)
                            Text(formatAccountNumber(account.number), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    onClick = { onSelect(account) }
                )
            }
        }
    }
}