package com.matechmatrix.shopflowpos.feature.customers.presentation

import com.matechmatrix.shopflowpos.core.model.Customer

data class CustomersState(
    val isLoading: Boolean = true,
    val customers: List<Customer> = emptyList(),
    val searchQuery: String = "",
    val currencySymbol: String = "Rs.",
    val showFormDialog: Boolean = false,
    val editingCustomer: Customer? = null,
    val showDueDialog: Customer? = null,
    val dueCollectAmount: String = "",
    val showDeleteId: String? = null,
    val formName: String = "",
    val formPhone: String = "",
    val formEmail: String = "",
    val formAddress: String = "",
    val formNotes: String = "",
    val formError: String? = null,
    val error: String? = null
) {
    val filtered get() = customers.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, true) || it.phone?.contains(searchQuery) == true
    }
    val totalDue get() = customers.sumOf { it.dueBalance }
}

sealed class CustomersIntent {
    data object Load : CustomersIntent()
    data class Search(val q: String) : CustomersIntent()
    data object ShowAddDialog : CustomersIntent()
    data class ShowEditDialog(val c: Customer) : CustomersIntent()
    data object DismissDialog : CustomersIntent()
    data object SaveCustomer : CustomersIntent()
    data class ConfirmDelete(val id: String) : CustomersIntent()
    data object DeleteCustomer : CustomersIntent()
    data class ShowDueDialog(val c: Customer) : CustomersIntent()
    data object DismissDueDialog : CustomersIntent()
    data class SetDueAmount(val v: String) : CustomersIntent()
    data object CollectDue : CustomersIntent()
    data class FormName(val v: String) : CustomersIntent()
    data class FormPhone(val v: String) : CustomersIntent()
    data class FormEmail(val v: String) : CustomersIntent()
    data class FormAddress(val v: String) : CustomersIntent()
    data class FormNotes(val v: String) : CustomersIntent()
}

sealed class CustomersEffect { data class Toast(val msg: String) : CustomersEffect() }