package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.GoodsStatus
import com.matechmatrix.shopflowpos.core.model.enums.PaymentStatus
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseOrder(
    val id                  : String,
    val poNumber            : String,
    val supplierId          : String,
    val supplierName        : String,
    val supplierPhone       : String  = "",
    val subtotal            : Double  = 0.0,
    val discountAmount      : Double  = 0.0,
    val totalAmount         : Double  = 0.0,
    val paidAmount          : Double  = 0.0,
    val dueAmount           : Double  = 0.0,
    val paymentStatus       : PaymentStatus = PaymentStatus.UNPAID,
    val supplierInvoiceRef  : String? = null,
    val goodsStatus         : GoodsStatus = GoodsStatus.RECEIVED,
    val notes               : String? = null,
    val purchasedAt         : Long,
    val updatedAt           : Long
)