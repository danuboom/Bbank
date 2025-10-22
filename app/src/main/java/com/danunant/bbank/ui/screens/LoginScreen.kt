package com.danunant.bbank.ui.screens


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, error: String?) {
    var username by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }


    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bbank", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("PIN (1234)") },
            visualTransformation = PasswordVisualTransformation())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onLogin(username.trim(), pin.trim()) }, enabled = username.isNotBlank() && pin.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }
        if (!error.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}