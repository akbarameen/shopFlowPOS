package com.matechmatrix.shopflowpos.feature.dues.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.dues.domain.usecase.*
import kotlin.math.roundToLong

class DuesViewModel(
    private val loadPage        : LoadDuesPageUseCase,
    private val collectSaleDue  : CollectSaleDueUseCase,
    private val payPurchaseDue  : PayPurchaseDueFromDuesUseCase
) : MviViewModel<DuesState, DuesIntent, DuesEffect>(DuesState()) {

    init { onIntent(DuesIntent.Load) }

    override suspend fun handleIntent(intent: DuesIntent) {
        when (intent) {
            DuesIntent.Load -> loadAll()

            is DuesIntent.SwitchTab -> setState { copy(activeTab = intent.tab) }

            is DuesIntent.FilterByCustomer -> setState {
                copy(selectedCustomerId = if (intent.customerId == selectedCustomerId) null else intent.customerId)
            }
            is DuesIntent.FilterBySupplier -> setState {
                copy(selectedSupplierId = if (intent.supplierId == selectedSupplierId) null else intent.supplierId)
            }

            is DuesIntent.ShowCollectDialog -> {
                val defaultCash = state.value.cashAccounts.firstOrNull()?.id ?: "default_cash"
                setState {
                    copy(payDialog = PayDialogState(
                        type = DuesTab.RECEIVABLE, referenceId = intent.sale.id,
                        partyName = intent.sale.customerName, invoiceRef = intent.sale.invoiceNumber,
                        dueAmount = intent.sale.dueAmount, amount = intent.sale.dueAmount.roundToLong().toString(),
                        accountType = AccountType.CASH, accountId = defaultCash
                    ))
                }
            }

            is DuesIntent.ShowPayDialog -> {
                val defaultCash = state.value.cashAccounts.firstOrNull()?.id ?: "default_cash"
                setState {
                    copy(payDialog = PayDialogState(
                        type = DuesTab.PAYABLE, referenceId = intent.order.id,
                        partyName = intent.order.supplierName, invoiceRef = intent.order.poNumber,
                        dueAmount = intent.order.dueAmount, amount = intent.order.dueAmount.roundToLong().toString(),
                        accountType = AccountType.CASH, accountId = defaultCash
                    ))
                }
            }

            DuesIntent.DismissPayDialog -> setState { copy(payDialog = null) }

            is DuesIntent.SetAmount -> setState { copy(payDialog = payDialog?.copy(amount = intent.v, error = null)) }

            is DuesIntent.SetAccountType -> setState {
                val id = when (intent.v) {
                    AccountType.CASH -> cashAccounts.firstOrNull()?.id ?: "default_cash"
                    AccountType.BANK -> bankAccounts.firstOrNull()?.id ?: ""
                    else             -> ""
                }
                copy(payDialog = payDialog?.copy(accountType = intent.v, accountId = id))
            }

            is DuesIntent.SetAccountId -> setState { copy(payDialog = payDialog?.copy(accountId = intent.v)) }

            DuesIntent.ConfirmPayment -> doConfirmPayment()
        }
    }

    private suspend fun loadAll() {
        setState { copy(isLoading = true) }
        val data = loadPage()
        setState {
            copy(
                isLoading             = false,
                customersWithDues     = data.customersWithDues,
                salesWithDue          = data.salesWithDue,
                suppliersWithDues     = data.suppliersWithDues,
                purchaseOrdersWithDue = data.purchaseOrdersWithDue,
                cashAccounts          = data.cashAccounts,
                bankAccounts          = data.bankAccounts,
                currencySymbol        = data.currencySymbol
            )
        }
    }

    private suspend fun doConfirmPayment() {
        val s      = state.value
        val dialog = s.payDialog ?: return
        val amount = dialog.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            setState { copy(payDialog = payDialog?.copy(error = "Enter a valid amount")) }; return
        }

        // Balance validation (client side — DB also validates via rollback)
        when (dialog.accountType) {
            AccountType.CASH -> {
                val acc = s.cashAccounts.find { it.id == dialog.accountId }
                if (dialog.type == DuesTab.PAYABLE && acc != null && amount > acc.balance) {
                    setState { copy(payDialog = payDialog?.copy(error = "Insufficient cash (${acc.balance.toLong()} available)")) }; return
                }
            }
            AccountType.BANK -> {
                val acc = s.bankAccounts.find { it.id == dialog.accountId }
                if (dialog.type == DuesTab.PAYABLE && acc != null && amount > acc.balance) {
                    setState { copy(payDialog = payDialog?.copy(error = "Insufficient bank balance (${acc.balance.toLong()} available)")) }; return
                }
            }
            else -> {}
        }

        setState { copy(isProcessing = true) }

        val result: AppResult<Unit> = if (dialog.type == DuesTab.RECEIVABLE) {
            val sale = s.salesWithDue.find { it.id == dialog.referenceId }
                ?: run { setState { copy(isProcessing = false, payDialog = payDialog?.copy(error = "Sale not found")) }; return }
            collectSaleDue(sale, amount, dialog.accountType, dialog.accountId)
        } else {
            val order = s.purchaseOrdersWithDue.find { it.id == dialog.referenceId }
                ?: run { setState { copy(isProcessing = false, payDialog = payDialog?.copy(error = "Order not found")) }; return }
            payPurchaseDue(order, amount, dialog.accountType, dialog.accountId)
        }

        when (result) {
            is AppResult.Success -> {
                val msg = if (dialog.type == DuesTab.RECEIVABLE)
                    "Collected ${s.currencySymbol} ${amount.toLong()} from ${dialog.partyName}"
                else
                    "Paid ${s.currencySymbol} ${amount.toLong()} to ${dialog.partyName}"
                setState { copy(isProcessing = false, payDialog = null) }
                setEffect(DuesEffect.ShowToast(msg))
                loadAll()
            }
            is AppResult.Error -> setState { copy(isProcessing = false, payDialog = payDialog?.copy(error = result.message)) }
            else               -> setState { copy(isProcessing = false) }
        }
    }
}
