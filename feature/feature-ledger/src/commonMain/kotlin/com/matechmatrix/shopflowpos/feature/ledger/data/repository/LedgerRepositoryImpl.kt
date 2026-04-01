package com.matechmatrix.shopflowpos.feature.ledger.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.LedgerEntryType
import com.matechmatrix.shopflowpos.core.model.enums.LedgerReferenceType
import com.matechmatrix.shopflowpos.feature.ledger.data.paging.LedgerPagingSource
import com.matechmatrix.shopflowpos.feature.ledger.domain.repository.LedgerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class LedgerRepositoryImpl(private val db: DatabaseProvider) : LedgerRepository {

    // ── Mappers ───────────────────────────────────────────────────────────────
    private fun mapCash(r: com.matechmatrix.shopflowpos.db.Cash_account) = CashAccount(
        id = r.id, name = r.name, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapBank(r: com.matechmatrix.shopflowpos.db.Bank_account) = BankAccount(
        id = r.id, bankName = r.bank_name, accountTitle = r.account_title,
        accountNumber = r.account_number, iban = r.iban, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    private fun mapEntry(r: com.matechmatrix.shopflowpos.db.Ledger_entry) = LedgerEntry(
        id            = r.id,
        accountType   = runCatching { AccountType.valueOf(r.account_type) }.getOrDefault(AccountType.CASH),
        accountId     = r.account_id,
        entryType     = runCatching { LedgerEntryType.valueOf(r.entry_type) }.getOrDefault(LedgerEntryType.CREDIT),
        referenceType = runCatching { LedgerReferenceType.valueOf(r.reference_type) }.getOrDefault(LedgerReferenceType.ADJUSTMENT),
        referenceId   = r.reference_id,
        amount        = r.amount,
        balanceAfter  = r.balance_after,
        description   = r.description,
        createdAt     = r.created_at
    )

    // ── Cash Accounts ─────────────────────────────────────────────────────────

    override suspend fun getAllCashAccounts(): AppResult<List<CashAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCash))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load cash accounts") }
        }

    override suspend fun adjustCashBalance(accountId: String, newBalance: Double): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now     = Clock.System.now().toEpochMilliseconds()
                val current = db.ledgerQueries.getCashAccountById(accountId).executeAsOne()
                val delta   = newBalance - current.balance

                db.database.transaction {
                    db.ledgerQueries.updateCashBalance(newBalance, now, accountId)
                    db.ledgerQueries.insertLedgerEntry(
                        id             = IdGenerator.generate(),
                        account_type   = "CASH",
                        account_id     = accountId,
                        entry_type     = if (delta >= 0) "CREDIT" else "DEBIT",
                        reference_type = "ADJUSTMENT",
                        reference_id   = null,
                        amount         = kotlin.math.abs(delta),
                        balance_after  = newBalance,
                        description    = "Manual balance adjustment → ${current.name}",
                        created_at     = now
                    )
                }
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to adjust cash balance") }
        }

    override suspend fun addCashAccount(name: String, openingBalance: Double): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                val id  = IdGenerator.generate()
                db.database.transaction {
                    db.ledgerQueries.insertCashAccount(id, name, openingBalance, now, now)
                    if (openingBalance > 0) {
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "CASH", account_id = id,
                            entry_type = "CREDIT", reference_type = "OPENING", reference_id = null,
                            amount = openingBalance, balance_after = openingBalance,
                            description = "Opening balance — $name", created_at = now
                        )
                    }
                }
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to add cash account") }
        }

    override suspend fun deactivateCashAccount(id: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.ledgerQueries.deactivateCashAccount(Clock.System.now().toEpochMilliseconds(), id)
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    // ── Bank Accounts ─────────────────────────────────────────────────────────

    override suspend fun getAllBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBank))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load bank accounts") }
        }

    override suspend fun addBankAccount(account: BankAccount): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                val id  = IdGenerator.generate()
                db.database.transaction {
                    db.ledgerQueries.insertBankAccount(
                        id             = id,
                        bank_name      = account.bankName,
                        account_title  = account.accountTitle,
                        account_number = account.accountNumber,
                        iban           = account.iban,
                        balance        = account.balance,
                        created_at     = now,
                        updated_at     = now
                    )
                    if (account.balance > 0) {
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "BANK", account_id = id,
                            entry_type = "CREDIT", reference_type = "OPENING", reference_id = null,
                            amount = account.balance, balance_after = account.balance,
                            description = "Opening balance — ${account.bankName} (${account.accountNumber})",
                            created_at = now
                        )
                    }
                }
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to add bank account") }
        }

    override suspend fun updateBankAccount(account: BankAccount): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.ledgerQueries.updateBankAccountDetails(
                    bank_name      = account.bankName,
                    account_title  = account.accountTitle,
                    account_number = account.accountNumber,
                    iban           = account.iban,
                    updated_at     = Clock.System.now().toEpochMilliseconds(),
                    id             = account.id
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to update bank account") }
        }

    override suspend fun deactivateBankAccount(id: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.ledgerQueries.deactivateBankAccount(Clock.System.now().toEpochMilliseconds(), id)
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun adjustBankBalance(id: String, newBalance: Double): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now     = Clock.System.now().toEpochMilliseconds()
                val current = db.ledgerQueries.getBankAccountById(id).executeAsOne()
                val delta   = newBalance - current.balance
                db.database.transaction {
                    db.ledgerQueries.updateBankBalance(newBalance, now, id)
                    db.ledgerQueries.insertLedgerEntry(
                        id = IdGenerator.generate(), account_type = "BANK", account_id = id,
                        entry_type = if (delta >= 0) "CREDIT" else "DEBIT",
                        reference_type = "ADJUSTMENT", reference_id = null,
                        amount = kotlin.math.abs(delta), balance_after = newBalance,
                        description = "Manual balance adjustment → ${current.bank_name}",
                        created_at = now
                    )
                }
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to adjust bank balance") }
        }

    // ── Transfer ──────────────────────────────────────────────────────────────

    override suspend fun transfer(
        fromType: AccountType, fromId: String,
        toType  : AccountType, toId  : String,
        amount  : Double, notes: String
    ): AppResult<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val now        = Clock.System.now().toEpochMilliseconds()
            val transferId = IdGenerator.generate()

            db.database.transaction {
                // Debit from account
                val fromDesc: String
                when (fromType) {
                    AccountType.CASH -> {
                        val acc = db.ledgerQueries.getCashAccountById(fromId).executeAsOne()
                        val nb  = (acc.balance - amount).coerceAtLeast(0.0)
                        db.ledgerQueries.updateCashBalance(nb, now, fromId)
                        fromDesc = acc.name
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "CASH", account_id = fromId,
                            entry_type = "DEBIT", reference_type = "TRANSFER_OUT", reference_id = transferId,
                            amount = amount, balance_after = nb, description = "Transfer out to $toType${if (notes.isNotBlank()) " · $notes" else ""}", created_at = now
                        )
                    }
                    AccountType.BANK -> {
                        val acc = db.ledgerQueries.getBankAccountById(fromId).executeAsOne()
                        val nb  = (acc.balance - amount).coerceAtLeast(0.0)
                        db.ledgerQueries.updateBankBalance(nb, now, fromId)
                        fromDesc = "${acc.bank_name} ${acc.account_number}"
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "BANK", account_id = fromId,
                            entry_type = "DEBIT", reference_type = "TRANSFER_OUT", reference_id = transferId,
                            amount = amount, balance_after = nb, description = "Transfer out to $toType${if (notes.isNotBlank()) " · $notes" else ""}", created_at = now
                        )
                    }
                    else -> rollback()
                }

                // Credit to account
                when (toType) {
                    AccountType.CASH -> {
                        val acc = db.ledgerQueries.getCashAccountById(toId).executeAsOne()
                        val nb  = acc.balance + amount
                        db.ledgerQueries.updateCashBalance(nb, now, toId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "CASH", account_id = toId,
                            entry_type = "CREDIT", reference_type = "TRANSFER_IN", reference_id = transferId,
                            amount = amount, balance_after = nb, description = "Transfer in from $fromDesc${if (notes.isNotBlank()) " · $notes" else ""}", created_at = now
                        )
                    }
                    AccountType.BANK -> {
                        val acc = db.ledgerQueries.getBankAccountById(toId).executeAsOne()
                        val nb  = acc.balance + amount
                        db.ledgerQueries.updateBankBalance(nb, now, toId)
                        db.ledgerQueries.insertLedgerEntry(
                            id = IdGenerator.generate(), account_type = "BANK", account_id = toId,
                            entry_type = "CREDIT", reference_type = "TRANSFER_IN", reference_id = transferId,
                            amount = amount, balance_after = nb, description = "Transfer in from $fromDesc${if (notes.isNotBlank()) " · $notes" else ""}", created_at = now
                        )
                    }
                    else -> rollback()
                }

                // Log account_transfer row
                db.ledgerQueries.insertAccountTransfer(
                    id                = transferId,
                    from_account_type = fromType.name,
                    from_account_id   = fromId,
                    to_account_type   = toType.name,
                    to_account_id     = toId,
                    amount            = amount,
                    notes             = notes.takeIf { it.isNotBlank() },
                    transferred_at    = now
                )
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Transfer failed") }
    }

    // ── Ledger History ────────────────────────────────────────────────────────

    override suspend fun getLedgerEntries(startMs: Long, endMs: Long): AppResult<List<LedgerEntry>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getLedgerByDateRange(startMs, endMs).executeAsList().map(::mapEntry))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load ledger") }
        }

    override suspend fun getLedgerByAccount(accountType: AccountType, accountId: String, limit: Long): AppResult<List<LedgerEntry>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getLedgerByAccount(accountType.name, accountId, limit, 0L)
                        .executeAsList().map(::mapEntry)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load ledger") }
        }

    override fun getLedgerEntriesPaged(startMs: Long, endMs: Long): Flow<PagingData<LedgerEntry>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { LedgerPagingSource(db.ledgerQueries, startMs, endMs, ::mapEntry) }
        ).flow
    }

    // ── Aggregates ────────────────────────────────────────────────────────────

    override suspend fun getTotalLiquidBalance(): AppResult<Double> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getTotalLiquidBalance().executeAsOne())
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}
