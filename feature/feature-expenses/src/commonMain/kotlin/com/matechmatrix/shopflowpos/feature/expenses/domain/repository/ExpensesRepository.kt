package com.matechmatrix.shopflowpos.feature.expenses.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.Expense
import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory

interface ExpensesRepository {
    suspend fun getExpensesByDateRange(startMs: Long, endMs: Long): AppResult<List<Expense>>
    suspend fun insertExpense(expense: Expense): AppResult<Unit>
    suspend fun deleteExpense(id: String): AppResult<Unit>
    suspend fun getCategoryTotals(startMs: Long, endMs: Long): AppResult<Map<ExpenseCategory, Double>>
    suspend fun getCurrencySymbol(): String
    suspend fun getBankAccounts(): AppResult<List<BankAccount>>
    suspend fun getCashBalance(): AppResult<Double>
}
