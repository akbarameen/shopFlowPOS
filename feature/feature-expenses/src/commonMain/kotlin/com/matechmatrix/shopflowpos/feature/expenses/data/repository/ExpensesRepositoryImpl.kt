package com.matechmatrix.shopflowpos.feature.expenses.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.Expense
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory
import com.matechmatrix.shopflowpos.feature.expenses.domain.repository.ExpensesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

import com.matechmatrix.shopflowpos.feature.expenses.presentation.CategoryTotal

import kotlinx.coroutines.withContext

class ExpensesRepositoryImpl(private val db: DatabaseProvider) : ExpensesRepository {

    // ── Row mappers ──────────────────────────────────────────────────────────

    private fun mapExpense(r: com.matechmatrix.shopflowpos.db.Expense) = Expense(
        id          = r.id,
        category    = runCatching { ExpenseCategory.valueOf(r.category) }
            .getOrDefault(ExpenseCategory.OTHER),
        title       = r.title,
        amount      = r.amount,
        accountType = runCatching { AccountType.valueOf(r.account_type) }
            .getOrDefault(AccountType.CASH),
        accountId   = r.account_id,
        receiptRef  = r.receipt_ref,
        notes       = r.notes,
        createdAt   = r.created_at,
    )

    private fun mapBank(r: com.matechmatrix.shopflowpos.db.Bank_account) = BankAccount(
        id            = r.id,
        bankName      = r.bank_name,
        accountTitle  = r.account_title,
        accountNumber = r.account_number,
        iban          = r.iban,
        balance       = r.balance,
        isActive      = r.is_active == 1L,
        createdAt     = r.created_at,
        updatedAt     = r.updated_at,
    )

    // ── Read ─────────────────────────────────────────────────────────────────

    override suspend fun getExpenses(from: Long, to: Long): AppResult<List<Expense>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.expenseQueries.getExpensesByDateRange(from, to)
                        .executeAsList().map(::mapExpense)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load expenses") }
        }

    override suspend fun getCategoryTotals(from: Long, to: Long): AppResult<List<CategoryTotal>> =
        withContext(Dispatchers.Default) {
            runCatching {
                val rows = db.expenseQueries.getExpensesByCategory(from, to).executeAsList()
                AppResult.Success(rows.mapNotNull { row ->
                    runCatching {
                        CategoryTotal(
                            category = ExpenseCategory.valueOf(row.category),
                            total    = row.total,
                        )
                    }.getOrNull()
                })
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load category totals") }
        }

    override suspend fun getTotalAmount(from: Long, to: Long): AppResult<Double> =
        withContext(Dispatchers.Default) {
            runCatching {
                val total = db.expenseQueries.getTotalExpensesByDateRange(from, to).executeAsOne()
                // SQLDelight returns Double? for COALESCE aggregate; use ?: 0.0
                AppResult.Success((total as? Double) ?: 0.0)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to compute total") }
        }

    override suspend fun getBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getAllActiveBankAccounts()
                        .executeAsList().map(::mapBank)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load bank accounts") }
        }

    override suspend fun getCurrencySymbol(): String =
        withContext(Dispatchers.Default) {
            db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
        }

    // ── Insert — full accounting flow ────────────────────────────────────────
    // Flow: insertExpense → debit account balance → insertLedgerEntry

    override suspend fun insertExpense(expense: Expense): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = expense.createdAt

                // 1. Persist expense row
                db.expenseQueries.insertExpense(
                    id           = expense.id,
                    category     = expense.category.name,
                    title        = expense.title,
                    amount       = expense.amount,
                    account_type = expense.accountType.name,
                    account_id   = expense.accountId,
                    receipt_ref  = expense.receiptRef,
                    notes        = expense.notes,
                    created_at   = now,
                )

                // 2. Debit account + record ledger entry
                debitAccount(expense.accountId, expense.accountType, expense.amount, now, expense.id, expense.title)

                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to record expense") }
        }

    // ── Update — adjusts the balance delta if amount changed ─────────────────

    override suspend fun updateExpense(expense: Expense): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()

                // Read old row to compute diff
                val old = db.expenseQueries.getExpenseById(expense.id).executeAsOneOrNull()
                    ?: return@withContext AppResult.Error("Expense not found")

                db.expenseQueries.updateExpense(
                    category     = expense.category.name,
                    title        = expense.title,
                    amount       = expense.amount,
                    account_type = expense.accountType.name,
                    account_id   = expense.accountId,
                    receipt_ref  = expense.receiptRef,
                    notes        = expense.notes,
                    id           = expense.id,
                )

                // Adjust account balance by the difference (same account only)
                val amountDiff = expense.amount - old.amount
                val sameAccount = expense.accountType.name == old.account_type &&
                        expense.accountId == old.account_id
                if (sameAccount && amountDiff != 0.0) {
                    adjustAccountBalance(expense.accountId, expense.accountType, -amountDiff, now)
                }

                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to update expense") }
        }

    // ── Delete — reverses the debit ──────────────────────────────────────────

    override suspend fun deleteExpense(id: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now  = Clock.System.now().toEpochMilliseconds()
                val row  = db.expenseQueries.getExpenseById(id).executeAsOneOrNull()
                    ?: return@withContext AppResult.Error("Expense not found")
                val type = runCatching { AccountType.valueOf(row.account_type) }
                    .getOrDefault(AccountType.CASH)

                // Reverse debit (add amount back)
                adjustAccountBalance(row.account_id, type, +row.amount, now)
                db.expenseQueries.deleteExpense(id)

                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to delete expense") }
        }

    // ── Private accounting helpers ────────────────────────────────────────────

    /**
     * Debits [accountId] by [amount], records a ledger DEBIT entry.
     * Called on expense insert.
     */
    private fun debitAccount(
        accountId   : String,
        accountType : AccountType,
        amount      : Double,
        now         : Long,
        expenseId   : String,
        description : String,
    ) {
        when (accountType) {
            AccountType.CASH -> {
                val cur  = db.ledgerQueries.getCashAccountById(accountId)
                    .executeAsOneOrNull()?.balance ?: 0.0
                val next = (cur - amount).coerceAtLeast(0.0)
                db.ledgerQueries.updateCashBalance(next, now, accountId)
                insertLedgerEntry(
                    accountType = accountType.name,
                    accountId   = accountId,
                    entryType   = "DEBIT",
                    refType     = "EXPENSE",
                    refId       = expenseId,
                    amount      = amount,
                    balanceAfter = next,
                    description = "Expense: $description",
                    now         = now,
                )
            }
            AccountType.BANK -> {
                val cur  = db.ledgerQueries.getBankAccountById(accountId)
                    .executeAsOneOrNull()?.balance ?: 0.0
                val next = (cur - amount).coerceAtLeast(0.0)
                db.ledgerQueries.updateBankBalance(next, now, accountId)
                insertLedgerEntry(
                    accountType  = accountType.name,
                    accountId    = accountId,
                    entryType    = "DEBIT",
                    refType      = "EXPENSE",
                    refId        = expenseId,
                    amount       = amount,
                    balanceAfter = next,
                    description  = "Expense: $description",
                    now          = now,
                )
            }
            AccountType.MOBILE_WALLET -> { /* future — not tracked in DB */ }
        }
    }

    /**
     * Adjusts [accountId] balance by [delta] (positive = credit, negative = debit).
     * Used on update/delete without inserting a ledger entry (adjustment-only).
     */
    private fun adjustAccountBalance(
        accountId   : String,
        accountType : AccountType,
        delta       : Double,
        now         : Long,
    ) {
        when (accountType) {
            AccountType.CASH -> {
                val cur  = db.ledgerQueries.getCashAccountById(accountId)
                    .executeAsOneOrNull()?.balance ?: 0.0
                db.ledgerQueries.updateCashBalance(
                    (cur + delta).coerceAtLeast(0.0), now, accountId
                )
            }
            AccountType.BANK -> {
                val cur  = db.ledgerQueries.getBankAccountById(accountId)
                    .executeAsOneOrNull()?.balance ?: 0.0
                db.ledgerQueries.updateBankBalance(
                    (cur + delta).coerceAtLeast(0.0), now, accountId
                )
            }
            else -> {}
        }
    }

    private fun insertLedgerEntry(
        accountType  : String,
        accountId    : String,
        entryType    : String,
        refType      : String,
        refId        : String,
        amount       : Double,
        balanceAfter : Double,
        description  : String,
        now          : Long,
    ) {
        db.ledgerQueries.insertLedgerEntry(
            id             = IdGenerator.generate(),
            account_type   = accountType,
            account_id     = accountId,
            entry_type     = entryType,
            reference_type = refType,
            reference_id   = refId,
            amount         = amount,
            balance_after  = balanceAfter,
            description    = description,
            created_at     = now,
        )
    }
}

//class ExpensesRepositoryImpl(private val db: DatabaseProvider) : ExpensesRepository {
//
//    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Expense) = Expense(
//        id = r.id,
//        category = runCatching { ExpenseCategory.valueOf(r.category) }.getOrDefault(ExpenseCategory.OTHER),
//        title = r.title,
//        amount = r.amount,
//        accountType = runCatching { AccountType.valueOf(r.account_type) }.getOrDefault(AccountType.CASH),
//        bankAccountId = r.bank_account_id,
//        notes = r.notes,
//        createdAt = r.created_at
//    )
//
//    override suspend fun getExpensesByDateRange(startMs: Long, endMs: Long): AppResult<List<Expense>> =
//        withContext(Dispatchers.Default) {
//            try {
//                AppResult.Success(db.expenseQueries.getExpensesByDateRange(startMs, endMs).executeAsList().map(::mapRow))
//            } catch (e: Exception) {
//                AppResult.Error(e.message ?: "Failed to fetch expenses")
//            }
//        }
//
//    override suspend fun insertExpense(expense: Expense): AppResult<Unit> = withContext(Dispatchers.Default) {
//        try {
//            val now = Clock.System.now().toEpochMilliseconds()
//
//            db.expenseQueries.insertExpense(
//                id = expense.id,
//                category = expense.category.name,
//                title = expense.title,
//                amount = expense.amount,
//                account_type = expense.accountType.name,
//                bank_account_id = expense.bankAccountId,
//                notes = expense.notes,
//                created_at = now
//            )
//
//            var newBalance = 0.0
//            val bankId = expense.bankAccountId
//            if (expense.accountType == AccountType.CASH) {
//                val current = db.ledgerQueries.getCashBalance().executeAsOne()
//                newBalance = (current - expense.amount).coerceAtLeast(0.0)
//                db.ledgerQueries.updateCashBalance(newBalance, now)
//            } else if (bankId != null) {
//                val bank = db.ledgerQueries.getBankAccountById(bankId).executeAsOne()
//                newBalance = (bank.balance - expense.amount).coerceAtLeast(0.0)
//                db.ledgerQueries.updateBankBalance(newBalance, now, bankId)
//            }
//
//            db.ledgerQueries.insertLedgerEntry(
//                id = IdGenerator.generate(),
//                type = TransactionType.EXPENSE.name,
//                amount = expense.amount,
//                account_type = expense.accountType.name,
//                bank_account_id = expense.bankAccountId,
//                reference_id = expense.id,
//                description = "Expense: ${expense.title}",
//                balance_after = newBalance,
//                created_at = now
//            )
//
//            AppResult.Success(Unit)
//        } catch (e: Exception) {
//            AppResult.Error(e.message ?: "Failed to add expense")
//        }
//    }
//
//    override suspend fun deleteExpense(id: String): AppResult<Unit> = withContext(Dispatchers.Default) {
//        try {
//            db.expenseQueries.deleteExpense(id)
//            AppResult.Success(Unit)
//        } catch (e: Exception) {
//            AppResult.Error(e.message ?: "Failed to delete expense")
//        }
//    }
//
//    override suspend fun getCategoryTotals(startMs: Long, endMs: Long): AppResult<Map<ExpenseCategory, Double>> =
//        withContext(Dispatchers.Default) {
//            try {
//                val results = db.expenseQueries.getExpensesByCategory(startMs, endMs).executeAsList()
//                val map = results.associate {
//                    val cat = runCatching { ExpenseCategory.valueOf(it.category) }.getOrDefault(ExpenseCategory.OTHER)
//                    cat to (it.total ?: 0.0)
//                }
//                AppResult.Success(map)
//            } catch (e: Exception) {
//                AppResult.Error(e.message ?: "Failed to fetch category totals")
//            }
//        }
//
//    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
//        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
//    }
//
//    override suspend fun getBankAccounts(): AppResult<List<BankAccount>> = withContext(Dispatchers.Default) {
//        try {
//            val rows = db.ledgerQueries.getAllBankAccounts().executeAsList()
//            AppResult.Success(rows.map { r ->
//                BankAccount(
//                    id = r.id,
//                    bankName = r.bank_name,
//                    accountTitle = r.account_title,
//                    accountNumber = r.account_number,
//                    balance = r.balance,
//                    updatedAt = r.updated_at
//                )
//            })
//        } catch (e: Exception) {
//            AppResult.Error(e.message ?: "Failed to fetch bank accounts")
//        }
//    }
//
//    override suspend fun getCashBalance(): AppResult<Double> = withContext(Dispatchers.Default) {
//        try {
//            AppResult.Success(db.ledgerQueries.getCashBalance().executeAsOne())
//        } catch (e: Exception) {
//            AppResult.Error(e.message ?: "Failed to fetch cash balance")
//        }
//    }
//}
