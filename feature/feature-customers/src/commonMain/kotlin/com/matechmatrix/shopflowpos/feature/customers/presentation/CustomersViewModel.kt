package com.matechmatrix.shopflowpos.feature.customers.presentation

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.feature.customers.domain.repository.CustomersRepository
import kotlinx.datetime.Clock

class CustomersViewModel(private val repo: CustomersRepository) :
    MviViewModel<CustomersState, CustomersIntent, CustomersEffect>(CustomersState()) {

    init {
        onIntent(CustomersIntent.Load)
    }

    override suspend fun handleIntent(intent: CustomersIntent) {
        when (intent) {
            CustomersIntent.Load -> load()
            is CustomersIntent.Search -> setState { copy(searchQuery = intent.q) }
            CustomersIntent.ShowAddDialog -> setState {
                copy(showFormDialog = true, editingCustomer = null, formName = "", formPhone = "",
                    formEmail = "", formAddress = "", formNotes = "", formError = null)
            }
            is CustomersIntent.ShowEditDialog -> setState {
                val c = intent.c
                copy(showFormDialog = true, editingCustomer = c, formName = c.name,
                    formPhone = c.phone, formAddress = c.address, formNotes = c.notes ?: "", formError = null)
            }
            CustomersIntent.DismissDialog -> setState { copy(showFormDialog = false, editingCustomer = null) }
            CustomersIntent.SaveCustomer -> save()
            is CustomersIntent.ConfirmDelete -> setState { copy(showDeleteId = intent.id) }
            CustomersIntent.DeleteCustomer -> {
                val id = currentState.showDeleteId ?: return
                setState { copy(showDeleteId = null) }
                repo.deleteCustomer(id)
                load()
            }
            is CustomersIntent.ShowDueDialog -> setState { copy(showDueDialog = intent.c, dueCollectAmount = "") }
            CustomersIntent.DismissDueDialog -> setState { copy(showDueDialog = null) }
            is CustomersIntent.SetDueAmount -> setState { copy(dueCollectAmount = intent.v) }
            CustomersIntent.CollectDue -> {
                val c = currentState.showDueDialog ?: return
                val amount = currentState.dueCollectAmount.toLongOrNull() ?: return
                repo.collectDue(c.id, amount)
                setState { copy(showDueDialog = null) }
                setEffect(CustomersEffect.Toast("Collected Rs.$amount from ${c.name}"))
                load()
            }
            is CustomersIntent.FormName    -> setState { copy(formName = intent.v) }
            is CustomersIntent.FormPhone   -> setState { copy(formPhone = intent.v) }
            is CustomersIntent.FormEmail   -> setState { copy(formEmail = intent.v) }
            is CustomersIntent.FormAddress -> setState { copy(formAddress = intent.v) }
            is CustomersIntent.FormNotes   -> setState { copy(formNotes = intent.v) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        when (val r = repo.getAllCustomers()) {
            is AppResult.Success -> setState { copy(isLoading = false, customers = r.data, currencySymbol = currency) }
            is AppResult.Error   -> setState { copy(isLoading = false, error = r.message) }
            else -> setState { copy(isLoading = false) }
        }
    }

    private suspend fun save() {
        val s = currentState
        if (s.formName.isBlank()) { setState { copy(formError = "Name is required") }; return }
        val c = Customer(
            id = s.editingCustomer?.id ?: IdGenerator.generate(),
            name = s.formName.trim(),
            phone = s.formPhone.trim(),
            address = s.formAddress.trim(),
            notes = s.formNotes.takeIf { it.isNotBlank() },
            dueBalance = s.editingCustomer?.dueBalance ?: 0.0,
            totalPurchases = s.editingCustomer?.totalPurchases ?: 0.0,
            totalTransactions = s.editingCustomer?.totalTransactions ?: 0,
            createdAt = s.editingCustomer?.createdAt ?: Clock.System.now().toEpochMilliseconds()
        )
        val result = if (s.editingCustomer != null) repo.updateCustomer(c) else repo.insertCustomer(c)
        when (result) {
            is AppResult.Success -> {
                setState { copy(showFormDialog = false, editingCustomer = null) }
                load()
            }
            is AppResult.Error   -> setState { copy(formError = result.message) }
            else -> {}
        }
    }
}
