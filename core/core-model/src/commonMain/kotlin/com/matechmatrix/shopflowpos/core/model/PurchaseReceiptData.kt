package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.GoodsStatus
import com.matechmatrix.shopflowpos.core.model.enums.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseReceiptData(
    val purchaseOrderId   : String,
    val poNumber          : String,
    val purchasedAt       : Long,
    // Supplier snapshot
    val supplierId        : String?,
    val supplierName      : String,
    val supplierPhone     : String  = "",
    // Items
    val items             : List<PurchaseOrderItem>,
    val payments          : List<PurchasePayment>,
    // Financials
    val subtotal          : Double,
    val discountAmount    : Double  = 0.0,
    val totalAmount       : Double,
    val paidAmount        : Double,
    val dueAmount         : Double,
    val paymentStatus     : PaymentStatus,
    val goodsStatus       : GoodsStatus,
    val notes             : String? = null,
    // Shop context
    val shopName          : String,
    val shopAddress       : String  = "",
    val shopPhone         : String  = "",
    val currencySymbol    : String  = "Rs.",
    val cashAccountName   : String  = "Cash",
    val bankAccountNames  : Map<String, String> = emptyMap()
) {
    val cashPaid: Double get() = payments.filter { it.accountType == AccountType.CASH }.sumOf { it.amount }
    val bankPaid: Double get() = payments.filter { it.accountType == AccountType.BANK }.sumOf { it.amount }
    val hasDiscount: Boolean get() = discountAmount > 0
    val hasDue: Boolean get() = dueAmount > 0
}