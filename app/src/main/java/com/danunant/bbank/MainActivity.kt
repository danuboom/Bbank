package com.danunant.bbank

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.danunant.bbank.ui.navigation.AppNavHost
import com.danunant.bbank.ui.theme.BbankTheme // Your theme file
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // <-- Make sure this is here
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BbankTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost() // <-- This runs your app navigation
                }
            }
        }
    }
}