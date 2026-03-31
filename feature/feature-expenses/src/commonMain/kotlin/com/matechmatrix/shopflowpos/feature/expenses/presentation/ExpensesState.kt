package com.matechmatrix.shopflowpos.feature.expenses.presentation

import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.Expense
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory

import kotlinx.datetime.*
import kotlin.time.ExperimentalTime

// ─── Date filter ─────────────────────────────────────────────────────────────

enum class DateFilter(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
}

fun DateFilter.toEpochRange(): Pair<Long, Long> {
    val tz    = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date
    return when (this) {
        DateFilter.TODAY      -> epochRangeForDay(today, tz)
        DateFilter.THIS_WEEK  -> {
            // Mon → Sun
            val dow    = today.dayOfWeek.ordinal  // Mon=0, Sun=6
            val monday = today.minus(DatePeriod(days = dow))
            val sunday = monday.plus(DatePeriod(days = 6))
            epochRangeForDay(monday, tz).first to epochRangeForDay(sunday, tz).second
        }
        DateFilter.THIS_MONTH -> {
            val first = LocalDate(today.year, today.month, 1)
            val last  = first.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
            epochRangeForDay(first, tz).first to epochRangeForDay(last, tz).second
        }
    }
}

@OptIn(ExperimentalTime::class)
private fun epochRangeForDay(date: LocalDate, tz: TimeZone): Pair<Long, Long> {
    val start = date.atStartOfDayIn(tz).toEpochMilliseconds()
    return start to (start + 86_400_000L - 1L)
}

// ─── Domain helper ────────────────────────────────────────────────────────────

data class CategoryTotal(
    val category : ExpenseCategory,
    val total    : Double,
)

// ─── State ────────────────────────────────────────────────────────────────────

data class ExpensesState(
    // List
    val expenses       : List<Expense>       = emptyList(),
    val categoryTotals : List<CategoryTotal> = emptyList(),
    val totalAmount    : Double              = 0.0,
    val isLoading      : Boolean             = true,
    val error          : String?             = null,

    // Filters
    val dateFilter       : DateFilter         = DateFilter.THIS_MONTH,
    val selectedCategory : ExpenseCategory?   = null,
    val currencySymbol   : String             = "Rs.",

    // Accounts (loaded for form)
    val bankAccounts : List<BankAccount> = emptyList(),

    // Sheet
    val showFormSheet  : Boolean   = false,
    val editingExpense : Expense?  = null,

    // Delete confirm
    val showDeleteId : String? = null,

    // ── Form fields ───────────────────────────────────────────────────────────
    val formCategory    : ExpenseCategory = ExpenseCategory.OTHER,
    val formTitle       : String          = "",
    val formAmount      : String          = "",
    val formAccountType : AccountType     = AccountType.CASH,
    val formAccountId   : String          = "default_cash",
    val formReceiptRef  : String          = "",
    val formNotes       : String          = "",
    val formError       : String?         = null,
    val isSaving        : Boolean         = false,
) {
    /** Expenses visible after category filter. */
    val filtered: List<Expense>
        get() = if (selectedCategory == null) expenses
        else expenses.filter { it.category == selectedCategory }
}

// ─── Intent ───────────────────────────────────────────────────────────────────

sealed class ExpensesIntent {
    data object Load                                               : ExpensesIntent()
    data class  SetDateFilter(val filter: DateFilter)             : ExpensesIntent()
    data class  SetCategoryFilter(val category: ExpenseCategory?) : ExpensesIntent()

    // Sheet
    data object ShowAddSheet                                       : ExpensesIntent()
    data class  ShowEditSheet(val expense: Expense)                : ExpensesIntent()
    data object DismissSheet                                       : ExpensesIntent()
    data object SaveExpense                                        : ExpensesIntent()

    // Delete
    data class  ConfirmDelete(val id: String) : ExpensesIntent()
    data object DeleteExpense                 : ExpensesIntent()

    // Form fields
    data class FormCategory   (val v: ExpenseCategory) : ExpensesIntent()
    data class FormTitle      (val v: String)           : ExpensesIntent()
    data class FormAmount     (val v: String)           : ExpensesIntent()
    data class FormAccountType(val v: AccountType)      : ExpensesIntent()
    data class FormAccountId  (val v: String)           : ExpensesIntent()
    data class FormReceiptRef (val v: String)           : ExpensesIntent()
    data class FormNotes      (val v: String)           : ExpensesIntent()
}

// ─── Effect ───────────────────────────────────────────────────────────────────

sealed class ExpensesEffect {
    data class ShowToast(val message: String) : ExpensesEffect()
}
//enum class ExpDateFilter { TODAY, WEEK, MONTH }
//
//data class ExpensesState(
//    val isLoading: Boolean = true,
//    val expenses: List<Expense> = emptyList(),
//    val categoryTotals: Map<ExpenseCategory, Double> = emptyMap(),
//    val dateFilter: ExpDateFilter = ExpDateFilter.TODAY,
//    val currencySymbol: String = "Rs.",
//    val showAddDialog: Boolean = false,
//    val formCategory: ExpenseCategory = ExpenseCategory.OTHER,
//    val formAmount: String = "",
//    val formDescription: String = "",
//    val formAccountType: AccountType = AccountType.CASH,
//    val formBankAccountId: String? = null,
//    val bankAccounts: List<BankAccount> = emptyList(),
//    val cashBalance: Double = 0.0,
//    val formError: String? = null,
//    val error: String? = null
//) {
//    val totalExpenses get() = expenses.sumOf { it.amount }
//}
//
//sealed class ExpensesIntent {
//    data object Load : ExpensesIntent()
//    data class SetFilter(val f: ExpDateFilter) : ExpensesIntent()
//    data object ShowAddDialog : ExpensesIntent()
//    data object DismissDialog : ExpensesIntent()
//    data object SaveExpense : ExpensesIntent()
//    data class DeleteExpense(val id: String) : ExpensesIntent()
//    data class FormCategory(val v: ExpenseCategory) : ExpensesIntent()
//    data class FormAmount(val v: String) : ExpensesIntent()
//    data class FormDescription(val v: String) : ExpensesIntent()
//    data class FormAccountType(val v: AccountType) : ExpensesIntent()
//    data class FormBankAccountId(val v: String?) : ExpensesIntent()
//}
//
//sealed class ExpensesEffect { data class Toast(val msg: String) : ExpensesEffect() }
