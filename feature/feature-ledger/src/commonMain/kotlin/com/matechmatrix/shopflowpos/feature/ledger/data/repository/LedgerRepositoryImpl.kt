package com.matechmatrix.shopflowpos.feature.ledger.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.LedgerEntry
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.TransactionType
import com.matechmatrix.shopflowpos.feature.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class LedgerRepositoryImpl(private val db: DatabaseProvider) : LedgerRepository {

    override suspend fun getCashBalance(): AppResult<Double> = withContext(Dispatchers.Default) {
        try {
            val balance = db.ledgerQueries.getCashBalance().executeAsOne()
            AppResult.Success(balance)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to fetch cash balance")
        }
    }

    override suspend fun setCashBalance(amount: Double): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.ledgerQueries.updateCashBalance(amount, now)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to set cash balance")
        }
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

    override suspend fun addBankAccount(account: BankAccount): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.ledgerQueries.insertBankAccount(
                id = IdGenerator.generate(),
                bank_name = account.bankName,
                account_title = account.accountTitle,
                account_number = account.accountNumber,
                balance = account.balance,
                updated_at = now
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to add bank account")
        }
    }

    override suspend fun updateBankAccount(account: BankAccount): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            db.ledgerQueries.updateBankAccount(
                bank_name = account.bankName,
                account_title = account.accountTitle,
                account_number = account.accountNumber,
                id = account.id
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update bank account")
        }
    }

    override suspend fun deleteBankAccount(id: String): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            db.ledgerQueries.deleteBankAccount(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete bank account")
        }
    }

    override suspend fun updateBankBalance(id: String, newBalance: Double): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.ledgerQueries.updateBankBalance(newBalance, now, id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update bank balance")
        }
    }

    override suspend fun getLedgerEntries(startMs: Long, endMs: Long): AppResult<List<LedgerEntry>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.ledgerQueries.getLedgerByDateRange(startMs, endMs).executeAsList()
            AppResult.Success(rows.map { r ->
                LedgerEntry(
                    id = r.id,
                    type = runCatching { TransactionType.valueOf(r.type) }.getOrDefault(TransactionType.CREDIT),
                    amount = r.amount,
                    accountType = runCatching { AccountType.valueOf(r.account_type) }.getOrDefault(AccountType.CASH),
                    bankAccountId = r.bank_account_id,
                    referenceId = r.reference_id,
                    description = r.description,
                    balanceAfter = r.balance_after,
                    createdAt = r.created_at
                )
            })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to fetch ledger entries")
        }
    }

    override suspend fun isFirstRun(): Boolean = withContext(Dispatchers.Default) {
        // Simple check if any bank account or cash balance is initialized
        db.ledgerQueries.getCashBalance().executeAsOneOrNull() == null
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}
