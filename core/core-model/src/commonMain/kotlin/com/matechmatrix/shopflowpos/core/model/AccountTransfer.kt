package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class AccountTransfer(
    val id              : String,
    val fromAccountType : AccountType,
    val fromAccountId   : String,
    val toAccountType   : AccountType,
    val toAccountId     : String,
    val amount          : Double,
    val notes           : String? = null,
    val transferredAt   : Long
)