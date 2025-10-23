package com.danunant.bbank.ui.navigation

import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.danunant.bbank.core.formatTHB
import com.danunant.bbank.core.formatThaiDateTime
import com.danunant.bbank.ui.screens.AboutScreen
import com.danunant.bbank.ui.screens.DashboardScreen
import com.danunant.bbank.ui.screens.LoginScreen
import com.danunant.bbank.ui.screens.ReceiptScreen
import com.danunant.bbank.ui.screens.TransactionsScreen
import com.danunant.bbank.ui.screens.TransferScreen
import com.danunant.bbank.ui.screens.UpsertAccountScreen
import com.danunant.bbank.ui.screens.RegistrationScreen
import com.danunant.bbank.vm.AuthViewModel
import com.danunant.bbank.vm.BankViewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// Navigation graphs help organize screens
object Graph {
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    // --- Get ViewModels at the top level ---
    val authViewModel: AuthViewModel = hiltViewModel()
    val bankViewModel: BankViewModel = hiltViewModel()

    val currentUser by authViewModel.currentUser.collectAsState()
    val isLogoutDialogVisible by bankViewModel.isLogoutDialogVisible.collectAsState()

    // --- Central navigation logic ---
    LaunchedEffect(currentUser) {
        val currentRoute = navController.currentBackStackEntry?.destination?.parent?.route
        if (currentUser == null && currentRoute != Graph.AUTH) {
            // User logged out, navigate to login
            navController.navigate(Graph.AUTH) {
                popUpTo(Graph.MAIN) { inclusive = true }
            }
        }
        if (currentUser != null && currentRoute != Graph.MAIN) {
            // User logged in, navigate to main app
            navController.navigate(Graph.MAIN) {
                popUpTo(Graph.AUTH) { inclusive = true }
            }
        }
    }

    // --- Show dialog at the top level ---
    if (isLogoutDialogVisible) {
        LogoutConfirmationDialog(
            onDismiss = bankViewModel::dismissLogoutDialog,
            onConfirm = {
                // Just call logout. The LaunchedEffect will handle navigation.
                bankViewModel.logout()
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Graph.AUTH
    ) {
        // --- Authentication Graph (Login) ---
        authGraph(navController, authViewModel)

        // --- Main App Graph (Dashboard, etc.) ---
        mainGraph(navController, bankViewModel)
    }
}

/**
 * Defines the authentication flow (login screen).
 */
private fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        route = Graph.AUTH,
        startDestination = Routes.Login
    ) {
        composable(Routes.Login) {
            // --- FIX: Get CoroutineScope ---
            val scope = rememberCoroutineScope()
            // --- END FIX ---

            val error by authViewModel.error.collectAsState()
            LoginScreen(
                onLogin = { username, pin ->
                    scope.launch {
                        authViewModel.login(username, pin)
                    }
                },
                error = error,
                onClearError = authViewModel::clearError,
                onNavigateToRegister = {
                    navController.navigate(Routes.Registration)
                }
            )
        }
        // --- ADDED REGISTRATION SCREEN ---
        composable(Routes.Registration) {
            RegistrationScreen(
                viewModel = authViewModel,
                onRegistrationSuccess = {
                    // Go back to login screen after success
                    navController.popBackStack()
                },
                onBack = {
                    // Go back to login screen if user cancels
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * Defines the main application flow (dashboard, transfers, etc.)
 */
private fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    bankViewModel: BankViewModel // Pass in the ViewModel
) {
    navigation(
        route = Graph.MAIN,
        startDestination = Routes.Dashboard
    ) {
        // --- Dashboard Screen ---
        composable(Routes.Dashboard) {
            // Get account to delete state for the dialog
            val accountToDelete by bankViewModel.accountToDelete.collectAsState()
            if (accountToDelete != null) {
                DeleteAccountDialog(
                    account = accountToDelete!!,
                    onConfirm = bankViewModel::onAccountDeleteConfirm,
                    onDismiss = bankViewModel::onAccountDeleteDismiss
                )
            }

            DashboardScreen(
                viewModel = bankViewModel,
                onNavigateToTransactions = { navController.navigate(Routes.Transactions) },
                onNavigateToTransfer = { navController.navigate(Routes.Transfer) },
                onNavigateToAbout = { navController.navigate(Routes.About) },
                onLogout = bankViewModel::showLogoutDialog,
                // --- ADDED ---
                onNavigateToUpsert = { accountId ->
                    val route = Routes.UpsertAccount.replace("{accountId}", accountId ?: "null")
                    navController.navigate(route)
                }
            )
        }

        // --- ADD THIS COMPOSABLE ---
        composable(
            route = Routes.UpsertAccount,
            arguments = listOf(navArgument("accountId") {
                type = NavType.StringType
                nullable = true
                defaultValue = "null" // Ensure "null" string is the default
            })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")

            UpsertAccountScreen(
                viewModel = bankViewModel,
                accountId = if (accountId == "null") null else accountId,
                onBack = { navController.popBackStack() }
            )
        }
        // --- END ADD ---

        // --- Transactions Screen ---
        composable(Routes.Transactions) {
            TransactionsScreen(
                viewModel = bankViewModel,
                onShowLogout = bankViewModel::showLogoutDialog,
                onBack = { navController.popBackStack() }
            )
        }

        // --- Transfer Screen ---
        composable(Routes.Transfer) {
            TransferScreen(
                viewModel = bankViewModel,
                onShowLogout = bankViewModel::showLogoutDialog,
                onBack = {
                    bankViewModel.clearTransferResult()
                    navController.popBackStack()
                },
                onTransferSuccess = { txnId ->
                    val route = Routes.Receipt.replace("{txnId}", txnId)
                    navController.navigate(route) {
                        popUpTo(Routes.Transfer) { inclusive = true }
                    }
                }
            )
        }

        // --- Receipt Screen (with argument) ---
        composable(
            route = Routes.Receipt,
            arguments = listOf(navArgument("txnId") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = LocalContext.current
            val txns by bankViewModel.txns.collectAsState()
            val txnId = backStackEntry.arguments?.getString("txnId")

            val txn by remember(txnId, txns) {
                derivedStateOf {
                    txns.find { it.id == txnId }
                }
            }

            ReceiptScreen(
                txn = txn,
                onShowLogout = bankViewModel::showLogoutDialog,
                onBack = { navController.popBackStack() },
                onShare = { transaction ->
                    val shareText = """
                        bBank Transfer Successful
                        Amount: ${formatTHB(transaction.amountSatang)}
                        Date: ${formatThaiDateTime(transaction.at)}
                        Ref: ${transaction.id.take(8)}
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(
                        Intent.createChooser(intent, "Share Slip")
                    )
                }
            )
        }

        // --- About Screen ---
        composable(Routes.About) {
            AboutScreen(
                onShowLogout = bankViewModel::showLogoutDialog,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

// --- Logout Dialog Composable ---
@Composable
private fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Out") },
        text = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log Out")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// --- ADDED: Delete Account Dialog ---
@Composable
private fun DeleteAccountDialog(
    account: com.danunant.bbank.data.Account, // Added full path to avoid import ambiguity if needed
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