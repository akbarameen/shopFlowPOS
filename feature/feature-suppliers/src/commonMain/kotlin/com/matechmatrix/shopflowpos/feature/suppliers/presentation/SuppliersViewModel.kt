package com.matechmatrix.shopflowpos.feature.suppliers.presentation

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.feature.suppliers.domain.repository.SuppliersRepository
import kotlinx.datetime.Clock

class SuppliersViewModel(private val repo: SuppliersRepository) :
    MviViewModel<SuppliersState, SuppliersIntent, SuppliersEffect>(SuppliersState()) {

    init {
        onIntent(SuppliersIntent.Load)
    }

    override suspend fun handleIntent(intent: SuppliersIntent) {
        when (intent) {
            SuppliersIntent.Load -> load()
            is SuppliersIntent.Search -> setState { copy(searchQuery = intent.q) }
            SuppliersIntent.ShowAddDialog -> setState {
                copy(showFormDialog = true, editingSupplier = null, formName = "", formPhone = "",
                    formEmail = "", formAddress = "", formNotes = "", formBalance = "0", formError = null)
            }
            is SuppliersIntent.ShowEditDialog -> setState {
                val s = intent.s
                copy(showFormDialog = true, editingSupplier = s, formName = s.name,
                    formPhone = s.phone, formEmail = s.email, formAddress = s.address,
                    formNotes = s.notes ?: "", formBalance = s.balance.toString(), formError = null)
            }
            SuppliersIntent.DismissDialog -> setState { copy(showFormDialog = false, editingSupplier = null) }
            SuppliersIntent.SaveSupplier -> save()
            is SuppliersIntent.ConfirmDelete -> setState { copy(showDeleteId = intent.id) }
            SuppliersIntent.DeleteSupplier -> {
                val id = currentState.showDeleteId ?: return
                setState { copy(showDeleteId = null) }
                repo.deleteSupplier(id)
                load()
            }
            is SuppliersIntent.FormName    -> setState { copy(formName = intent.v) }
            is SuppliersIntent.FormPhone   -> setState { copy(formPhone = intent.v) }
            is SuppliersIntent.FormEmail   -> setState { copy(formEmail = intent.v) }
            is SuppliersIntent.FormAddress -> setState { copy(formAddress = intent.v) }
            is SuppliersIntent.FormBalance -> setState { copy(formBalance = intent.v) }
            is SuppliersIntent.FormNotes   -> setState { copy(formNotes = intent.v) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        when (val r = repo.getAllSuppliers()) {
            is AppResult.Success -> setState { copy(isLoading = false, suppliers = r.data, currencySymbol = currency) }
            is AppResult.Error   -> setState { copy(isLoading = false, formError = r.message) }
            else -> setState { copy(isLoading = false) }
        }
    }

    private suspend fun save() {
        val s = currentState
        if (s.formName.isBlank()) { setState { copy(formError = "Name is required") }; return }
        
        val supplier = Supplier(
            id = s.editingSupplier?.id ?: IdGenerator.generate(),
            name = s.formName.trim(),
            phone = s.formPhone.trim(),
            email = s.formEmail.trim(),
            address = s.formAddress.trim(),
            balance = s.formBalance.toDoubleOrNull() ?: 0.0,
            notes = s.formNotes.takeIf { it.isNotBlank() },
            createdAt = s.editingSupplier?.createdAt ?: Clock.System.now().toEpochMilliseconds()
        )
        
        val result = if (s.editingSupplier != null) repo.updateSupplier(supplier) else repo.insertSupplier(supplier)
        when (result) {
            is AppResult.Success -> {
                setState { copy(showFormDialog = false, editingSupplier = null) }
                load()
            }
            is AppResult.Error -> setState { copy(formError = result.message) }
            else -> {}
        }
    }
}
