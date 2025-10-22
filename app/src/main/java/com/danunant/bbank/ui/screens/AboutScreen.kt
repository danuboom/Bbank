package com.danunant.bbank.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("About / Developer") }) }) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("Bbank (Demo)", style = MaterialTheme.typography.titleLarge)
            Text("Author: Danunant Jaidee")
            Text("Locale: THB, Thai date/time")
            Text("Design: Material 3, Jetpack Compose")
        }
    }
}