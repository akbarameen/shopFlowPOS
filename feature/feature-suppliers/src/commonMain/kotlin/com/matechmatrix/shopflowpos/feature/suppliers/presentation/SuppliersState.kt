package com.matechmatrix.shopflowpos.feature.suppliers.presentation

import com.matechmatrix.shopflowpos.core.model.Supplier

// ── State ──────────────────────────────────────────────────────────────────
data class SuppliersState(
    val isLoading: Boolean = true,
    val suppliers: List<com.matechmatrix.shopflowpos.core.model.Supplier> = emptyList(),
    val searchQuery: String = "",
    val currencySymbol: String = "Rs.",
    val showFormDialog: Boolean = false,
    val editingSupplier: com.matechmatrix.shopflowpos.core.model.Supplier? = null,
    val showDeleteId: String? = null,
    val formName: String = "",
    val formPhone: String = "",
    val formEmail: String = "",
    val formAddress: String = "",
    val formBalance: String = "0",
    val formNotes: String = "",
    val formError: String? = null
) {
    val filtered get() = suppliers.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.phone?.contains(searchQuery) == true
    }
}

sealed class SuppliersIntent {
    data object Load : SuppliersIntent()
    data class Search(val q: String) : SuppliersIntent()
    data object ShowAddDialog : SuppliersIntent()
    data class ShowEditDialog(val s: Supplier) : SuppliersIntent()
    data object DismissDialog : SuppliersIntent()
    data object SaveSupplier : SuppliersIntent()
    data class ConfirmDelete(val id: String) : SuppliersIntent()
    data object DeleteSupplier : SuppliersIntent()
    data class FormName(val v: String) : SuppliersIntent()
    data class FormPhone(val v: String) : SuppliersIntent()
    data class FormEmail(val v: String) : SuppliersIntent()
    data class FormAddress(val v: String) : SuppliersIntent()
    data class FormBalance(val v: String) : SuppliersIntent()
    data class FormNotes(val v: String) : SuppliersIntent()
}

sealed class SuppliersEffect { data class Toast(val msg: String) : SuppliersEffect() }
