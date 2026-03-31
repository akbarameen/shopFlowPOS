package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class InstallmentPayment(
    val id          : String,
    val planId      : String,
    val amount      : Double,
    val accountType : AccountType,
    val accountId   : String,
    val notes       : String? = null,
    val paidAt      : Long
)