package com.matechmatrix.shopflowpos.feature.salesreturn.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.RefundMethod
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.ReturnItemRequest
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.ReturnRequest
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.usecase.*

class SalesReturnViewModel(
    private val getReturns  : GetReturnsByDateRangeUseCase,
    private val getSettings : GetSalesReturnSettingsUseCase,
    private val lookupSale  : LookupSaleByInvoiceUseCase,
    private val processReturn: ProcessSaleReturnUseCase,
) : MviViewModel<SalesReturnState, SalesReturnIntent, SalesReturnEffect>(SalesReturnState()) {

    init { onIntent(SalesReturnIntent.Load) }

    override suspend fun handleIntent(intent: SalesReturnIntent) {
        when (intent) {
            SalesReturnIntent.Load -> load()

            SalesReturnIntent.ShowAddDialog -> {
                val settings = getSettings()
                setState {
                    copy(
                        showAddDialog    = true,
                        searchInvoice    = "", foundSale = null, foundItems = emptyList(),
                        saleSearchError  = null, formReason = "", formNotes = "",
                        formDeductionPct = settings.defaultDeduction.toString(),
                        formRefundMethod = RefundMethod.CASH,
                        formAccountType  = AccountType.CASH,
                        formAccountId    = settings.cashAccounts.firstOrNull()?.id ?: "default_cash",
                        cashAccounts     = settings.cashAccounts,
                        bankAccounts     = settings.bankAccounts,
                        formError        = null, defaultDeduction = settings.defaultDeduction
                    )
                }
            }

            SalesReturnIntent.DismissDialog ->
                setState { copy(showAddDialog = false, foundSale = null, foundItems = emptyList()) }

            is SalesReturnIntent.SearchInvoice -> setState { copy(searchInvoice = intent.v, saleSearchError = null) }

            SalesReturnIntent.LookupSale -> doLookup()

            is SalesReturnIntent.SetItemQty -> setState {
                copy(foundItems = foundItems.map {
                    if (it.saleItem.id == intent.saleItemId)
                        it.copy(returnedQty = intent.qty.coerceIn(0, it.saleItem.quantity))
                    else it
                })
            }

            is SalesReturnIntent.SetItemRestock -> setState {
                copy(foundItems = foundItems.map {
                    if (it.saleItem.id == intent.saleItemId) it.copy(restockItem = intent.restock) else it
                })
            }

            is SalesReturnIntent.FormReason      -> setState { copy(formReason = intent.v) }
            is SalesReturnIntent.FormNotes        -> setState { copy(formNotes = intent.v) }
            is SalesReturnIntent.FormDeductionPct -> setState { copy(formDeductionPct = intent.v) }
            is SalesReturnIntent.FormRefundMethod -> setState {
                val defaultId = when (intent.v) {
                    RefundMethod.CASH  -> cashAccounts.firstOrNull()?.id ?: "default_cash"
                    RefundMethod.BANK  -> bankAccounts.firstOrNull()?.id ?: ""
                    else               -> ""
                }
                copy(
                    formRefundMethod = intent.v,
                    formAccountType  = when (intent.v) { RefundMethod.BANK -> AccountType.BANK; else -> AccountType.CASH },
                    formAccountId    = defaultId
                )
            }
            is SalesReturnIntent.FormAccountType -> setState { copy(formAccountType = intent.v) }
            is SalesReturnIntent.FormAccountId   -> setState { copy(formAccountId = intent.v) }

            SalesReturnIntent.SaveReturn -> doProcessReturn()
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val settings = getSettings()
        val range    = DateTimeUtils.thisMonthRange()
        when (val r = getReturns(range.first, range.second)) {
            is AppResult.Success -> setState {
                copy(
                    isLoading      = false,
                    returns        = r.data.sortedByDescending { it.returnedAt },
                    currencySymbol = settings.currencySymbol,
                    cashAccounts   = settings.cashAccounts,
                    bankAccounts   = settings.bankAccounts,
                    defaultDeduction = settings.defaultDeduction
                )
            }
            is AppResult.Error -> setState { copy(isLoading = false, error = r.message) }
            else               -> setState { copy(isLoading = false) }
        }
    }

    private suspend fun doLookup() {
        setState { copy(isSearching = true, saleSearchError = null, foundSale = null, foundItems = emptyList()) }
        when (val r = lookupSale(state.value.searchInvoice)) {
            is AppResult.Success -> {
                val (sale, items) = r.data ?: run {
                    setState { copy(isSearching = false, saleSearchError = "Invoice not found") }
                    return
                }
                setState {
                    copy(
                        isSearching   = false,
                        foundSale     = sale,
                        foundItems    = items.map { ReturnItemState(it) },
                        saleSearchError = null
                    )
                }
            }
            is AppResult.Error -> setState { copy(isSearching = false, saleSearchError = r.message) }
            else               -> setState { copy(isSearching = false) }
        }
    }

    private suspend fun doProcessReturn() {
        val s    = state.value
        val sale = s.foundSale ?: return setState { copy(formError = "Look up a sale first") }

        setState { copy(isProcessing = true, formError = null) }

        val request = ReturnRequest(
            originalSaleId   = sale.id,
            originalSale     = sale,
            items            = s.foundItems.filter { it.returnedQty > 0 }.map {
                ReturnItemRequest(it.saleItem, it.returnedQty, it.restockItem)
            },
            returnReason     = s.formReason.trim(),
            notes            = s.formNotes.trim(),
            refundMethod     = s.formRefundMethod,
            accountType      = if (s.formRefundMethod == RefundMethod.STORE_CREDIT) null else s.formAccountType,
            accountId        = if (s.formRefundMethod == RefundMethod.STORE_CREDIT) null else s.formAccountId,
            deductionPercent = s.formDeductionPct.toDoubleOrNull() ?: 0.0,
            customerId       = sale.customerId,
            customerName     = sale.customerName
        )

        when (val r = processReturn(request)) {
            is AppResult.Success -> {
                setState { copy(isProcessing = false, showAddDialog = false) }
                setEffect(SalesReturnEffect.ShowToast("Return ${r.data.returnNumber} processed — Refund: ${s.currencySymbol} ${s.netRefund.toLong()}"))
                load()
            }
            is AppResult.Error -> setState { copy(isProcessing = false, formError = r.message) }
            else               -> setState { copy(isProcessing = false) }
        }
    }
}