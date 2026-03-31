package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class RepairPayment(
    val id          : String,
    val jobId       : String,
    val amount      : Double,
    val accountType : AccountType,
    val accountId   : String,
    val notes       : String? = null,
    val paidAt      : Long
)