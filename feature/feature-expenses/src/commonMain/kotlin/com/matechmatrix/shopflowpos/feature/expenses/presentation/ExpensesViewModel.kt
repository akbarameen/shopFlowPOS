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

class ExpensesViewModel(private val repo: ExpensesRepository) :
    MviViewModel<ExpensesState, ExpensesIntent, ExpensesEffect>(ExpensesState()) {

    init {
        onIntent(ExpensesIntent.Load)
    }

    override suspend fun handleIntent(intent: ExpensesIntent) {
        when (intent) {
            ExpensesIntent.Load -> load(currentState.dateFilter)
            is ExpensesIntent.SetFilter -> {
                setState { copy(dateFilter = intent.f) }
                load(intent.f)
            }
            ExpensesIntent.ShowAddDialog -> {
                val banks = (repo.getBankAccounts() as? AppResult.Success)?.data ?: emptyList()
                val cash = (repo.getCashBalance() as? AppResult.Success)?.data ?: 0.0
                setState {
                    copy(
                        showAddDialog = true,
                        formCategory = ExpenseCategory.OTHER,
                        formAmount = "",
                        formDescription = "",
                        formAccountType = AccountType.CASH,
                        formBankAccountId = banks.firstOrNull()?.id,
                        bankAccounts = banks,
                        cashBalance = cash,
                        formError = null
                    )
                }
            }
            ExpensesIntent.DismissDialog -> setState { copy(showAddDialog = false) }
            ExpensesIntent.SaveExpense -> saveExpense()
            is ExpensesIntent.DeleteExpense -> {
                repo.deleteExpense(intent.id)
                load(currentState.dateFilter)
            }
            is ExpensesIntent.FormCategory    -> setState { copy(formCategory = intent.v) }
            is ExpensesIntent.FormAmount -> setState { copy(formAmount = intent.v) }
            is ExpensesIntent.FormDescription -> setState { copy(formDescription = intent.v) }
            is ExpensesIntent.FormAccountType -> setState { copy(formAccountType = intent.v) }
            is ExpensesIntent.FormBankAccountId -> setState { copy(formBankAccountId = intent.v) }
            is ExpensesIntent.FormAmount -> setState { copy(formAmount = intent.v) }
        }
    }

    private suspend fun load(filter: ExpDateFilter) {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        val range = when (filter) {
            ExpDateFilter.TODAY -> DateTimeUtils.todayRange()
            ExpDateFilter.WEEK  -> DateTimeUtils.thisWeekRange()
            ExpDateFilter.MONTH -> DateTimeUtils.thisMonthRange()
        }
        val start = range.first
        val end = range.second

        val expensesResult = repo.getExpensesByDateRange(start, end)
        val totalsResult = repo.getCategoryTotals(start, end)
        
        val expenses = (expensesResult as? AppResult.Success)?.data ?: emptyList()
        val totals = (totalsResult as? AppResult.Success)?.data ?: emptyMap()
        
        setState {
            copy(
                isLoading = false, 
                expenses = expenses.sortedByDescending { it.createdAt },
                categoryTotals = totals, 
                currencySymbol = currency
            )
        }
    }

    private suspend fun saveExpense() {
        val s = currentState
        val amount = s.formAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            setState { copy(formError = "Enter a valid amount") }
            return
        }
        if (s.formDescription.isBlank()) {
            setState { copy(formError = "Description is required") }
            return
        }

        // Balance Check
        if (s.formAccountType == AccountType.CASH) {
            if (amount > s.cashBalance) {
                setState { copy(formError = "Insufficient cash balance. Available: ${s.cashBalance}") }
                return
            }
        } else if (s.formAccountType == AccountType.BANK) {
            if (s.formBankAccountId == null) {
                setState { copy(formError = "Please select a bank account") }
                return
            }
            val bank = s.bankAccounts.find { it.id == s.formBankAccountId }
            val bankBalance = bank?.balance ?: 0.0
            if (amount > bankBalance) {
                setState { copy(formError = "Insufficient bank balance. Available: $bankBalance") }
                return
            }
        }

        val expense = Expense(
            id = IdGenerator.generate(),
            category = s.formCategory,
            title = s.formDescription.trim(),
            amount = amount,
            accountType = s.formAccountType,
            bankAccountId = if (s.formAccountType == AccountType.BANK) s.formBankAccountId else null,
            notes = null,
            createdAt = Clock.System.now().toEpochMilliseconds()
        )
        when (val r = repo.insertExpense(expense)) {
            is AppResult.Success -> {
                setState { copy(showAddDialog = false) }
                setEffect(ExpensesEffect.Toast("Expense added"))
                load(s.dateFilter)
            }
            is AppResult.Error -> setState { copy(formError = r.message) }
            else -> {}
        }
    }
}
