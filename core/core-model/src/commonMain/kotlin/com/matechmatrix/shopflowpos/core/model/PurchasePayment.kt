package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class PurchasePayment(
    val id              : String,
    val purchaseOrderId : String,
    val amount          : Double,
    val accountType     : AccountType,
    val accountId       : String,
    val referenceNumber : String? = null,
    val notes           : String? = null,
    val paidAt          : Long
)