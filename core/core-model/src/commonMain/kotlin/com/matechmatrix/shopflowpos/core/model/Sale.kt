package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.PaymentStatus
import com.matechmatrix.shopflowpos.core.model.enums.SaleStatus
import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id              : String,
    val invoiceNumber   : String,
    // Customer snapshot (always filled; customerId nullable for walk-ins)
    val customerId      : String? = null,
    val customerName    : String  = "Walk-in Customer",
    val customerPhone   : String  = "",
    val customerCnic    : String? = null,
    val customerAddress : String  = "",
    // Financials
    val subtotal        : Double  = 0.0,
    val discountAmount  : Double  = 0.0,
    val taxAmount       : Double  = 0.0,
    val totalAmount     : Double  = 0.0,
    val costTotal       : Double  = 0.0,
    val grossProfit     : Double  = 0.0,
    val paidAmount      : Double  = 0.0,
    val dueAmount       : Double  = 0.0,
    val paymentStatus   : PaymentStatus = PaymentStatus.PAID,
    val dueDate         : Long?   = null,
    val status          : SaleStatus    = SaleStatus.COMPLETED,
    val notes           : String? = null,
    val soldAt          : Long,
    val updatedAt       : Long
)