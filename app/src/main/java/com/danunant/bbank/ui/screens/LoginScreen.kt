package com.danunant.bbank.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextButton
import com.danunant.bbank.R

@Composable
fun LoginScreen(
    onLogin: (String, String) -> Unit,
    error: String?,
    onClearError: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val maxPinLength = 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = "Bank Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 48.dp)
        )

        if (!error.isNullOrBlank()) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                onClearError() // Clear error on type
            },
            label = { Text("Username or Account ID") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Username Icon") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = {
                if (it.length <= maxPinLength) {
                    pin = it
                    onClearError() // Clear error on type
                }
            },
            label = { Text("PIN") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "PIN Icon") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
        )

        Text(
            text = "Max $maxPinLength digits",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 24.dp)
        )

        Button(
            onClick = { onLogin(username.trim(), pin.trim()) },
            enabled = username.isNotBlank() && pin.length == maxPinLength,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Login", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(16.dp)) // Spacing

        // --- ADDED REGISTER BUTTON ---
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}