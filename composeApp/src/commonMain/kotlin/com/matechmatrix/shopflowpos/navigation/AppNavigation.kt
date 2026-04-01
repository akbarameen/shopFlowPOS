package com.matechmatrix.shopflowpos.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import kotlinx.coroutines.launch

// ─── Top-level destinations — these are the 13 main nav screens ───────────────
// Any route NOT in this set is a "child" detail screen that shows ← Back instead of ☰ Menu.
private val topLevelRoutes = setOf(
    AppRoute.Dashboard.route,
    AppRoute.POS.route,
    AppRoute.Inventory.route,
    AppRoute.Transactions.route,
    AppRoute.SalesReturn.route,
    AppRoute.Customers.route,
    AppRoute.Suppliers.route,
    AppRoute.Expenses.route,
    AppRoute.Ledger.route,
    AppRoute.Reports.route,
    AppRoute.Installments.route,
    AppRoute.Repairs.route,
    AppRoute.Dues.route,
    AppRoute.Purchase.route,
    AppRoute.Settings.route
)

@Composable
fun AppNavigation(
    navController: NavHostController,
    windowSize   : AppWindowSize
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route
    val isAuthScreen   = currentRoute == AppRoute.Auth.route

    // True when the current screen is a child detail screen (not a top-level tab).
    // TopBar shows ← Back instead of ☰ Menu in this case.
    // Also hides the BottomBar on phone.
    val canNavigateBack = navController.previousBackStackEntry != null
            && currentRoute != null
            && currentRoute !in topLevelRoutes

    // ── Top-level tab navigation ─────────────────────────────────────────────
    // Pops back to Dashboard so tapping tabs never creates a deep back stack.
    // saveState/restoreState preserve per-tab scroll position and UI state.
    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(AppRoute.Dashboard.route) {
                saveState = true
                inclusive = false   // Keep Dashboard at the bottom of the stack
            }
            launchSingleTop = true
            restoreState    = true
        }
    }

    // ── Child navigation (detail screens) ────────────────────────────────────
    // Just pushes the route — pressing Back returns to the parent.
    fun navigateChild(route: String) {
        navController.navigate(route)
    }

    // ── Auth screen — completely bare, no chrome ──────────────────────────────
    if (isAuthScreen) {
        AppNavHost(
            navController = navController,
            windowSize    = windowSize,
            navigateChild = ::navigateChild
        )
        return
    }

    when (windowSize) {

        // ═══════════════════════════════════════════════════════════════════════
        //  COMPACT — Phone
        //  Layout:  ModalNavigationDrawer wraps Scaffold(topBar + bottomBar)
        //
        //  Hamburger ☰ opens full sidebar drawer (all 13 screens reachable)
        //  BottomBar  → 5 quick tabs
        //  Child screen → ← back replaces ☰, BottomBar hidden
        // ═══════════════════════════════════════════════════════════════════════
        AppWindowSize.COMPACT -> {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope       = rememberCoroutineScope()

            ModalNavigationDrawer(
                drawerState     = drawerState,
                gesturesEnabled = !canNavigateBack,  // disable swipe-open on detail screens
                drawerContent   = {
                    ShopFlowDrawerContent(
                        currentRoute = currentRoute,
                        onNavigate   = { route ->
                            navigateTopLevel(route)
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        ShopFlowTopBar(
                            currentRoute    = currentRoute,
                            windowSize      = windowSize,
                            canNavigateBack = canNavigateBack,
                            onMenuClick     = { scope.launch { drawerState.open() } },
                            onBackClick     = { navController.navigateUp() },
                            onAvatarClick   = { navigateTopLevel(AppRoute.Settings.route) }
                        )
                    },
                    bottomBar = {
                        if (!canNavigateBack) {
                            ShopFlowBottomBar(
                                currentRoute = currentRoute,
                                onNavigate   = ::navigateTopLevel
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        windowSize    = windowSize,
                        navigateChild = ::navigateChild,
                        modifier      = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════════
        //  MEDIUM — Tablet
        //  Layout: Scaffold(topBar) → Row[ NavigationRail | Content ]
        // ═══════════════════════════════════════════════════════════════════════
        AppWindowSize.MEDIUM -> {
            Scaffold(
                topBar = {
                    ShopFlowTopBar(
                        currentRoute    = currentRoute,
                        windowSize      = windowSize,
                        canNavigateBack = canNavigateBack,
                        onMenuClick     = { /* rail always visible */ },
                        onBackClick     = { navController.navigateUp() },
                        onAvatarClick   = { navigateTopLevel(AppRoute.Settings.route) }
                    )
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ShopFlowNavigationRail(
                        currentRoute = currentRoute,
                        onNavigate   = ::navigateTopLevel
                    )
                    AppNavHost(
                        navController = navController,
                        windowSize    = windowSize,
                        navigateChild = ::navigateChild,
                        modifier      = Modifier.weight(1f)
                    )
                }
            }
        }

        // ═══════════════════════════════════════════════════════════════════════
        //  EXPANDED — Desktop
        //  Layout: Scaffold(topBar) → Row[ PermanentDrawer | Content ]
        // ═══════════════════════════════════════════════════════════════════════
        AppWindowSize.EXPANDED -> {
            Scaffold(
                topBar = {
                    ShopFlowTopBar(
                        currentRoute    = currentRoute,
                        windowSize      = windowSize,
                        canNavigateBack = canNavigateBack,
                        onMenuClick     = { /* always visible */ },
                        onBackClick     = { navController.navigateUp() },
                        onAvatarClick   = { navigateTopLevel(AppRoute.Settings.route) }
                    )
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    ShopFlowPermanentDrawer(
                        currentRoute = currentRoute,
                        onNavigate   = ::navigateTopLevel
                    )
                    AppNavHost(
                        navController = navController,
                        windowSize    = windowSize,
                        navigateChild = ::navigateChild,
                        modifier      = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}