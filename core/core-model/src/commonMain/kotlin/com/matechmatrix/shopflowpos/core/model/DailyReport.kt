package com.matechmatrix.shopflowpos.core.model

import kotlinx.serialization.Serializable

@Serializable
data class DailyReport(
    val date: String,                           // "yyyy-MM-dd"
    val totalSales: Int,
    val totalRevenue: Double,
    val totalCost: Double,
    val grossProfit: Double,
    val totalExpenses: Double,
    val netProfit: Double,                      // grossProfit + returnDeductions - totalExpenses
    val cashRevenue: Double,
    val bankRevenue: Double,
    val totalReturns: Int,
    val returnDeductions: Double,               // these go to profit
    val topProducts: List<ProductSalesSummary> = emptyList()
)

@Serializable
data class ProductSalesSummary(
    val productId: String,
    val productName: String,
    val quantitySold: Int,
    val revenue: Double
)