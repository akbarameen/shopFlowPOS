package com.matechmatrix.shopflowpos.feature.expenses.presentation

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.model.Expense
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory
import com.matechmatrix.shopflowpos.feature.expenses.domain.repository.ExpensesRepository
import kotlinx.datetime.Clock

class ExpensesViewModel(
    private val repo: ExpensesRepository
) : MviViewModel<ExpensesState, ExpensesIntent, ExpensesEffect>(ExpensesState()) {

    init { onIntent(ExpensesIntent.Load) }

    override suspend fun handleIntent(intent: ExpensesIntent) {
        when (intent) {
            ExpensesIntent.Load        -> load()

            is ExpensesIntent.SetDateFilter -> {
                setState { copy(dateFilter = intent.filter) }
                reload()
            }

            is ExpensesIntent.SetCategoryFilter ->
                setState { copy(selectedCategory = intent.category) }

            // ── Sheet ─────────────────────────────────────────────────────────
            ExpensesIntent.ShowAddSheet -> setState {
                copy(
                    showFormSheet   = true,
                    editingExpense  = null,
                    formCategory    = ExpenseCategory.OTHER,
                    formTitle       = "",
                    formAmount      = "",
                    formAccountType = AccountType.CASH,
                    formAccountId   = "default_cash",
                    formReceiptRef  = "",
                    formNotes       = "",
                    formError       = null,
                )
            }

            is ExpensesIntent.ShowEditSheet -> {
                val e = intent.expense
                setState {
                    copy(
                        showFormSheet   = true,
                        editingExpense  = e,
                        formCategory    = e.category,
                        formTitle       = e.title,
                        formAmount      = e.amount.toString(),
                        formAccountType = e.accountType,
                        formAccountId   = e.accountId,
                        formReceiptRef  = e.receiptRef ?: "",
                        formNotes       = e.notes ?: "",
                        formError       = null,
                    )
                }
            }

            ExpensesIntent.DismissSheet -> setState {
                copy(showFormSheet = false, editingExpense = null, formError = null)
            }

            ExpensesIntent.SaveExpense -> doSave()

            // ── Delete ────────────────────────────────────────────────────────
            is ExpensesIntent.ConfirmDelete ->
                setState { copy(showDeleteId = intent.id.ifBlank { null }) }

            ExpensesIntent.DeleteExpense -> {
                val id = state.value.showDeleteId ?: return
                setState { copy(showDeleteId = null) }
                when (val r = repo.deleteExpense(id)) {
                    is AppResult.Success -> {
                        setEffect(ExpensesEffect.ShowToast("Expense deleted"))
                        reload()
                    }
                    is AppResult.Error -> setEffect(ExpensesEffect.ShowToast(r.message))
                    else -> {}
                }
            }

            // ── Form fields ───────────────────────────────────────────────────
            is ExpensesIntent.FormCategory    -> setState { copy(formCategory    = intent.v) }
            is ExpensesIntent.FormTitle       -> setState { copy(formTitle       = intent.v) }
            is ExpensesIntent.FormAmount      -> setState { copy(formAmount      = intent.v) }
            is ExpensesIntent.FormAccountType -> {
                // Reset accountId to default when switching type
                val defaultId = if (intent.v == AccountType.CASH) "default_cash"
                else state.value.bankAccounts.firstOrNull()?.id ?: ""
                setState { copy(formAccountType = intent.v, formAccountId = defaultId) }
            }
            is ExpensesIntent.FormAccountId   -> setState { copy(formAccountId   = intent.v) }
            is ExpensesIntent.FormReceiptRef  -> setState { copy(formReceiptRef  = intent.v) }
            is ExpensesIntent.FormNotes       -> setState { copy(formNotes       = intent.v) }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        val banks    = when (val r = repo.getBankAccounts()) {
            is AppResult.Success -> r.data
            else                 -> emptyList()
        }
        setState { copy(currencySymbol = currency, bankAccounts = banks) }
        reload()
    }

    private suspend fun reload() {
        val (from, to) = state.value.dateFilter.toEpochRange()
        setState { copy(isLoading = true, error = null) }

        val expensesResult = repo.getExpenses(from, to)
        val totalsResult   = repo.getCategoryTotals(from, to)
        val sumResult      = repo.getTotalAmount(from, to)

        setState {
            copy(
                isLoading      = false,
                expenses       = (expensesResult as? AppResult.Success)?.data ?: expenses,
                categoryTotals = (totalsResult   as? AppResult.Success)?.data ?: categoryTotals,
                totalAmount    = (sumResult       as? AppResult.Success)?.data ?: totalAmount,
                error          = (expensesResult  as? AppResult.Error)?.message,
            )
        }
    }

    private suspend fun doSave() {
        val s = state.value
        if (s.formTitle.isBlank()) {
            setState { copy(formError = "Title is required") }
            return
        }
        val amount = s.formAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            setState { copy(formError = "Enter a valid amount") }
            return
        }
        if (s.formAccountType == AccountType.BANK && s.formAccountId.isBlank()) {
            setState { copy(formError = "Select a bank account") }
            return
        }

        setState { copy(isSaving = true, formError = null) }
        val isNew    = s.editingExpense == null
        val now      = Clock.System.now().toEpochMilliseconds()
        val expense  = Expense(
            id          = s.editingExpense?.id ?: IdGenerator.generate(),
            category    = s.formCategory,
            title       = s.formTitle.trim(),
            amount      = amount,
            accountType = s.formAccountType,
            accountId   = s.formAccountId,
            receiptRef  = s.formReceiptRef.takeIf { it.isNotBlank() },
            notes       = s.formNotes.takeIf { it.isNotBlank() },
            createdAt   = s.editingExpense?.createdAt ?: now,
        )

        val result = if (isNew) repo.insertExpense(expense) else repo.updateExpense(expense)
        when (result) {
            is AppResult.Success -> {
                setState { copy(isSaving = false, showFormSheet = false, editingExpense = null) }
                setEffect(ExpensesEffect.ShowToast(if (isNew) "Expense recorded" else "Expense updated"))
                reload()
            }
            is AppResult.Error -> setState { copy(isSaving = false, formError = result.message) }
            else               -> setState { copy(isSaving = false) }
        }
    }
}

//class ExpensesViewModel(private val repo: ExpensesRepository) :
//    MviViewModel<ExpensesState, ExpensesIntent, ExpensesEffect>(ExpensesState()) {
//
//    init {
//        onIntent(ExpensesIntent.Load)
//    }
//
//    override suspend fun handleIntent(intent: ExpensesIntent) {
//        when (intent) {
//            ExpensesIntent.Load -> load(currentState.dateFilter)
//            is ExpensesIntent.SetFilter -> {
//                setState { copy(dateFilter = intent.f) }
//                load(intent.f)
//            }
//            ExpensesIntent.ShowAddDialog -> {
//                val banks = (repo.getBankAccounts() as? AppResult.Success)?.data ?: emptyList()
//                val cash = (repo.getCashBalance() as? AppResult.Success)?.data ?: 0.0
//                setState {
//                    copy(
//                        showAddDialog = true,
//                        formCategory = ExpenseCategory.OTHER,
//                        formAmount = "",
//                        formDescription = "",
//                        formAccountType = AccountType.CASH,
//                        formBankAccountId = banks.firstOrNull()?.id,
//                        bankAccounts = banks,
//                        cashBalance = cash,
//                        formError = null
//                    )
//                }
//            }
//            ExpensesIntent.DismissDialog -> setState { copy(showAddDialog = false) }
//            ExpensesIntent.SaveExpense -> saveExpense()
//            is ExpensesIntent.DeleteExpense -> {
//                repo.deleteExpense(intent.id)
//                load(currentState.dateFilter)
//            }
//            is ExpensesIntent.FormCategory    -> setState { copy(formCategory = intent.v) }
//            is ExpensesIntent.FormAmount -> setState { copy(formAmount = intent.v) }
//            is ExpensesIntent.FormDescription -> setState { copy(formDescription = intent.v) }
//            is ExpensesIntent.FormAccountType -> setState { copy(formAccountType = intent.v) }
//            is ExpensesIntent.FormBankAccountId -> setState { copy(formBankAccountId = intent.v) }
//            is ExpensesIntent.FormAmount -> setState { copy(formAmount = intent.v) }
//        }
//    }
//
//    private suspend fun load(filter: ExpDateFilter) {
//        setState { copy(isLoading = true) }
//        val currency = repo.getCurrencySymbol()
//        val range = when (filter) {
//            ExpDateFilter.TODAY -> DateTimeUtils.todayRange()
//            ExpDateFilter.WEEK  -> DateTimeUtils.thisWeekRange()
//            ExpDateFilter.MONTH -> DateTimeUtils.thisMonthRange()
//        }
//        val start = range.first
//        val end = range.second
//
//        val expensesResult = repo.getExpensesByDateRange(start, end)
//        val totalsResult = repo.getCategoryTotals(start, end)
//
//        val expenses = (expensesResult as? AppResult.Success)?.data ?: emptyList()
//        val totals = (totalsResult as? AppResult.Success)?.data ?: emptyMap()
//
//        setState {
//            copy(
//                isLoading = false,
//                expenses = expenses.sortedByDescending { it.createdAt },
//                categoryTotals = totals,
//                currencySymbol = currency
//            )
//        }
//    }
//
//    private suspend fun saveExpense() {
//        val s = currentState
//        val amount = s.formAmount.toDoubleOrNull()
//        if (amount == null || amount <= 0) {
//            setState { copy(formError = "Enter a valid amount") }
//            return
//        }
//        if (s.formDescription.isBlank()) {
//            setState { copy(formError = "Description is required") }
//            return
//        }
//
//        // Balance Check
//        if (s.formAccountType == AccountType.CASH) {
//            if (amount > s.cashBalance) {
//                setState { copy(formError = "Insufficient cash balance. Available: ${s.cashBalance}") }
//                return
//            }
//        } else if (s.formAccountType == AccountType.BANK) {
//            if (s.formBankAccountId == null) {
//                setState { copy(formError = "Please select a bank account") }
//                return
//            }
//            val bank = s.bankAccounts.find { it.id == s.formBankAccountId }
//            val bankBalance = bank?.balance ?: 0.0
//            if (amount > bankBalance) {
//                setState { copy(formError = "Insufficient bank balance. Available: $bankBalance") }
//                return
//            }
//        }
//
//        val expense = Expense(
//            id = IdGenerator.generate(),
//            category = s.formCategory,
//            title = s.formDescription.trim(),
//            amount = amount,
//            accountType = s.formAccountType,
//            bankAccountId = if (s.formAccountType == AccountType.BANK) s.formBankAccountId else null,
//            notes = null,
//            createdAt = Clock.System.now().toEpochMilliseconds()
//        )
//        when (val r = repo.insertExpense(expense)) {
//            is AppResult.Success -> {
//                setState { copy(showAddDialog = false) }
//                setEffect(ExpensesEffect.Toast("Expense added"))
//                load(s.dateFilter)
//            }
//            is AppResult.Error -> setState { copy(formError = r.message) }
//            else -> {}
//        }
//    }
//}
