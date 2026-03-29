package com.matechmatrix.shopflowpos.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val section: String = ""
)

// Bottom nav — 5 items max (phone)
val bottomNavItems = listOf(
    NavItem(AppRoute.Dashboard.route,    "Home",       Icons.Rounded.Dashboard),
    NavItem(AppRoute.POS.route,          "POS",        Icons.Rounded.PointOfSale),
    NavItem(AppRoute.Inventory.route,    "Stock",      Icons.Rounded.Inventory2),
    NavItem(AppRoute.Transactions.route, "Sales",      Icons.Rounded.Receipt),
    NavItem(AppRoute.Settings.route,     "Settings",   Icons.Rounded.Settings)
)

// Drawer / Rail — all items (tablet + desktop)
val drawerNavItems = listOf(
    NavItem(AppRoute.Dashboard.route,    "Dashboard",    Icons.Rounded.Dashboard,       "MAIN"),
    NavItem(AppRoute.POS.route,          "Point of Sale",Icons.Rounded.PointOfSale,     "MAIN"),
    NavItem(AppRoute.Inventory.route,    "Inventory",    Icons.Rounded.Inventory2,      "STOCK"),
    NavItem(AppRoute.Transactions.route, "Transactions", Icons.Rounded.Receipt,         "SALES"),
    NavItem(AppRoute.SalesReturn.route,  "Sales Return", Icons.Rounded.AssignmentReturn,"SALES"),
    NavItem(AppRoute.Customers.route,    "Customers",    Icons.Rounded.Group,           "PEOPLE"),
    NavItem(AppRoute.Suppliers.route,    "Suppliers",    Icons.Rounded.LocalShipping,   "PEOPLE"),
    NavItem(AppRoute.Ledger.route,       "Cash & Bank",  Icons.Rounded.AccountBalance,  "FINANCE"),
    NavItem(AppRoute.Expenses.route,     "Expenses",     Icons.Rounded.Money,           "FINANCE"),
    NavItem(AppRoute.Reports.route,      "Reports",      Icons.Rounded.Analytics,       "FINANCE"),
    NavItem(AppRoute.Installments.route, "Installments", Icons.Rounded.CreditCard,      "MORE"),
    NavItem(AppRoute.Repairs.route,      "Repairs",      Icons.Rounded.Build,           "MORE"),
    NavItem(AppRoute.Settings.route,     "Settings",     Icons.Rounded.Settings,        "MORE")
)