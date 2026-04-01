package com.matechmatrix.shopflowpos.feature.dashboard.presentation

import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Sale

data class DashboardState(
    val isLoading: Boolean = true,
    val shopName: String = "",
    val currencySymbol: String = "Rs.",
    val analyticsVisible: Boolean = true,
    // Today's stats
    val todayRevenue: Double = 0.0,
    val todayGrossProfit: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val todayNetProfit: Double = 0.0,
    val todaySalesCount: Long = 0L,
    // Balances
    val cashBalance: Double = 0.0,
    val bankBalance: Double = 0.0,
    // Dues
    val totalReceivables: Double = 0.0,
    val totalPayables: Double = 0.0,
    // Inventory stats
    val totalInventoryValue: Double = 0.0,
    val totalInventorySellingValue: Double = 0.0,
    // Lists
    val recentSales: List<Sale> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val error: String? = null,
    val weeklyRevenue: Map<String, Double> = emptyMap(),
    val monthlyRevenue: Map<String, Double> = emptyMap(),
    val yearlyRevenue: Map<String, Double> = emptyMap()
)

sealed class DashboardIntent {
    data object Load : DashboardIntent()
    data object Refresh : DashboardIntent()
    data object ToggleAnalyticsVisibility : DashboardIntent()
    data object NavigateToPOS : DashboardIntent()
    data object NavigateToInventory : DashboardIntent()
    data object NavigateToTransactions : DashboardIntent()
    data object NavigateToLedger : DashboardIntent()
}

sealed class DashboardEffect {
    data object GoToPOS : DashboardEffect()
    data object GoToInventory : DashboardEffect()
    data object GoToTransactions : DashboardEffect()
    data object GoToLedger : DashboardEffect()
}
