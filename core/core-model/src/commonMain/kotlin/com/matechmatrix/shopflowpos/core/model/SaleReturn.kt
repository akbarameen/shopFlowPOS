package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.RefundMethod
import kotlinx.serialization.Serializable
// ── SaleReturn.kt (header) ───────────────────────────────────────────────────
@Serializable
data class SaleReturn(
    val id                 : String,
    val returnNumber       : String,
    val originalSaleId     : String,
    val customerId         : String? = null,
    val customerName       : String  = "",
    val customerPhone      : String  = "",
    val grossRefundAmount  : Double  = 0.0,
    val deductionAmount    : Double  = 0.0,
    val netRefundAmount    : Double  = 0.0,
    val refundMethod       : RefundMethod = RefundMethod.CASH,
    val accountType        : AccountType? = null,
    val accountId          : String? = null,
    val returnReason       : String  = "",
    val notes              : String? = null,
    val returnedAt         : Long
)
