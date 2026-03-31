package com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.RefundMethod

data class ReturnRequest(
    val originalSaleId  : String,
    val originalSale    : Sale,
    val items           : List<ReturnItemRequest>,
    val returnReason    : String,
    val notes           : String,
    val refundMethod    : RefundMethod,
    val accountType     : AccountType?,
    val accountId       : String?,
    val deductionPercent: Double,   // 0–100
    val customerId      : String?,
    val customerName    : String
)

data class ReturnItemRequest(
    val saleItem        : SaleItem,
    val returnedQty     : Int,
    val restockItem     : Boolean
)

interface SalesReturnRepository {
    suspend fun getReturnsByDateRange(startMs: Long, endMs: Long): AppResult<List<SaleReturn>>
    suspend fun getSaleByInvoice(invoiceNumber: String): AppResult<Sale?>
    suspend fun getSaleItems(saleId: String): AppResult<List<SaleItem>>

    /**
     * Atomically:
     *   1. incrementSequence("RET") → return_number
     *   2. insertSaleReturn
     *   3. insertSaleReturnItem (per item)
     *   4. If restockItem: incrementStock + insertStockMovement (SALE_RETURN)
     *   5. Refund: update account balance + insertLedgerEntry (DEBIT, EXPENSE style)
     *   6. Update sale.status (PARTIALLY_RETURNED or RETURNED)
     *   7. decrementCustomerOutstanding if customerId != null
     */
    suspend fun processReturn(request: ReturnRequest): AppResult<SaleReturn>

    suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>>
    suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>>
    suspend fun getDefaultDeductionPercent(): Double
    suspend fun getCurrencySymbol(): String
}