package com.matechmatrix.shopflowpos.feature.reports.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult

data class ReportSummary(
    val revenue: Long = 0L,
    val grossProfit: Long = 0L,
    val expenses: Long = 0L,
    val netProfit: Long = 0L,
    val salesCount: Long = 0L,
    val returnsAmount: Long = 0L,
    val cashRevenue: Long = 0L,
    val bankRevenue: Long = 0L
)

data class TopProduct(val name: String, val qty: Long, val revenue: Long)

interface ReportsRepository {
    suspend fun getReport(startMs: Long, endMs: Long): AppResult<ReportSummary>
    suspend fun getTopProducts(startMs: Long, endMs: Long, limit: Int): AppResult<List<TopProduct>>
    suspend fun getCurrencySymbol(): String
    suspend fun getShopName(): String
}