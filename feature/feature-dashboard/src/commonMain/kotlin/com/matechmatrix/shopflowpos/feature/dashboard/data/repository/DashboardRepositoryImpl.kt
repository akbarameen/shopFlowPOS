package com.matechmatrix.shopflowpos.feature.dashboard.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import com.matechmatrix.shopflowpos.core.model.enums.SaleStatus
import com.matechmatrix.shopflowpos.feature.dashboard.domain.repository.DashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DashboardRepositoryImpl(
    private val db: DatabaseProvider
) : DashboardRepository {

    override suspend fun getTodayRevenue(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val (start, end) = DateTimeUtils.todayRange()
            val amount = db.saleQueries.getDailyRevenue(start, end).executeAsOneOrNull() ?: 0.0
            AppResult.Success(amount)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get revenue")
        }
    }

    override suspend fun getTodayGrossProfit(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val (start, end) = DateTimeUtils.todayRange()
            val profit = db.saleQueries.getDailyGrossProfit(start, end).executeAsOneOrNull() ?: 0.0
            AppResult.Success(profit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get gross profit")
        }
    }

    override suspend fun getTodayExpenses(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val (start, end) = DateTimeUtils.todayRange()
            val expenses = db.expenseQueries.getTotalExpensesByDateRange(start, end).executeAsOneOrNull() ?: 0.0
            AppResult.Success(expenses)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get expenses")
        }
    }

    override suspend fun getTodaySalesCount(): AppResult<Long> = withContext(Dispatchers.Default) {
        try {
            val (start, end) = DateTimeUtils.todayRange()
            val count = db.saleQueries.getDailySalesCount(start, end).executeAsOneOrNull() ?: 0L
            AppResult.Success(count)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get sales count")
        }
    }

    override suspend fun getCashBalance(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val balance = db.ledgerQueries.getCashBalance().executeAsOne()
            AppResult.Success(balance)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get cash balance")
        }
    }

    override suspend fun getTotalBankBalance(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val total = db.ledgerQueries.getTotalBankBalance().executeAsOneOrNull() ?: 0.0
            AppResult.Success(total)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get bank balance")
        }
    }

    override suspend fun getRecentSales(limit: Int): AppResult<List<Sale>> = withContext(Dispatchers.Default) {
        try {
            val (start, end) = DateTimeUtils.todayRange()
            val rows = db.saleQueries.getSalesByDateRange(start, end).executeAsList()
            val sales = rows.takeLast(limit).map { row ->
                Sale(
                    id             = row.id,
                    invoiceNumber  = row.invoice_number,
                    customerId     = row.customer_id,
                    customerName   = row.customer_name,
                    subtotal       = row.subtotal,
                    discount       = row.discount,
                    totalAmount    = row.total_amount,
                    costTotal      = row.cost_total,
                    grossProfit    = row.gross_profit,
                    paymentMethod  = PaymentMethod.valueOf(row.payment_method),
                    cashAmount     = row.cash_amount,
                    bankAmount     = row.bank_amount,
                    bankAccountId  = row.bank_account_id,
                    dueAmount      = row.due_amount,
                    dueDate        = row.due_date,
                    status         = SaleStatus.valueOf(row.status),
                    notes          = row.notes,
                    soldAt         = row.sold_at
                )
            }
            AppResult.Success(sales)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get recent sales")
        }
    }

    override suspend fun getLowStockProducts(): AppResult<List<Product>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.productQueries.getLowStock().executeAsList()
            val products = rows.map { row ->
                Product(
                    id                = row.id,
                    name              = row.name,
                    brand             = row.brand,
                    model             = row.model,
                    imei              = row.imei,
                    barcode           = row.barcode,
                    category          = ProductCategory.valueOf(row.category),
                    condition         = ProductCondition.valueOf(row.condition_type),
                    ptaStatus         = row.pta_status,
                    costPrice         = row.cost_price,
                    sellingPrice      = row.selling_price,
                    stock             = row.stock.toInt(),
                    lowStockThreshold = row.low_stock_threshold.toInt(),
                    description       = row.notes,
                    isActive          = row.is_active == 1L,
                    createdAt         = row.created_at,
                    updatedAt         = row.updated_at
                )
            }
            AppResult.Success(products)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get low stock products")
        }
    }

    override suspend fun getAnalyticsVisible(): Boolean = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("analytics_visible").executeAsOneOrNull() != "false"
    }

    override suspend fun setAnalyticsVisible(visible: Boolean) = withContext(Dispatchers.Default) {
        db.settingsQueries.upsertSetting("analytics_visible", visible.toString())
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }

    override suspend fun getShopName(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("shop_name").executeAsOneOrNull() ?: "My Shop"
    }

    override suspend fun getTotalInventoryValue(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val total = db.productQueries.getTotalInventoryValue().executeAsOne()
            AppResult.Success(total)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get inventory value")
        }
    }

    override suspend fun getTotalInventorySellingValue(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val total = db.productQueries.getTotalInventorySellingValue().executeAsOne()
            AppResult.Success(total)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get inventory selling value")
        }
    }

    override suspend fun getWeeklyRevenue(startMs: Long): AppResult<Map<String, Double>> = withContext(Dispatchers.Default) {
        try {
            val results = db.saleQueries.getWeeklyRevenue(startMs).executeAsList()
            // Ensure the Double? from DB is converted to a non-nullable Double
            AppResult.Success(results.associate { it.week to (it.revenue ?: 0.0) })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get weekly revenue")
        }
    }

    override suspend fun getMonthlyRevenue(startMs: Long): AppResult<Map<String, Double>> = withContext(Dispatchers.Default) {
        try {
            val results = db.saleQueries.getMonthlyRevenue(startMs).executeAsList()
            // Ensure the Double? from DB is converted to a non-nullable Double
            AppResult.Success(results.associate { it.month to (it.revenue ?: 0.0) })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get monthly revenue")
        }
    }

    override suspend fun getYearlyRevenue(startMs: Long): AppResult<Map<String, Double>> = withContext(Dispatchers.Default) {
        try {
            val results = db.saleQueries.getYearlyRevenue(startMs).executeAsList()
            // Ensure the Double? from DB is converted to a non-nullable Double
            AppResult.Success(results.associate { it.year to (it.revenue ?: 0.0) })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get yearly revenue")
        }
    }
}
