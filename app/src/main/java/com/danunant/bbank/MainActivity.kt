package com.danunant.bbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danunant.bbank.data.BbankRepository
import com.danunant.bbank.data.TransferResult
import com.danunant.bbank.ui.navigation.Routes
import com.danunant.bbank.ui.screens.*
import com.danunant.bbank.ui.theme.BbankTheme
import com.danunant.bbank.ui.vm.AuthViewModel
import com.danunant.bbank.ui.vm.BankViewModel
import com.danunant.bbank.util.buildSlipBitmap
import com.danunant.bbank.util.shareBitmap
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repo = BbankRepository()
        setContent {
            val snackbar = remember { SnackbarHostState() }
            val nav = rememberNavController()
            val dark = isSystemInDarkTheme()

            BbankTheme(dark = dark) {
                Surface(Modifier.fillMaxSize()) {
                    val authVm: AuthViewModel = viewModel(factory = simpleFactory { AuthViewModel(repo) })
                    val bankVm: BankViewModel = viewModel(factory = simpleFactory { BankViewModel(repo) })

                    var loginError by remember { mutableStateOf<String?>(null) }
                    var transferError by remember { mutableStateOf<String?>(null) }

                    Scaffold(
                        snackbarHost = { SnackbarHost(hostState = snackbar) }
                    ) { inner ->
                        NavHost(
                            navController = nav,
                            startDestination = Routes.Login,
                            modifier = Modifier.padding(inner)
                        ) {
                            composable(Routes.Login) {
                                LoginScreen(
                                    onLogin = { u, p ->
                                        if (authVm.login(u, p)) {
                                            loginError = null
                                            nav.navigate(Routes.Dashboard) {
                                                popUpTo(Routes.Login) { inclusive = true }
                                            }
                                        } else loginError = "Invalid username or PIN"
                                    },
                                    error = loginError
                                )
                            }
                            composable(Routes.Dashboard) {
                                val accounts by bankVm.accounts.collectAsStateWithLifecycle()
                                DashboardScreen(
                                    accounts = accounts,
                                    onViewTransactions = { nav.navigate(Routes.Transactions) },
                                    onTransfer = { nav.navigate(Routes.Transfer) },
                                    onAbout = { nav.navigate(Routes.About) }
                                )
                            }
                            composable(Routes.Transactions) {
                                val txns by bankVm.txns.collectAsStateWithLifecycle()
                                TransactionsScreen(txns = txns, onBack = { nav.popBackStack() })
                            }
                            composable(Routes.Transfer) {
                                val accounts by bankVm.accounts.collectAsState() // or collectAsStateWithLifecycle()
                                TransferScreen(
                                    accounts = accounts,
                                    lastError = transferError,
                                    onSubmit = { from, to, amt, desc ->
                                        when (val res = bankVm.transfer(from, to, amt, desc)) {
                                            is TransferResult.Error -> transferError = res.message
                                            is TransferResult.Success -> {
                                                transferError = null
                                                nav.navigate("receipt/${res.txn.id}") {
                                                    popUpTo(Routes.Dashboard) { inclusive = false }
                                                }
                                            }
                                        }
                                    }
                                )
                            }

                            composable(
                                route = Routes.Receipt,
                                arguments = listOf(navArgument("txnId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val txnId = backStackEntry.arguments?.getString("txnId")
                                val txns by bankVm.txns.collectAsStateWithLifecycle()
                                val current = txns.find { it.id == txnId }
                                ReceiptScreen(txn = current) { t ->
                                    val bmp = buildSlipBitmap(t)
                                    shareBitmap(this@MainActivity, bmp, "receipt_${t.id.take(8)}.png")
                                }
                            }
                            composable(Routes.About) { AboutScreen() }
                        }
                    }
                }
            }
        }
    }
}

inline fun <reified T : ViewModel> simpleFactory(crossinline builder: () -> T) =
    viewModelFactory { initializer { builder() } }