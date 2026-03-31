package com.matechmatrix.shopflowpos.feature.reports.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult

data class ReportSummary(
    val revenue        : Double = 0.0,
    val grossProfit    : Double = 0.0,
    val expenses       : Double = 0.0,
    val netProfit      : Double = 0.0,  // grossProfit - expenses
    val salesCount     : Long   = 0L,
    val returnsAmount  : Double = 0.0,
    val returnCount    : Long   = 0L,
    val cashRevenue    : Double = 0.0,  // from sale_payment table
    val bankRevenue    : Double = 0.0,
    val totalDues      : Double = 0.0,  // outstanding from sales
    val costTotal      : Double = 0.0
) {
    val profitMargin: Double get() =
        if (revenue > 0) (grossProfit / revenue) * 100.0 else 0.0
}

data class TopProduct(
    val productId  : String,
    val name       : String,
    val qty        : Long,
    val revenue    : Double,
    val cost       : Double
) {
    val grossProfit: Double get() = revenue - cost
}

interface ReportsRepository {
    suspend fun getReport(startMs: Long, endMs: Long): AppResult<ReportSummary>
    suspend fun getTopProducts(startMs: Long, endMs: Long, limit: Int): AppResult<List<TopProduct>>
    suspend fun getCurrencySymbol(): String
    suspend fun getShopName(): String
}
