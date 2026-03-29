package com.matechmatrix.shopflowpos.feature.transactions.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.SaleStatus
import com.matechmatrix.shopflowpos.feature.transactions.domain.repository.TransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransactionsRepositoryImpl(private val db: DatabaseProvider) : TransactionsRepository {

    private fun mapSale(row: com.matechmatrix.shopflowpos.db.Sale) = Sale(
        id = row.id,
        invoiceNumber = row.invoice_number,
        customerId = row.customer_id,
        customerName = row.customer_name,
        items = emptyList(), // Items can be fetched separately if needed
        subtotal = row.subtotal,
        discount = row.discount,
        totalAmount = row.total_amount,
        costTotal = row.cost_total,
        grossProfit = row.gross_profit,
        paymentMethod = runCatching { PaymentMethod.valueOf(row.payment_method) }.getOrDefault(PaymentMethod.CASH),
        cashAmount = row.cash_amount,
        bankAmount = row.bank_amount,
        bankAccountId = row.bank_account_id,
        status = runCatching { SaleStatus.valueOf(row.status) }.getOrDefault(SaleStatus.COMPLETED),
        notes = row.notes,
        soldAt = row.sold_at
    )

    override suspend fun getSalesByDateRange(startMs: Long, endMs: Long): AppResult<List<Sale>> = withContext(Dispatchers.Default) {
        try {
            AppResult.Success(db.saleQueries.getSalesByDateRange(startMs, endMs).executeAsList().map(::mapSale))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to fetch transactions")
        }
    }

    override suspend fun getTodaySales(): AppResult<List<Sale>> {
        val (s, e) = DateTimeUtils.todayRange()
        return getSalesByDateRange(s, e)
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}
