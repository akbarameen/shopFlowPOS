package com.matechmatrix.shopflowpos.feature.salesreturn.presentation

import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.SaleReturn


data class SalesReturnState(
    val isLoading: Boolean = true,
    val returns: List<SaleReturn> = emptyList(),
    val currencySymbol: String = "Rs.",
    val showAddDialog: Boolean = false,
    val searchInvoice: String = "",
    val foundSale: Sale? = null,
    val saleSearchError: String? = null,
    val formReason: String = "",
    val formRefundAmount: String = "",
    val formRestockProductId: String = "",
    val formRestockQty: String = "0",
    val formError: String? = null
) { val totalReturns get() = returns.sumOf { it.refundAmount } }

sealed class SalesReturnIntent {
    data object Load : SalesReturnIntent()
    data object ShowAddDialog : SalesReturnIntent()
    data object DismissDialog : SalesReturnIntent()
    data class SearchInvoice(val v: String) : SalesReturnIntent()
    data object LookupSale : SalesReturnIntent()
    data class FormReason(val v: String) : SalesReturnIntent()
    data class FormRefund(val v: String) : SalesReturnIntent()
    data class FormRestockProductId(val v: String) : SalesReturnIntent()
    data class FormRestockQty(val v: String) : SalesReturnIntent()
    data object SaveReturn : SalesReturnIntent()
}

sealed class SalesReturnEffect { data class Toast(val msg: String) : SalesReturnEffect() }
