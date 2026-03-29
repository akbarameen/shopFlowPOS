package com.matechmatrix.shopflowpos.feature.salesreturn.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.*
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.SalesReturnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SalesReturnRepositoryImpl(private val db: DatabaseProvider) : SalesReturnRepository {

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Sale_return) = SaleReturn(
        id = r.id,
        originalSaleId = r.original_sale_id,
        productId = r.product_id,
        productName = r.product_name,
        returnedQuantity = r.returned_quantity.toInt(),
        originalSellingPrice = r.original_selling_price,
        deductionAmount = r.deduction_amount,
        refundAmount = r.refund_amount,
        returnReason = r.return_reason,
        returnedAt = r.returned_at
    )

    override suspend fun getReturnsByDateRange(startMs: Long, endMs: Long): AppResult<List<SaleReturn>> =
        withContext(Dispatchers.IO) {
            try {
                val rows = db.saleReturnQueries.getReturnsByDateRange(startMs, endMs).executeAsList()
                AppResult.Success(rows.map(::mapRow))
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to load returns")
            }
        }

    override suspend fun getSaleByInvoice(invoiceNumber: String): AppResult<Sale?> =
        withContext(Dispatchers.IO) {
            try {
                // Using the specific date range check as a fallback if you don't have a direct invoice query
                val row = db.saleQueries.getSalesByDateRange(0L, Long.MAX_VALUE).executeAsList()
                    .firstOrNull { it.invoice_number == invoiceNumber }

                if (row == null) return@withContext AppResult.Success(null)

                val items = db.saleQueries.getSaleItemsBySaleId(row.id).executeAsList().map { si ->
                    SaleItem(
                        id = si.id, saleId = si.sale_id, productId = si.product_id,
                        productName = si.product_name, quantity = si.quantity.toInt(),
                        costPrice = si.cost_price, sellingPrice = si.selling_price,
                        discount = si.discount, lineTotal = si.line_total
                    )
                }

                AppResult.Success(Sale(
                    id = row.id, invoiceNumber = row.invoice_number,
                    customerId = row.customer_id, customerName = row.customer_name,
                    items = items, subtotal = row.subtotal, discount = row.discount,
                    totalAmount = row.total_amount, costTotal = row.cost_total,
                    grossProfit = row.gross_profit,
                    paymentMethod = runCatching { PaymentMethod.valueOf(row.payment_method) }.getOrDefault(PaymentMethod.CASH),
                    cashAmount = row.cash_amount, bankAmount = row.bank_amount,
                    bankAccountId = row.bank_account_id,
                    status = runCatching { SaleStatus.valueOf(row.status) }.getOrDefault(SaleStatus.COMPLETED),
                    notes = row.notes, soldAt = row.sold_at
                ))
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to find sale")
            }
        }

    override suspend fun insertReturn(
        saleReturn: SaleReturn,
        restockProductId: String?,
        restockQty: Int
    ): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.transaction {
                val now = Clock.System.now().toEpochMilliseconds()

                // 1. Insert Return Record
                db.saleReturnQueries.insertReturn(
                    id = saleReturn.id,
                    original_sale_id = saleReturn.originalSaleId,
                    product_id = saleReturn.productId,
                    product_name = saleReturn.productName,
                    returned_quantity = saleReturn.returnedQuantity.toLong(),
                    original_selling_price = saleReturn.originalSellingPrice,
                    deduction_amount = saleReturn.deductionAmount,
                    refund_amount = saleReturn.refundAmount,
                    return_reason = saleReturn.returnReason,
                    returned_at = now,

                )

                // 2. Increment Stock in Product Table
                if (!restockProductId.isNullOrBlank() && restockQty > 0) {
                    db.productQueries.incrementStock(
                        stock = restockQty.toLong(), // SQLDelight uses the column name 'stock' for the '?'
                        updated_at = now,
                        id = restockProductId
                    )
                }

                // 3. Update Ledger if money was refunded
                if (saleReturn.refundAmount > 0) {
                    val currentBalance = db.ledgerQueries.getCashBalance().executeAsOne()
                    val newBalance = (currentBalance - saleReturn.refundAmount).coerceAtLeast(0.0)

                    db.ledgerQueries.updateCashBalance(newBalance, now)

                    db.ledgerQueries.insertLedgerEntry(
                        id = IdGenerator.generate(),
                        type = TransactionType.RETURN_REFUND.name,
                        amount = saleReturn.refundAmount,
                        account_type = AccountType.CASH.name,
                        bank_account_id = null,
                        reference_id = saleReturn.id,
                        description = "Sales Return Refund for ${saleReturn.productName}",
                        balance_after = newBalance,
                        created_at = now
                    )
                }
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to process return")
        }
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.IO) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}