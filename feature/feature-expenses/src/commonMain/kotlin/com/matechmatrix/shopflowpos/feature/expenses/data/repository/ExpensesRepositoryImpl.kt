package com.matechmatrix.shopflowpos.feature.expenses.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.Expense
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory
import com.matechmatrix.shopflowpos.core.model.enums.TransactionType
import com.matechmatrix.shopflowpos.feature.expenses.domain.repository.ExpensesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class ExpensesRepositoryImpl(private val db: DatabaseProvider) : ExpensesRepository {

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Expense) = Expense(
        id = r.id,
        category = runCatching { ExpenseCategory.valueOf(r.category) }.getOrDefault(ExpenseCategory.OTHER),
        title = r.title,
        amount = r.amount,
        accountType = runCatching { AccountType.valueOf(r.account_type) }.getOrDefault(AccountType.CASH),
        bankAccountId = r.bank_account_id,
        notes = r.notes,
        createdAt = r.created_at
    )

    override suspend fun getExpensesByDateRange(startMs: Long, endMs: Long): AppResult<List<Expense>> =
        withContext(Dispatchers.Default) {
            try {
                AppResult.Success(db.expenseQueries.getExpensesByDateRange(startMs, endMs).executeAsList().map(::mapRow))
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to fetch expenses")
            }
        }

    override suspend fun insertExpense(expense: Expense): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            
            db.expenseQueries.insertExpense(
                id = expense.id,
                category = expense.category.name,
                title = expense.title,
                amount = expense.amount,
                account_type = expense.accountType.name,
                bank_account_id = expense.bankAccountId,
                notes = expense.notes,
                created_at = now
            )

            var newBalance = 0.0
            val bankId = expense.bankAccountId
            if (expense.accountType == AccountType.CASH) {
                val current = db.ledgerQueries.getCashBalance().executeAsOne()
                newBalance = (current - expense.amount).coerceAtLeast(0.0)
                db.ledgerQueries.updateCashBalance(newBalance, now)
            } else if (bankId != null) {
                val bank = db.ledgerQueries.getBankAccountById(bankId).executeAsOne()
                newBalance = (bank.balance - expense.amount).coerceAtLeast(0.0)
                db.ledgerQueries.updateBankBalance(newBalance, now, bankId)
            }

            db.ledgerQueries.insertLedgerEntry(
                id = IdGenerator.generate(),
                type = TransactionType.EXPENSE.name,
                amount = expense.amount,
                account_type = expense.accountType.name,
                bank_account_id = expense.bankAccountId,
                reference_id = expense.id,
                description = "Expense: ${expense.title}",
                balance_after = newBalance,
                created_at = now
            )
            
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to add expense")
        }
    }

    override suspend fun deleteExpense(id: String): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            db.expenseQueries.deleteExpense(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete expense")
        }
    }

    override suspend fun getCategoryTotals(startMs: Long, endMs: Long): AppResult<Map<ExpenseCategory, Double>> =
        withContext(Dispatchers.Default) {
            try {
                val results = db.expenseQueries.getExpensesByCategory(startMs, endMs).executeAsList()
                val map = results.associate { 
                    val cat = runCatching { ExpenseCategory.valueOf(it.category) }.getOrDefault(ExpenseCategory.OTHER)
                    cat to (it.total ?: 0.0)
                }
                AppResult.Success(map)
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to fetch category totals")
            }
        }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }

    override suspend fun getBankAccounts(): AppResult<List<BankAccount>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.ledgerQueries.getAllBankAccounts().executeAsList()
            AppResult.Success(rows.map { r ->
                BankAccount(
                    id = r.id,
                    bankName = r.bank_name,
                    accountTitle = r.account_title,
                    accountNumber = r.account_number,
                    balance = r.balance,
                    updatedAt = r.updated_at
                )
            })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to fetch bank accounts")
        }
    }

    override suspend fun getCashBalance(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            AppResult.Success(db.ledgerQueries.getCashBalance().executeAsOne())
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to fetch cash balance")
        }
    }
}
