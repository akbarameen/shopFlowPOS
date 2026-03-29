package com.matechmatrix.shopflowpos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.feature.customers.presentation.CustomersScreen
import com.matechmatrix.shopflowpos.feature.dashboard.presentation.DashboardScreen
import com.matechmatrix.shopflowpos.feature.expenses.presentation.ExpensesScreen
import com.matechmatrix.shopflowpos.feature.installments.presentation.InstallmentsScreen
import com.matechmatrix.shopflowpos.feature.inventory.presentation.InventoryScreen
import com.matechmatrix.shopflowpos.feature.ledger.presentation.LedgerScreen
import com.matechmatrix.shopflowpos.feature.pos.presentation.PosScreen
import com.matechmatrix.shopflowpos.feature.repairs.presentation.RepairsScreen
import com.matechmatrix.shopflowpos.feature.reports.presentation.ReportsScreen
import com.matechmatrix.shopflowpos.feature.salesreturn.presentation.SalesReturnScreen
import com.matechmatrix.shopflowpos.feature.settings.presentation.SettingsScreen
import com.matechmatrix.shopflowpos.feature.suppliers.presentation.SuppliersScreen
import com.matechmatrix.shopflowpos.feature.transactions.presentation.TransactionsScreen
import com.matechmatrix.shopflows.feature.auth.presentation.AuthScreen

/**
 * The NavHost that declares all composable destinations.
 *
 * [navigateChild] is passed down to screens that need to push a detail/child
 * route onto the back stack (e.g. CustomerDetail, SaleDetail). The AppNavigation
 * parent provides this lambda — it calls navController.navigate(route) WITHOUT
 * clearing the back stack, so pressing Back returns to the parent screen.
 *
 * For cross-feature navigation from inside a screen, call:
 *   navigateChild(AppRoute.CustomerDetail.route + "/$customerId")
 */
@Composable
fun AppNavHost(
    navController : NavHostController,
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    modifier      : Modifier = Modifier
) {
    NavHost(
        navController    = navController,
        startDestination = AppRoute.Auth.route,
        modifier         = modifier
    ) {
        composable(AppRoute.Auth.route) {
            AuthScreen(
                onActivated = {
                    navController.navigate(AppRoute.Dashboard.route) {
                        popUpTo(AppRoute.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.Dashboard.route) {
            DashboardScreen(
                windowSize    = windowSize,
                navigateChild = navigateChild
            )
        }

        composable(AppRoute.POS.route) {
            PosScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Inventory.route) {
            InventoryScreen(
                windowSize    = windowSize,
                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Transactions.route) {
            TransactionsScreen(
                windowSize    = windowSize,
////                navigateChild = navigateChild
            )
        }

        composable(AppRoute.SalesReturn.route) {
            SalesReturnScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Customers.route) {
            CustomersScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Suppliers.route) {
            SuppliersScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Expenses.route) {
            ExpensesScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Ledger.route) {
            LedgerScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Reports.route) {
            ReportsScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Installments.route) {
            InstallmentsScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Repairs.route) {
            RepairsScreen(
                windowSize    = windowSize,
//                navigateChild = navigateChild
            )
        }

        composable(AppRoute.Settings.route) {
            SettingsScreen(windowSize = windowSize)
        }

        // ─── Future child/detail routes — add here ────────────────────────────
        // Example:
        // composable(AppRoute.CustomerDetail.route + "/{customerId}") { backStackEntry ->
        //     val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
        //     CustomerDetailScreen(customerId = customerId, windowSize = windowSize)
        // }
    }
}