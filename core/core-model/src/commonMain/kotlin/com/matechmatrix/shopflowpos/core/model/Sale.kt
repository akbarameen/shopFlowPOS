package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.SaleStatus
import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String,
    val invoiceNumber: String,
    val customerId: String? = null,
    val customerName: String? = null,
    val items: List<SaleItem> = emptyList(),
    val subtotal: Double,
    val discount: Double = 0.0,
    val totalAmount: Double,
    val costTotal: Double,
    val grossProfit: Double,
    val paymentMethod: PaymentMethod,
    val cashAmount: Double = 0.0,
    val bankAmount: Double = 0.0,
    val bankAccountId: String? = null,
    val dueAmount: Double = 0.0,
    val dueDate: Long? = null,
    val status: SaleStatus = SaleStatus.COMPLETED,
    val notes: String? = null,
    val soldAt: Long
)
