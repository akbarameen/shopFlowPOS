package com.matechmatrix.shopflowpos.feature.salesreturn.presentation

import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.RefundMethod

data class ReturnItemState(
    val saleItem   : SaleItem,
    val returnedQty: Int     = 0,
    val restockItem: Boolean = true
)

data class SalesReturnState(
    val isLoading        : Boolean          = true,
    val returns          : List<SaleReturn> = emptyList(),
    val currencySymbol   : String           = "Rs.",
    val cashAccounts     : List<CashAccount>  = emptyList(),
    val bankAccounts     : List<BankAccount>  = emptyList(),
    val defaultDeduction : Double           = 0.0,
    val error            : String?          = null,

    // Add return dialog
    val showAddDialog    : Boolean          = false,
    val searchInvoice    : String           = "",
    val isSearching      : Boolean          = false,
    val foundSale        : Sale?            = null,
    val foundItems       : List<ReturnItemState> = emptyList(),
    val saleSearchError  : String?          = null,

    // Return form
    val formReason       : String           = "",
    val formNotes        : String           = "",
    val formDeductionPct : String           = "0",
    val formRefundMethod : RefundMethod     = RefundMethod.CASH,
    val formAccountType  : AccountType      = AccountType.CASH,
    val formAccountId    : String           = "default_cash",
    val formError        : String?          = null,
    val isProcessing     : Boolean          = false,
) {
    val totalReturns    : Double get() = returns.sumOf { it.netRefundAmount }
    val grossRefund     : Double get() = foundItems.sumOf { it.saleItem.unitPrice * it.returnedQty }
    val deductionAmount : Double get() = grossRefund * ((formDeductionPct.toDoubleOrNull() ?: 0.0) / 100.0)
    val netRefund       : Double get() = (grossRefund - deductionAmount).coerceAtLeast(0.0)
    val hasSelectedItems: Boolean get() = foundItems.any { it.returnedQty > 0 }
}

sealed class SalesReturnIntent {
    data object Load                                           : SalesReturnIntent()
    data object ShowAddDialog                                  : SalesReturnIntent()
    data object DismissDialog                                  : SalesReturnIntent()

    data class  SearchInvoice(val v: String)                   : SalesReturnIntent()
    data object LookupSale                                     : SalesReturnIntent()
    data class  SetItemQty(val saleItemId: String, val qty: Int): SalesReturnIntent()
    data class  SetItemRestock(val saleItemId: String, val restock: Boolean) : SalesReturnIntent()

    data class  FormReason(val v: String)                      : SalesReturnIntent()
    data class  FormNotes(val v: String)                       : SalesReturnIntent()
    data class  FormDeductionPct(val v: String)                : SalesReturnIntent()
    data class  FormRefundMethod(val v: RefundMethod)          : SalesReturnIntent()
    data class  FormAccountType(val v: AccountType)            : SalesReturnIntent()
    data class  FormAccountId(val v: String)                   : SalesReturnIntent()
    data object SaveReturn                                     : SalesReturnIntent()
}

sealed class SalesReturnEffect {
    data class ShowToast(val message: String) : SalesReturnEffect()
}