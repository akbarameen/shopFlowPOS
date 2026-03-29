package com.matechmatrix.shopflowpos.feature.expenses.presentation

import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.Expense
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory

enum class ExpDateFilter { TODAY, WEEK, MONTH }

data class ExpensesState(
    val isLoading: Boolean = true,
    val expenses: List<Expense> = emptyList(),
    val categoryTotals: Map<ExpenseCategory, Double> = emptyMap(),
    val dateFilter: ExpDateFilter = ExpDateFilter.TODAY,
    val currencySymbol: String = "Rs.",
    val showAddDialog: Boolean = false,
    val formCategory: ExpenseCategory = ExpenseCategory.OTHER,
    val formAmount: String = "",
    val formDescription: String = "",
    val formAccountType: AccountType = AccountType.CASH,
    val formBankAccountId: String? = null,
    val bankAccounts: List<BankAccount> = emptyList(),
    val cashBalance: Double = 0.0,
    val formError: String? = null,
    val error: String? = null
) {
    val totalExpenses get() = expenses.sumOf { it.amount }
}

sealed class ExpensesIntent {
    data object Load : ExpensesIntent()
    data class SetFilter(val f: ExpDateFilter) : ExpensesIntent()
    data object ShowAddDialog : ExpensesIntent()
    data object DismissDialog : ExpensesIntent()
    data object SaveExpense : ExpensesIntent()
    data class DeleteExpense(val id: String) : ExpensesIntent()
    data class FormCategory(val v: ExpenseCategory) : ExpensesIntent()
    data class FormAmount(val v: String) : ExpensesIntent()
    data class FormDescription(val v: String) : ExpensesIntent()
    data class FormAccountType(val v: AccountType) : ExpensesIntent()
    data class FormBankAccountId(val v: String?) : ExpensesIntent()
}

sealed class ExpensesEffect { data class Toast(val msg: String) : ExpensesEffect() }
