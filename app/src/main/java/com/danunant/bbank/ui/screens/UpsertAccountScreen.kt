package com.danunant.bbank.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.danunant.bbank.data.Account
import com.danunant.bbank.data.AccountType
import com.danunant.bbank.vm.BankViewModel
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.input.ImeAction
// --- ADD THIS IMPORT ---
import com.danunant.bbank.core.formatAccountNumber
// -----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpsertAccountScreen(
    viewModel: BankViewModel,
    accountId: String?, // Null if creating, non-null if editing
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var account by remember { mutableStateOf<Account?>(null) }

    var name by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") } // Store plain number
    var type by remember { mutableStateOf(AccountType.CHECKING) }
    var typeExpanded by remember { mutableStateOf(false) }

    var numberError by remember { mutableStateOf<String?>(null) }
    val isNumberValid = number.length == 10 && number.all { it.isDigit() }
    val isFormValid = name.isNotBlank() && isNumberValid

    val isEditMode = accountId != null
    val title = if (isEditMode) "Edit Account" else "Create Account"

    // Fetch the account if we are in edit mode
    LaunchedEffect(accountId) {
        if (isEditMode) {
            account = viewModel.getAccountById(accountId!!)
            account?.let {
                name = it.name
                number = it.number // Store plain number
                type = it.type
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Validation check
                    if (!isNumberValid) {
                        numberError = "Account number must be 10 digits."
                        return@FloatingActionButton
                    }
                    if (name.isBlank()){
                        // Optional: Add name validation error state if needed
                        return@FloatingActionButton
                    }

                    scope.launch {
                        viewModel.saveAccount(accountId, name, type, number) // Save plain number
                        onBack()
                    }
                }
                // Optional: Disable button visually
                // enabled = isFormValid
            ) {
                Icon(Icons.Default.Save, "Save Account")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                // Display formatted number, store plain number in state
                value = formatAccountNumber(number), // Use the imported function
                onValueChange = { newValue ->
                    val plainNumber = newValue.filter { it.isDigit() }
                    if (plainNumber.length <= 10) {
                        number = plainNumber // Store plain number
                        numberError = null // Clear error on type
                    }
                },
                label = { Text("Account Number (10 digits)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                readOnly = isEditMode,
                isError = numberError != null,
                supportingText = {
                    if (numberError != null) {
                        Text(numberError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // Account Type Dropdown
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    AccountType.values().forEach { acctType ->
                        // Don't allow changing to/from Credit Card
                        if (acctType != AccountType.CREDIT) {
                            DropdownMenuItem(
                                text = { Text(acctType.name) },
                                onClick = {
                                    type = acctType
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}