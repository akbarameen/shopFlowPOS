package com.matechmatrix.shopflowpos.navigation

sealed class AppRoute(val route: String) {
    data object Auth         : AppRoute("auth")
    data object Dashboard    : AppRoute("dashboard")
    data object POS          : AppRoute("pos")
    data object Inventory    : AppRoute("inventory")
    data object Transactions : AppRoute("transactions")
    data object SalesReturn  : AppRoute("sales_return")
    data object Customers    : AppRoute("customers")
    data object Suppliers    : AppRoute("suppliers")
    data object Expenses     : AppRoute("expenses")
    data object Ledger       : AppRoute("ledger")
    data object Reports      : AppRoute("reports")
    data object Installments : AppRoute("installments")
    data object Repairs      : AppRoute("repairs")
    data object Settings     : AppRoute("settings")
    data object Purchase     : AppRoute("Purchase")
    data object Dues         : AppRoute("Dues")
    data object Reviews      : AppRoute("reviews")
}