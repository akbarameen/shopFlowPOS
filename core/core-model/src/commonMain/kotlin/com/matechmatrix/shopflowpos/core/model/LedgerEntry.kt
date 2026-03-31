package com.matechmatrix.shopflowpos.core.model

import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.LedgerEntryType
import com.matechmatrix.shopflowpos.core.model.enums.LedgerReferenceType
import kotlinx.serialization.Serializable

@Serializable
data class LedgerEntry(
    val id            : String,
    val accountType   : AccountType,
    val accountId     : String,
    val entryType     : LedgerEntryType,       // CREDIT | DEBIT
    val referenceType : LedgerReferenceType,
    val referenceId   : String? = null,
    val amount        : Double,
    val balanceAfter  : Double,
    val description   : String,
    val createdAt     : Long
)
