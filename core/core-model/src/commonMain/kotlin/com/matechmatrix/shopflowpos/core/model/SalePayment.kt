package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class SalePayment(
    val id              : String,
    val saleId          : String,
    val amount          : Double,
    val accountType     : AccountType,
    val accountId       : String,
    val referenceNumber : String? = null,
    val notes           : String? = null,
    val paidAt          : Long
)
