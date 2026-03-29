package com.matechmatrix.shopflowpos.feature.installments.presentation

import InstallmentsEffect
import InstallmentsIntent
import InstallmentsState
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.Installment
import com.matechmatrix.shopflowpos.feature.installments.domain.repository.InstallmentsRepository
import kotlinx.datetime.Clock


class InstallmentsViewModel(private val repo: InstallmentsRepository) :
    MviViewModel<InstallmentsState, InstallmentsIntent, InstallmentsEffect>(InstallmentsState()) {

    init { onIntent(InstallmentsIntent.Load) }

    override suspend fun handleIntent(intent: InstallmentsIntent) {
        when (intent) {
            InstallmentsIntent.Load -> load()
            InstallmentsIntent.ShowAddDialog -> setState {
                copy(showAddDialog = true, formCustomerName = "", formDescription = "",
                    formTotalAmount = "", formInstallmentCount = "3", formNotes = "", formError = null)
            }
            InstallmentsIntent.DismissDialog -> setState { copy(showAddDialog = false) }
            InstallmentsIntent.SaveInstallment -> save()
            is InstallmentsIntent.ShowPaymentDialog -> setState { copy(showPaymentDialog = intent.i, paymentAmount = "") }
            InstallmentsIntent.DismissPaymentDialog -> setState { copy(showPaymentDialog = null) }
            is InstallmentsIntent.SetPaymentAmount -> setState { copy(paymentAmount = intent.v) }
            // In handleIntent for RecordPayment
            InstallmentsIntent.RecordPayment -> {
                val i = state.value.showPaymentDialog ?: return
                val nextDate = Clock.System.now().toEpochMilliseconds() + 2592000000L // +30 days

                repo.recordPayment(i.id, nextDate)
                setState { copy(showPaymentDialog = null) }
                setEffect(InstallmentsEffect.Toast("Payment recorded"))
                load()
            }

//            InstallmentsIntent.RecordPayment -> {
//                val i = state.value.showPaymentDialog ?: return
//                val amount = state.value.paymentAmount.toLongOrNull() ?: return
//                repo.recordPayment(i.id, amount)
//                setState { copy(showPaymentDialog = null) }
//                setEffect(InstallmentsEffect.Toast("Payment of Rs.$amount recorded"))
//                load()
//            }
            is InstallmentsIntent.DeleteInstallment -> { repo.deleteInstallment(intent.id); load() }
            is InstallmentsIntent.FormCustomerName    -> setState { copy(formCustomerName = intent.v) }
            is InstallmentsIntent.FormDescription     -> setState { copy(formDescription = intent.v) }
            is InstallmentsIntent.FormTotalAmount     -> setState { copy(formTotalAmount = intent.v) }
            is InstallmentsIntent.FormInstallmentCount -> setState { copy(formInstallmentCount = intent.v) }
            is InstallmentsIntent.FormNotes           -> setState { copy(formNotes = intent.v) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        val list = (repo.getAllInstallments() as? AppResult.Success)?.data ?: emptyList()
        setState { copy(isLoading = false, installments = list.sortedByDescending { it.createdAt }, currencySymbol = currency) }
    }

    private suspend fun save() {
        val s = state.value
        val total = s.formTotalAmount.toDoubleOrNull() ?: 0.0
        val months = s.formInstallmentCount.toIntOrNull() ?: 1

        if (s.formCustomerName.isBlank()) { setState { copy(formError = "Customer name required") }; return }
        if (total <= 0) { setState { copy(formError = "Enter valid total amount") }; return }

        val now = Clock.System.now().toEpochMilliseconds()
        // Simple calculation: Adjust based on your business logic for down payments
        val monthly = total / months

        val installment = Installment(
            id = IdGenerator.generate(),
            customerId = "CUST-${IdGenerator.generate().take(5)}",
            customerName = s.formCustomerName.trim(),
            productId = "PROD-VAR", // Or from state if product is selected
            productName = s.formDescription.trim(),
            totalAmount = total,
            downPayment = 0.0,
            remainingAmount = total,
            monthlyAmount = monthly,
            totalMonths = months,
            paidMonths = 0,
            startDate = now,
            nextDueDate = now + 2592000000L, // Default to +30 days
            isCompleted = false,
            createdAt = now
        )

        when (val r = repo.insertInstallment(installment)) {
            is AppResult.Success -> {
                setState { copy(showAddDialog = false) }
                load()
            }
            is AppResult.Error -> {
                setState { copy(formError = r.message) }
            }
            is AppResult.Loading -> {
                // Usually, you might set a loading state in the form
                setState { copy(isLoading = true) }
            }
        }
    }


}