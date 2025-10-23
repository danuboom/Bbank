package com.danunant.bbank.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.danunant.bbank.vm.AuthViewModel
import android.widget.Toast // Added
import androidx.compose.ui.platform.LocalContext // Added

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel,
    onRegistrationSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    val maxPinLength = 4

    val error by viewModel.registrationError.collectAsState()
    val success by viewModel.registrationSuccess.collectAsState()
    val context = LocalContext.current
    // Navigate back automatically on success
    LaunchedEffect(success) {
        if (success) {
            // --- SHOW TOAST ---
            Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
            // --- END TOAST ---
            onRegistrationSuccess()
            viewModel.resetRegistrationSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Register") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!error.isNullOrBlank()) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; viewModel.clearError() },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
            )

            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it; viewModel.clearError() },
                label = { Text("Display Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = pin,
                onValueChange = {
                    if (it.length <= maxPinLength && it.all { c -> c.isDigit() }) { // Allow only digits
                        pin = it; viewModel.clearError()
                    }
                },
                // --- UPDATE LABEL ---
                label = { Text("Choose a 4-Digit PIN") },
                // --- END UPDATE ---
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = confirmPin,
                onValueChange = {
                    if (it.length <= maxPinLength && it.all { c -> c.isDigit() }) { // Allow only digits
                        confirmPin = it; viewModel.clearError()
                    }
                },
                label = { Text("Confirm PIN") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.register(username.trim(), displayName.trim(), pin, confirmPin) },
                enabled = username.isNotBlank() && displayName.isNotBlank() && pin.length == maxPinLength && confirmPin.length == maxPinLength,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Register", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}