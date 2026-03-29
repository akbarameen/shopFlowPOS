package com.matechmatrix.shopflowpos.feature.salesreturn.presentation

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.SaleReturn
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.SalesReturnRepository
import kotlinx.datetime.Clock

class SalesReturnViewModel(private val repo: SalesReturnRepository) :
    MviViewModel<SalesReturnState, SalesReturnIntent, SalesReturnEffect>(SalesReturnState()) {

    init {
        onIntent(SalesReturnIntent.Load)
    }

    override suspend fun handleIntent(intent: SalesReturnIntent) {
        when (intent) {
            SalesReturnIntent.Load -> load()
            SalesReturnIntent.ShowAddDialog -> setState {
                copy(
                    showAddDialog = true, searchInvoice = "", foundSale = null,
                    saleSearchError = null, formReason = "", formRefundAmount = "",
                    formRestockProductId = "", formRestockQty = "0", formError = null
                )
            }
            SalesReturnIntent.DismissDialog -> setState { copy(showAddDialog = false) }
            is SalesReturnIntent.SearchInvoice -> setState { copy(searchInvoice = intent.v) }
            SalesReturnIntent.LookupSale -> lookupSale()
            is SalesReturnIntent.FormReason -> setState { copy(formReason = intent.v) }
            is SalesReturnIntent.FormRefund -> setState { copy(formRefundAmount = intent.v) }
            is SalesReturnIntent.FormRestockProductId -> setState { copy(formRestockProductId = intent.v) }
            is SalesReturnIntent.FormRestockQty -> setState { copy(formRestockQty = intent.v) }
            SalesReturnIntent.SaveReturn -> saveReturn()
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        val range = DateTimeUtils.thisMonthRange()

        when (val r = repo.getReturnsByDateRange(range.first, range.second)) {
            is AppResult.Success -> setState {
                copy(
                    isLoading = false,
                    returns = r.data.sortedByDescending { it.returnedAt },
                    currencySymbol = currency
                )
            }
            is AppResult.Error -> setState { copy(isLoading = false, formError = r.message) }
            else -> setState { copy(isLoading = false) }
        }
    }

    private suspend fun lookupSale() {
        val invoice = currentState.searchInvoice.trim()
        if (invoice.isBlank()) {
            setState { copy(saleSearchError = "Enter invoice number") }
            return
        }
        when (val r = repo.getSaleByInvoice(invoice)) {
            is AppResult.Success -> {
                if (r.data == null) {
                    setState { copy(saleSearchError = "Invoice not found", foundSale = null) }
                } else {
                    setState {
                        copy(
                            foundSale = r.data,
                            saleSearchError = null,
                            formRefundAmount = r.data!!.totalAmount.toString(),
                            // Auto-select first item for restock by default
                            formRestockProductId = r.data!!.items.firstOrNull()?.productId ?: ""
                        )
                    }
                }
            }
            is AppResult.Error -> setState { copy(saleSearchError = r.message) }
            else -> {}
        }
    }

    private suspend fun saveReturn() {
        val s = currentState
        val sale = s.foundSale ?: return setState { copy(formError = "Look up a sale first") }

        if (s.formReason.isBlank()) return setState { copy(formError = "Reason is required") }

        val refund = s.formRefundAmount.toDoubleOrNull() ?: 0.0
        val restockQty = s.formRestockQty.toIntOrNull() ?: 0
        val restockProductId = s.formRestockProductId.ifBlank { null }

        // Find product details for the return record
        val selectedItem = sale.items.find { it.productId == restockProductId } ?: sale.items.firstOrNull()

        val saleReturn = SaleReturn(
            id = IdGenerator.generate(),
            originalSaleId = sale.id,
            productId = selectedItem?.productId ?: "unknown",
            productName = selectedItem?.productName ?: "Unknown Product",
            returnedQuantity = restockQty.coerceAtLeast(1),
            originalSellingPrice = selectedItem?.sellingPrice ?: 0.0,
            deductionAmount = 0.0,
            refundAmount = refund,
            returnReason = s.formReason.trim(),
            returnedAt = Clock.System.now().toEpochMilliseconds()
        )

        when (val r = repo.insertReturn(saleReturn, restockProductId, restockQty)) {
            is AppResult.Success -> {
                setState { copy(showAddDialog = false) }
                setEffect(SalesReturnEffect.Toast("Return processed successfully"))
                load()
            }
            is AppResult.Error -> setState { copy(formError = r.message) }
            else -> {}
        }
    }
}