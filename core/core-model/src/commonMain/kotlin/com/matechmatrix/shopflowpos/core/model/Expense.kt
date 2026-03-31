package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class Expense(
    val id          : String,
    val category    : ExpenseCategory,
    val title       : String,
    val amount      : Double,
    val accountType : AccountType = AccountType.CASH,
    val accountId   : String      = "default_cash",
    val receiptRef  : String?     = null,
    val notes       : String?     = null,
    val createdAt   : Long
)