package com.matechmatrix.shopflowpos.feature.suppliers.presentation

import com.matechmatrix.shopflowpos.core.model.Supplier

// ─── State ────────────────────────────────────────────────────────────────────

data class SuppliersState(
    val isLoading      : Boolean      = true,
    val suppliers      : List<Supplier> = emptyList(),
    val searchQuery    : String       = "",
    val currencySymbol : String       = "Rs.",

    // Sheet
    val showFormSheet  : Boolean      = false,
    val editingSupplier: Supplier?    = null,
    val showDeleteId   : String?      = null,

    // ── Form fields ───────────────────────────────────────────────────────────
    val formName           : String = "",
    val formPhone          : String = "",
    val formWhatsapp       : String = "",
    val formEmail          : String = "",
    val formAddress        : String = "",
    val formCity           : String = "",
    val formNtn            : String = "",       // National Tax Number
    val formOpeningBalance : String = "0",      // amount owed at setup time
    val formNotes          : String = "",
    val formError          : String? = null,
    val isSaving           : Boolean = false,
) {
    val filtered: List<Supplier>
        get() = if (searchQuery.isBlank()) suppliers
        else suppliers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.phone.contains(searchQuery, ignoreCase = true) ||
                    (it.whatsapp?.contains(searchQuery, ignoreCase = true) == true)
        }
}

// ─── Intent ───────────────────────────────────────────────────────────────────

sealed class SuppliersIntent {
    data object Load                                      : SuppliersIntent()
    data class  Search(val q: String)                     : SuppliersIntent()
    data object ShowAddSheet                              : SuppliersIntent()
    data class  ShowEditSheet(val s: Supplier)            : SuppliersIntent()
    data object DismissSheet                              : SuppliersIntent()
    data object SaveSupplier                              : SuppliersIntent()
    data class  ConfirmDelete(val id: String)             : SuppliersIntent()
    data object DeleteSupplier                            : SuppliersIntent()

    // Form fields
    data class FormName          (val v: String) : SuppliersIntent()
    data class FormPhone         (val v: String) : SuppliersIntent()
    data class FormWhatsapp      (val v: String) : SuppliersIntent()
    data class FormEmail         (val v: String) : SuppliersIntent()
    data class FormAddress       (val v: String) : SuppliersIntent()
    data class FormCity          (val v: String) : SuppliersIntent()
    data class FormNtn           (val v: String) : SuppliersIntent()
    data class FormOpeningBalance(val v: String) : SuppliersIntent()
    data class FormNotes         (val v: String) : SuppliersIntent()
}

// ─── Effect ───────────────────────────────────────────────────────────────────

sealed class SuppliersEffect {
    data class Toast(val msg: String) : SuppliersEffect()
}