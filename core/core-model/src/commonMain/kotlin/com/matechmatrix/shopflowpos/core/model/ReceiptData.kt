package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.PaymentStatus
import kotlinx.serialization.Serializable

/**
 * Fully self-contained receipt snapshot.
 * Created once inside [completeSale] and never mutated.
 */
@Serializable
data class ReceiptData(
    val saleId           : String,
    val invoiceNumber    : String,
    val soldAt           : Long,
    // Customer
    val customerId       : String?  = null,
    val customerName     : String   = "Walk-in Customer",
    val customerPhone    : String   = "",
    val customerCnic     : String?  = null,
    val customerAddress  : String   = "",
    // Items
    val items            : List<SaleItem>,
    val payments         : List<SalePayment>,
    // Financials
    val subtotal         : Double,
    val discountAmount   : Double   = 0.0,
    val taxAmount        : Double   = 0.0,
    val totalAmount      : Double,
    val costTotal        : Double,
    val grossProfit      : Double,
    val paidAmount       : Double,
    val dueAmount        : Double,
    val paymentStatus    : PaymentStatus,
    val notes            : String?  = null,
    // Shop context (snapshot at time of sale)
    val shopName         : String,
    val shopAddress      : String   = "",
    val shopPhone        : String   = "",
    val currencySymbol   : String   = "Rs.",
    // Account labels for receipt display
    val cashAccountName  : String   = "Cash",
    val bankAccountNames : Map<String, String> = emptyMap()  // accountId → "HBL - 1234"
) {
    val cashPaid : Double get() = payments.filter { it.accountType == AccountType.CASH }.sumOf { it.amount }
    val bankPaid : Double get() = payments.filter { it.accountType == AccountType.BANK }.sumOf { it.amount }
    val hasDiscount: Boolean get() = discountAmount > 0
    val hasTax     : Boolean get() = taxAmount > 0
    val hasDue     : Boolean get() = dueAmount > 0
    val isWalkIn   : Boolean get() = customerId == null
}