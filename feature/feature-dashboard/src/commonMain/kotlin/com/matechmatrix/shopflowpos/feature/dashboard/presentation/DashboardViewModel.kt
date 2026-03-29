package com.matechmatrix.shopflowpos.feature.dashboard.presentation

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.feature.dashboard.domain.usecase.GetDashboardStatsUseCase
import kotlinx.datetime.Clock

class DashboardViewModel(
    private val useCase: GetDashboardStatsUseCase
) : MviViewModel<DashboardState, DashboardIntent, DashboardEffect>(DashboardState()) {

    init {
        onIntent(DashboardIntent.Load)
    }

    override suspend fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.Load,
            DashboardIntent.Refresh -> loadAll()

            DashboardIntent.ToggleAnalyticsVisibility -> {
                val newValue = !state.value.analyticsVisible
                useCase.setAnalyticsVisible(newValue)
                setState { copy(analyticsVisible = newValue) }
            }

            DashboardIntent.NavigateToPOS ->
                setEffect(DashboardEffect.GoToPOS)

            DashboardIntent.NavigateToInventory ->
                setEffect(DashboardEffect.GoToInventory)

            DashboardIntent.NavigateToTransactions ->
                setEffect(DashboardEffect.GoToTransactions)

            DashboardIntent.NavigateToLedger ->
                setEffect(DashboardEffect.GoToLedger)
        }
    }

    private suspend fun loadAll() {
        setState { copy(isLoading = true, error = null) }
        try {
            // Settings
            val shopName       = useCase.getShopName()
            val currency       = useCase.getCurrencySymbol()
            val analyticsVis   = useCase.getAnalyticsVisible()

            // Stats
            val revenue        = (useCase.getTodayRevenue() as? AppResult.Success)?.data ?: 0.0
            val grossProfit    = (useCase.getTodayGrossProfit() as? AppResult.Success)?.data ?: 0.0
            val expenses       = (useCase.getTodayExpenses() as? AppResult.Success)?.data ?: 0.0
            val salesCount     = (useCase.getTodaySalesCount() as? AppResult.Success)?.data ?: 0L
            val cash           = (useCase.getCashBalance() as? AppResult.Success)?.data ?: 0.0
            val bank           = (useCase.getTotalBankBalance() as? AppResult.Success)?.data ?: 0.0
            
            // Inventory Stats
            val invValue       = (useCase.getTotalInventoryValue() as? AppResult.Success)?.data ?: 0.0
            val invSellValue   = (useCase.getTotalInventorySellingValue() as? AppResult.Success)?.data ?: 0.0

            val recentSales    = (useCase.getRecentSales(5) as? AppResult.Success)?.data ?: emptyList()
            val lowStock       = (useCase.getLowStockProducts() as? AppResult.Success)?.data ?: emptyList()

            // Dynamic Revenue Stats
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val weekStart = nowMs - (7 * 24 * 60 * 60 * 1000L)
            val monthStart = nowMs - (30 * 24 * 60 * 60 * 1000L)
            val yearStart = nowMs - (365 * 24 * 60 * 60 * 1000L)

            val weeklyRev = (useCase.getWeeklyRevenue(weekStart) as? AppResult.Success)?.data ?: emptyMap()
            val monthlyRev = (useCase.getMonthlyRevenue(monthStart) as? AppResult.Success)?.data ?: emptyMap()
            val yearlyRev = (useCase.getYearlyRevenue(yearStart) as? AppResult.Success)?.data ?: emptyMap()

            setState {
                copy(
                    isLoading        = false,
                    shopName         = shopName,
                    currencySymbol   = currency,
                    analyticsVisible = analyticsVis,
                    todayRevenue     = revenue,
                    todayGrossProfit = grossProfit,
                    todayExpenses    = expenses,
                    todayNetProfit   = grossProfit - expenses,
                    todaySalesCount  = salesCount,
                    cashBalance      = cash,
                    bankBalance      = bank,
                    totalInventoryValue = invValue,
                    totalInventorySellingValue = invSellValue,
                    recentSales      = recentSales,
                    lowStockProducts = lowStock,
                    weeklyRevenue    = weeklyRev,
                    monthlyRevenue   = monthlyRev,
                    yearlyRevenue    = yearlyRev
                )
            }
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.message) }
        }
    }
}
