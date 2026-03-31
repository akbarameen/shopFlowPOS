package com.matechmatrix.shopflowpos.feature.reports.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportsRepository
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportsRepositoryImpl(private val db: DatabaseProvider) : ReportsRepository {

    override suspend fun getReport(startMs: Long, endMs: Long): AppResult<ReportSummary> =
        withContext(Dispatchers.Default) {
            runCatching {
                val revenue      = db.saleQueries.getDailyRevenue(startMs, endMs).executeAsOne()
                val grossProfit  = db.saleQueries.getDailyGrossProfit(startMs, endMs).executeAsOne()
                val salesCount   = db.saleQueries.getDailySalesCount(startMs, endMs).executeAsOne()
                val expenses     = db.expenseQueries.getTotalExpensesByDateRange(startMs, endMs).executeAsOne()
                // Cash/bank revenue from sale_payment table (new schema)
                val cashRevenue  = db.saleQueries.getDailyCashCollected(startMs, endMs).executeAsOne()
                val bankRevenue  = db.saleQueries.getDailyBankCollected(startMs, endMs).executeAsOne()
                // Returns from sale_return header table
                val returnResult = db.saleReturnQueries.getTotalRefundsByDateRange(startMs, endMs).executeAsOne()
                val returnsAmount = returnResult.net_refund
                val returnCount   = returnResult.return_count
                // Total outstanding dues from sales
                val totalDues    = db.saleQueries.getTotalCustomerDues().executeAsOne()

                AppResult.Success(
                    ReportSummary(
                        revenue       = revenue,
                        grossProfit   = grossProfit,
                        expenses      = expenses,
                        netProfit     = grossProfit - expenses,
                        salesCount    = salesCount,
                        returnsAmount = returnsAmount,
                        returnCount   = returnCount,
                        cashRevenue   = cashRevenue,
                        bankRevenue   = bankRevenue,
                        totalDues     = totalDues,
                        costTotal     = revenue - grossProfit
                    )
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to generate report") }
        }

    override suspend fun getTopProducts(startMs: Long, endMs: Long, limit: Int): AppResult<List<TopProduct>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val rows = db.saleQueries.getTopSellingProductsByDateRange(startMs, endMs).executeAsList()
                AppResult.Success(rows.map {
                    TopProduct(
                        productId = it.product_id,
                        name      = it.product_name,
                        qty       = it.total_qty ?: 0L,
                        revenue   = it.total_revenue ?: 0.0,
                        cost      = it.total_cost ?: 0.0
                    )
                })
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load top products") }
        }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }

    override suspend fun getShopName(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("shop_name").executeAsOneOrNull() ?: "My Shop"
    }
}
