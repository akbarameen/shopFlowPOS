package com.matechmatrix.shopflowpos.feature.salesreturn.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.*
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.ReturnRequest
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.SalesReturnRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SalesReturnRepositoryImpl(private val db: DatabaseProvider) : SalesReturnRepository {

    private fun mapReturn(r: com.matechmatrix.shopflowpos.db.Sale_return) = SaleReturn(
        id               = r.id,
        returnNumber     = r.return_number,
        originalSaleId   = r.original_sale_id,
        customerId       = r.customer_id,
        customerName     = r.customer_name,
        customerPhone    = r.customer_phone,
        grossRefundAmount = r.gross_refund_amount,
        deductionAmount  = r.deduction_amount,
        netRefundAmount  = r.net_refund_amount,
        refundMethod     = runCatching { RefundMethod.valueOf(r.refund_method) }.getOrDefault(RefundMethod.CASH),
        accountType      = r.account_type?.let { runCatching { AccountType.valueOf(it) }.getOrNull() },
        accountId        = r.account_id,
        returnReason     = r.return_reason,
        notes            = r.notes,
        returnedAt       = r.returned_at
    )

    private fun mapSaleRow(r: com.matechmatrix.shopflowpos.db.Sale) = Sale(
        id              = r.id,
        invoiceNumber   = r.invoice_number,
        customerId      = r.customer_id,
        customerName    = r.customer_name,
        customerPhone   = r.customer_phone,
        customerCnic    = r.customer_cnic,
        customerAddress = r.customer_address,
        subtotal        = r.subtotal,
        discountAmount  = r.discount_amount,
        taxAmount       = r.tax_amount,
        totalAmount     = r.total_amount,
        costTotal       = r.cost_total,
        grossProfit     = r.gross_profit,
        paidAmount      = r.paid_amount,
        dueAmount       = r.due_amount,
        paymentStatus   = runCatching { PaymentStatus.valueOf(r.payment_status) }.getOrDefault(PaymentStatus.PAID),
        dueDate         = r.due_date,
        status          = runCatching { SaleStatus.valueOf(r.status) }.getOrDefault(SaleStatus.COMPLETED),
        notes           = r.notes,
        soldAt          = r.sold_at,
        updatedAt       = r.updated_at
    )

    private fun mapSaleItem(r: com.matechmatrix.shopflowpos.db.Sale_item) = SaleItem(
        id          = r.id,
        saleId      = r.sale_id,
        productId   = r.product_id,
        productName = r.product_name,
        imei        = r.imei,
        category    = r.category,
        quantity    = r.quantity.toInt(),
        unitCost    = r.unit_cost,
        unitPrice   = r.unit_price,
        discount    = r.discount,
        lineTotal   = r.line_total
    )

    override suspend fun getReturnsByDateRange(startMs: Long, endMs: Long): AppResult<List<SaleReturn>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.saleReturnQueries.getReturnsByDateRange(startMs, endMs).executeAsList().map(::mapReturn)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load returns") }
        }

    override suspend fun getSaleByInvoice(invoiceNumber: String): AppResult<Sale?> =
        withContext(Dispatchers.Default) {
            runCatching {
                // Use indexed invoice_number column for O(1) lookup
                val row = db.saleQueries.getSalesByDateRange(0L, Long.MAX_VALUE)
                    .executeAsList()
                    .firstOrNull { it.invoice_number.equals(invoiceNumber, ignoreCase = true) }
                AppResult.Success(row?.let { mapSaleRow(it) })
            }.getOrElse { AppResult.Error(it.message ?: "Failed to find sale") }
        }

    override suspend fun getSaleItems(saleId: String): AppResult<List<SaleItem>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.saleQueries.getSaleItems(saleId).executeAsList().map(::mapSaleItem))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load items") }
        }

    override suspend fun processReturn(request: ReturnRequest): AppResult<SaleReturn> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now      = Clock.System.now().toEpochMilliseconds()
                val returnId = IdGenerator.generate()
                var returnNumber = ""

                // Compute financials
                val grossRefund = request.items.sumOf { ri ->
                    ri.saleItem.unitPrice * ri.returnedQty - ri.saleItem.discount
                }
                val deduction   = grossRefund * (request.deductionPercent / 100.0)
                val netRefund   = (grossRefund - deduction).coerceAtLeast(0.0)

                db.database.transaction {
                    // 1. Generate return number
                    db.invoiceSequenceQueries.incrementSequence(now, "RET")
                    val seq = db.invoiceSequenceQueries.getSequence("RET").executeAsOne()
                    returnNumber = "RET-${seq.toString().padStart(5, '0')}"

                    // 2. Insert sale_return header
                    db.saleReturnQueries.insertSaleReturn(
                        id                  = returnId,
                        return_number       = returnNumber,
                        original_sale_id    = request.originalSaleId,
                        customer_id         = request.customerId,
                        customer_name       = request.customerName,
                        customer_phone      = request.originalSale.customerPhone,
                        gross_refund_amount = grossRefund,
                        deduction_amount    = deduction,
                        net_refund_amount   = netRefund,
                        refund_method       = request.refundMethod.name,
                        account_type        = request.accountType?.name,
                        account_id          = request.accountId,
                        return_reason       = request.returnReason,
                        notes               = request.notes.takeIf { it.isNotBlank() },
                        returned_at         = now
                    )

                    // 3. Insert sale_return_items + handle stock
                    request.items.filter { it.returnedQty > 0 }.forEach { ri ->
                        val itemId     = IdGenerator.generate()
                        val lineRefund = ri.saleItem.unitPrice * ri.returnedQty - ri.saleItem.discount - (ri.saleItem.unitPrice * ri.returnedQty * request.deductionPercent / 100.0)

                        db.saleReturnQueries.insertSaleReturnItem(
                            id                    = itemId,
                            return_id             = returnId,
                            original_sale_item_id = ri.saleItem.id,
                            product_id            = ri.saleItem.productId,
                            product_name          = ri.saleItem.productName,
                            imei                  = ri.saleItem.imei,
                            returned_quantity     = ri.returnedQty.toLong(),
                            unit_price            = ri.saleItem.unitPrice,
                            restock_item          = if (ri.restockItem) 1L else 0L,
                            line_refund           = lineRefund.coerceAtLeast(0.0)
                        )

                        // Stock back in
                        if (ri.restockItem) {
                            val product = db.productQueries.getById(ri.saleItem.productId).executeAsOneOrNull()
                            val before  = product?.stock ?: 0L

                            if (ri.saleItem.imei.isNullOrBlank()) {
                                db.productQueries.incrementStock(ri.returnedQty.toLong(), now, ri.saleItem.productId)
                            } else {
                                // Re-activate IMEI item
                                db.productQueries.updateStock(1L, now, ri.saleItem.productId)
                            }

                            db.stockMovementQueries.insertStockMovement(
                                id              = IdGenerator.generate(),
                                product_id      = ri.saleItem.productId,
                                product_name    = ri.saleItem.productName,
                                movement_type   = MovementType.SALE_RETURN.name,
                                reference_type  = "SALE_RETURN",
                                reference_id    = returnId,
                                quantity_before = before,
                                quantity_change = ri.returnedQty.toLong(),
                                quantity_after  = before + ri.returnedQty,
                                unit_cost       = ri.saleItem.unitCost,
                                notes           = returnNumber,
                                created_at      = now
                            )
                        }
                    }

                    // 4. Refund accounting
                    if (netRefund > 0 && request.refundMethod != RefundMethod.STORE_CREDIT && !request.accountId.isNullOrBlank()) {
                        val entryId = IdGenerator.generate()
                        when (request.accountType) {
                            AccountType.CASH -> {
                                val acc        = db.ledgerQueries.getCashAccountById(request.accountId!!).executeAsOne()
                                val newBalance = (acc.balance - netRefund).coerceAtLeast(0.0)
                                db.ledgerQueries.updateCashBalance(newBalance, now, request.accountId)
                                db.ledgerQueries.insertLedgerEntry(
                                    id             = entryId,
                                    account_type   = "CASH",
                                    account_id     = request.accountId,
                                    entry_type     = "DEBIT",
                                    reference_type = "SALE_PAYMENT",
                                    reference_id   = returnId,
                                    amount         = netRefund,
                                    balance_after  = newBalance,
                                    description    = "Return Refund $returnNumber — ${request.customerName}",
                                    created_at     = now
                                )
                            }
                            AccountType.BANK -> {
                                val acc        = db.ledgerQueries.getBankAccountById(request.accountId!!).executeAsOne()
                                val newBalance = (acc.balance - netRefund).coerceAtLeast(0.0)
                                db.ledgerQueries.updateBankBalance(newBalance, now, request.accountId)
                                db.ledgerQueries.insertLedgerEntry(
                                    id             = entryId,
                                    account_type   = "BANK",
                                    account_id     = request.accountId,
                                    entry_type     = "DEBIT",
                                    reference_type = "SALE_PAYMENT",
                                    reference_id   = returnId,
                                    amount         = netRefund,
                                    balance_after  = newBalance,
                                    description    = "Return Refund $returnNumber — ${request.customerName}",
                                    created_at     = now
                                )
                            }
                            else -> {}
                        }
                    }

                    // 5. Update sale status
                    val totalReturnedQty = request.items.sumOf { it.returnedQty }
                    val totalSoldQty     = request.originalSale.let {
                        db.saleQueries.getSaleItems(it.id).executeAsList().sumOf { si -> si.quantity }
                    }
                    val newSaleStatus = if (totalReturnedQty >= totalSoldQty)
                        SaleStatus.RETURNED.name else SaleStatus.PARTIALLY_RETURNED.name
                    db.saleQueries.updateSaleStatus(newSaleStatus, now, request.originalSaleId)

                    // 6. Reduce customer outstanding if applicable (for credit sales)
                    if (!request.customerId.isNullOrBlank() && request.originalSale.dueAmount > 0) {
                        val reduction = netRefund.coerceAtMost(request.originalSale.dueAmount)
                        if (reduction > 0) {
                            db.customerQueries.decrementCustomerOutstanding(reduction, now, request.customerId)
                        }
                    }
                }

                AppResult.Success(
                    SaleReturn(
                        id               = returnId,
                        returnNumber     = returnNumber,
                        originalSaleId   = request.originalSaleId,
                        customerId       = request.customerId,
                        customerName     = request.customerName,
                        customerPhone    = request.originalSale.customerPhone,
                        grossRefundAmount = grossRefund,
                        deductionAmount  = deduction,
                        netRefundAmount  = netRefund,
                        refundMethod     = request.refundMethod,
                        accountType      = request.accountType,
                        accountId        = request.accountId,
                        returnReason     = request.returnReason,
                        notes            = request.notes.takeIf { it.isNotBlank() },
                        returnedAt       = now
                    )
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to process return") }
        }

    override suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map {
                    CashAccount(id = it.id, name = it.name, balance = it.balance,
                        isActive = it.is_active == 1L, createdAt = it.created_at, updatedAt = it.updated_at)
                })
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map {
                    BankAccount(id = it.id, bankName = it.bank_name, accountTitle = it.account_title,
                        accountNumber = it.account_number, iban = it.iban, balance = it.balance,
                        isActive = it.is_active == 1L, createdAt = it.created_at, updatedAt = it.updated_at)
                })
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getDefaultDeductionPercent(): Double = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("default_deduction_percent").executeAsOneOrNull()?.toDoubleOrNull() ?: 0.0
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}