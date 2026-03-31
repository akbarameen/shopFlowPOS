package com.matechmatrix.shopflowpos.feature.customers.presentation

import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.CashAccount
import com.matechmatrix.shopflowpos.core.model.BankAccount
// ─── State ───────────────────────────────────────────────────────────────────

data class CustomersState(
    // List
    val isLoading        : Boolean      = true,
    val customers        : List<Customer> = emptyList(),
    val searchQuery      : String       = "",
    val currencySymbol   : String       = "Rs.",
    val error            : String?      = null,

    // Available accounts for due collection
    val cashAccounts     : List<CashAccount>  = emptyList(),
    val bankAccounts     : List<BankAccount>  = emptyList(),

    // Add / Edit dialog
    val showFormDialog   : Boolean      = false,
    val editingCustomer  : Customer?    = null,

    // Form fields
    val formName         : String       = "",
    val formPhone        : String       = "",
    val formWhatsapp     : String       = "",
    val formEmail        : String       = "",
    val formCnic         : String       = "",
    val formAddress      : String       = "",
    val formCity         : String       = "",
    val formCreditLimit  : String       = "",
    val formNotes        : String       = "",
    val formError        : String?      = null,

    // Due collection dialog
    val showDueDialog    : Customer?    = null,
    val dueCollectAmount : String       = "",
    val dueAccountType   : AccountType  = AccountType.CASH,
    val dueAccountId     : String       = "default_cash",

    // Delete confirmation
    val showDeleteId     : String?      = null,
    val isSaving         : Boolean      = false,
) {
    /** Live-filtered list driven by search query (no extra DB round-trip). */
    val filtered: List<Customer>
        get() = if (searchQuery.isBlank()) customers
        else customers.filter { c ->
            c.name.contains(searchQuery, ignoreCase = true) ||
                    c.phone.contains(searchQuery) ||
                    c.cnic?.contains(searchQuery) == true ||
                    c.email?.contains(searchQuery, ignoreCase = true) == true
        }

    val totalOutstanding: Double get() = customers.sumOf { it.outstandingBalance }
    val selectedCashAccount: CashAccount? get() = cashAccounts.find { it.id == dueAccountId }
    val selectedBankAccount: BankAccount? get() = bankAccounts.find { it.id == dueAccountId }
}

// ─── Intent ──────────────────────────────────────────────────────────────────

sealed class CustomersIntent {
    data object Load                                      : CustomersIntent()
    data class  Search(val q: String)                     : CustomersIntent()

    // Form
    data object ShowAddDialog                             : CustomersIntent()
    data class  ShowEditDialog(val customer: Customer)    : CustomersIntent()
    data object DismissDialog                             : CustomersIntent()
    data object SaveCustomer                              : CustomersIntent()

    // Delete
    data class  ConfirmDelete(val id: String)             : CustomersIntent()
    data object DeleteCustomer                            : CustomersIntent()

    // Due collection
    data class  ShowDueDialog(val customer: Customer)     : CustomersIntent()
    data object DismissDueDialog                          : CustomersIntent()
    data class  SetDueAmount(val v: String)               : CustomersIntent()
    data class  SetDueAccountType(val v: AccountType)     : CustomersIntent()
    data class  SetDueAccountId(val v: String)            : CustomersIntent()
    data object CollectDue                                : CustomersIntent()

    // Form field intents
    data class FormName       (val v: String) : CustomersIntent()
    data class FormPhone      (val v: String) : CustomersIntent()
    data class FormWhatsapp   (val v: String) : CustomersIntent()
    data class FormEmail      (val v: String) : CustomersIntent()
    data class FormCnic       (val v: String) : CustomersIntent()
    data class FormAddress    (val v: String) : CustomersIntent()
    data class FormCity       (val v: String) : CustomersIntent()
    data class FormCreditLimit(val v: String) : CustomersIntent()
    data class FormNotes      (val v: String) : CustomersIntent()
}

// ─── Effect ───────────────────────────────────────────────────────────────────

sealed class CustomersEffect {
    data class ShowToast(val message: String) : CustomersEffect()
}