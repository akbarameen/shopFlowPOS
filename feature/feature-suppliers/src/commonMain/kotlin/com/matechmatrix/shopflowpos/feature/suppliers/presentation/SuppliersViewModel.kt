package com.matechmatrix.shopflowpos.feature.suppliers.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.feature.suppliers.domain.repository.SuppliersRepository
import kotlinx.datetime.Clock

class SuppliersViewModel(
    private val repo: SuppliersRepository
) : MviViewModel<SuppliersState, SuppliersIntent, SuppliersEffect>(SuppliersState()) {

    init { onIntent(SuppliersIntent.Load) }

    override suspend fun handleIntent(intent: SuppliersIntent) {
        when (intent) {
            SuppliersIntent.Load         -> load()
            is SuppliersIntent.Search    -> setState { copy(searchQuery = intent.q) }

            // ── Sheet ─────────────────────────────────────────────────────────
            SuppliersIntent.ShowAddSheet -> setState {
                copy(
                    showFormSheet        = true,
                    editingSupplier      = null,
                    formName             = "", formPhone       = "", formWhatsapp = "",
                    formEmail            = "", formAddress     = "", formCity     = "",
                    formNtn              = "", formOpeningBalance = "0", formNotes = "",
                    formError            = null,
                )
            }

            is SuppliersIntent.ShowEditSheet -> {
                val s = intent.s
                setState {
                    copy(
                        showFormSheet        = true,
                        editingSupplier      = s,
                        formName             = s.name,
                        formPhone            = s.phone,
                        formWhatsapp         = s.whatsapp ?: "",
                        formEmail            = s.email,
                        formAddress          = s.address,
                        formCity             = s.city,
                        formNtn              = s.ntn ?: "",
                        formOpeningBalance   = s.openingBalance.toString(),
                        formNotes            = s.notes ?: "",
                        formError            = null,
                    )
                }
            }

            SuppliersIntent.DismissSheet -> setState {
                copy(showFormSheet = false, editingSupplier = null, formError = null)
            }

            SuppliersIntent.SaveSupplier -> save()

            // ── Delete ────────────────────────────────────────────────────────
            is SuppliersIntent.ConfirmDelete ->
                setState { copy(showDeleteId = intent.id.ifBlank { null }) }

            SuppliersIntent.DeleteSupplier -> {
                val id = state.value.showDeleteId ?: return
                setState { copy(showDeleteId = null) }
                when (val r = repo.softDeleteSupplier(id)) {
                    is AppResult.Success -> {
                        setEffect(SuppliersEffect.Toast("Supplier removed"))
                        load()
                    }
                    is AppResult.Error -> setEffect(SuppliersEffect.Toast(r.message))
                    else -> {}
                }
            }

            // ── Form fields ───────────────────────────────────────────────────
            is SuppliersIntent.FormName           -> setState { copy(formName           = intent.v) }
            is SuppliersIntent.FormPhone          -> setState { copy(formPhone          = intent.v) }
            is SuppliersIntent.FormWhatsapp       -> setState { copy(formWhatsapp       = intent.v) }
            is SuppliersIntent.FormEmail          -> setState { copy(formEmail          = intent.v) }
            is SuppliersIntent.FormAddress        -> setState { copy(formAddress        = intent.v) }
            is SuppliersIntent.FormCity           -> setState { copy(formCity           = intent.v) }
            is SuppliersIntent.FormNtn            -> setState { copy(formNtn            = intent.v) }
            is SuppliersIntent.FormOpeningBalance -> setState { copy(formOpeningBalance = intent.v) }
            is SuppliersIntent.FormNotes          -> setState { copy(formNotes          = intent.v) }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        when (val r = repo.getAllSuppliers()) {
            is AppResult.Success -> setState {
                copy(isLoading = false, suppliers = r.data, currencySymbol = currency)
            }
            is AppResult.Error   -> setState {
                copy(isLoading = false)
            }.also { setEffect(SuppliersEffect.Toast(r.message)) }
            else                 -> setState { copy(isLoading = false) }
        }
    }

    private suspend fun save() {
        val s = state.value
        if (s.formName.isBlank()) {
            setState { copy(formError = "Supplier name is required") }
            return
        }
        setState { copy(isSaving = true, formError = null) }

        val isNew    = s.editingSupplier == null
        val now      = Clock.System.now().toEpochMilliseconds()
        val supplier = Supplier(
            id             = s.editingSupplier?.id ?: IdGenerator.generate(),
            name           = s.formName.trim(),
            phone          = s.formPhone.trim(),
            whatsapp       = s.formWhatsapp.takeIf { it.isNotBlank() },
            email          = s.formEmail.trim(),
            address        = s.formAddress.trim(),
            city           = s.formCity.trim(),
            ntn            = s.formNtn.takeIf { it.isNotBlank() },
            openingBalance = s.formOpeningBalance.toDoubleOrNull() ?: 0.0,
            // outstandingBalance kept as-is when editing; set to opening on new
            outstandingBalance = if (isNew) s.formOpeningBalance.toDoubleOrNull() ?: 0.0
            else s.editingSupplier!!.outstandingBalance,
            totalPurchased = s.editingSupplier?.totalPurchased ?: 0.0,
            notes          = s.formNotes.takeIf { it.isNotBlank() },
            isActive       = true,
            createdAt      = s.editingSupplier?.createdAt ?: now,
            updatedAt      = now,
        )

        val result = if (isNew) repo.insertSupplier(supplier) else repo.updateSupplier(supplier)
        when (result) {
            is AppResult.Success -> {
                setState { copy(isSaving = false, showFormSheet = false, editingSupplier = null) }
                setEffect(SuppliersEffect.Toast(if (isNew) "Supplier added" else "Supplier updated"))
                load()
            }
            is AppResult.Error -> setState { copy(isSaving = false, formError = result.message) }
            else               -> setState { copy(isSaving = false) }
        }
    }
}