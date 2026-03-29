package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.TransactionType
import kotlinx.serialization.Serializable

@Serializable
data class LedgerEntry(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val accountType: AccountType,
    val bankAccountId: String? = null,
    val referenceId: String? = null,
    val description: String,
    val balanceAfter: Double,
    val createdAt: Long
)