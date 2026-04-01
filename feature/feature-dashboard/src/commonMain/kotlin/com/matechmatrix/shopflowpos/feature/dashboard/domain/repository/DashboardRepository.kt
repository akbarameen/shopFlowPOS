package com.matechmatrix.shopflowpos.feature.dashboard.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Sale

interface DashboardRepository {
    suspend fun getTodayRevenue(): AppResult<Double>
    suspend fun getTodayGrossProfit(): AppResult<Double>
    suspend fun getTodayExpenses(): AppResult<Double>
    suspend fun getTodaySalesCount(): AppResult<Long>
    suspend fun getCashBalance(): AppResult<Double>
    suspend fun getTotalBankBalance(): AppResult<Double>
    suspend fun getRecentSales(limit: Int = 5): AppResult<List<Sale>>
    suspend fun getLowStockProducts(): AppResult<List<Product>>
    suspend fun getAnalyticsVisible(): Boolean
    suspend fun setAnalyticsVisible(visible: Boolean)
    suspend fun getCurrencySymbol(): String
    suspend fun getShopName(): String
    suspend fun getTotalInventoryValue(): AppResult<Double>
    suspend fun getTotalInventorySellingValue(): AppResult<Double>
    suspend fun getWeeklyRevenue(startMs: Long): AppResult<Map<String, Double>>
    suspend fun getMonthlyRevenue(startMs: Long): AppResult<Map<String, Double>>
    suspend fun getYearlyRevenue(startMs: Long): AppResult<Map<String, Double>>
    suspend fun getTotalReceivables(): AppResult<Double>
    suspend fun getTotalPayables(): AppResult<Double>
}
