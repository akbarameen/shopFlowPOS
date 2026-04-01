package com.matechmatrix.shopflowpos.feature.dashboard.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.feature.dashboard.domain.repository.DashboardRepository

class GetDashboardStatsUseCase(
    private val repo: DashboardRepository
) {
    suspend fun getTodayRevenue(): AppResult<Double> = repo.getTodayRevenue()
    suspend fun getTodayGrossProfit(): AppResult<Double> = repo.getTodayGrossProfit()
    suspend fun getTodayExpenses(): AppResult<Double> = repo.getTodayExpenses()
    suspend fun getTodaySalesCount(): AppResult<Long> = repo.getTodaySalesCount()
    suspend fun getCashBalance(): AppResult<Double> = repo.getCashBalance()
    suspend fun getTotalBankBalance(): AppResult<Double> = repo.getTotalBankBalance()
    suspend fun getRecentSales(limit: Int): AppResult<List<Sale>> = repo.getRecentSales(limit)
    suspend fun getLowStockProducts(): AppResult<List<Product>> = repo.getLowStockProducts()
    suspend fun getAnalyticsVisible(): Boolean = repo.getAnalyticsVisible()
    suspend fun setAnalyticsVisible(visible: Boolean) = repo.setAnalyticsVisible(visible)
    suspend fun getCurrencySymbol(): String = repo.getCurrencySymbol()
    suspend fun getShopName(): String = repo.getShopName()
    suspend fun getTotalInventoryValue(): AppResult<Double> = repo.getTotalInventoryValue()
    suspend fun getTotalInventorySellingValue(): AppResult<Double> = repo.getTotalInventorySellingValue()
    suspend fun getWeeklyRevenue(startMs: Long): AppResult<Map<String, Double>> = repo.getWeeklyRevenue(startMs)
    suspend fun getMonthlyRevenue(startMs: Long): AppResult<Map<String, Double>> = repo.getMonthlyRevenue(startMs)
    suspend fun getYearlyRevenue(startMs: Long): AppResult<Map<String, Double>> = repo.getYearlyRevenue(startMs)
    suspend fun getTotalReceivables(): AppResult<Double> = repo.getTotalReceivables()
    suspend fun getTotalPayables(): AppResult<Double> = repo.getTotalPayables()
}
