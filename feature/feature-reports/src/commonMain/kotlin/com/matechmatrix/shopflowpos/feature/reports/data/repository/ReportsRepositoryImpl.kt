package com.matechmatrix.shopflowpos.feature.reports.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportsRepository
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class ReportsRepositoryImpl(private val db: DatabaseProvider) : ReportsRepository {

    override suspend fun getReport(startMs: Long, endMs: Long): AppResult<ReportSummary> =
        withContext(Dispatchers.IO) { // Use IO for DB operations
            try {
                // SQLDelight returns Double for these sums
                val revenue = db.saleQueries.getDailyRevenue(startMs, endMs).executeAsOne()
                val grossProfit = db.saleQueries.getDailyGrossProfit(startMs, endMs).executeAsOne()
                val expenses = db.expenseQueries.getTotalExpensesByDateRange(startMs, endMs).executeAsOne()
                val salesCount = db.saleQueries.getDailySalesCount(startMs, endMs).executeAsOne()

                // Assuming saleReturnQueries also returns Double
                val returns = 0.0 // Replace with actual call if implemented

                val cashRevenue = db.saleQueries.getDailyCashRevenue(startMs, endMs).executeAsOne()
                val bankRevenue = db.saleQueries.getDailyBankRevenue(startMs, endMs).executeAsOne()

                AppResult.Success(ReportSummary(
                    revenue = revenue.toLong(),
                    grossProfit = grossProfit.toLong(),
                    expenses = expenses.toLong(),
                    netProfit = grossProfit.toLong() - expenses.toLong(),
                    salesCount = salesCount.toLong(),
                    returnsAmount = returns.toLong(),
                    cashRevenue = cashRevenue.toLong(),
                    bankRevenue = bankRevenue.toLong()
                ))
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to generate report")
            }
        }

    override suspend fun getTopProducts(startMs: Long, endMs: Long, limit: Int): AppResult<List<TopProduct>> =
        withContext(Dispatchers.IO) {
            try {
                // Use the built-in SQL query for better performance
                val products = db.saleQueries.getTopProductsByDateRange(startMs, endMs)
                    .executeAsList()
                    .map {
                        TopProduct(
                            name = it.product_name,
                            qty = it.total_qty ?: 0L,
                            revenue = it.total_revenue?.toLong() ?: 0L
                        )
                    }
                AppResult.Success(products)
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to load top products")
            }
        }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.IO) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }

    override suspend fun getShopName(): String = withContext(Dispatchers.IO) {
        db.settingsQueries.getSetting("shop_name").executeAsOneOrNull() ?: "My Shop"
    }
}