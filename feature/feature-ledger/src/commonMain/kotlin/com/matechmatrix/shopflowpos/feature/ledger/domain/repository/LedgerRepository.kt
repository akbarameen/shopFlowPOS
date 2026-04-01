package com.matechmatrix.shopflowpos.feature.ledger.domain.repository

import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import kotlinx.coroutines.flow.Flow

interface LedgerRepository {
    // ── Cash Accounts ─────────────────────────────────────────────────────────
    suspend fun getAllCashAccounts(): AppResult<List<CashAccount>>
    /** Adjusts a cash account balance and logs a ledger ADJUSTMENT entry. */
    suspend fun adjustCashBalance(accountId: String, newBalance: Double): AppResult<Unit>
    suspend fun addCashAccount(name: String, openingBalance: Double): AppResult<Unit>
    suspend fun deactivateCashAccount(id: String): AppResult<Unit>

    // ── Bank Accounts ─────────────────────────────────────────────────────────
    suspend fun getAllBankAccounts(): AppResult<List<BankAccount>>
    suspend fun addBankAccount(account: BankAccount): AppResult<Unit>
    suspend fun updateBankAccount(account: BankAccount): AppResult<Unit>
    suspend fun deactivateBankAccount(id: String): AppResult<Unit>
    /** Adjusts a bank account balance and logs a ledger ADJUSTMENT entry. */
    suspend fun adjustBankBalance(id: String, newBalance: Double): AppResult<Unit>

    // ── Account Transfer ──────────────────────────────────────────────────────
    /**
     * Atomically moves [amount] between two accounts and logs both
     * a DEBIT (from) and CREDIT (to) ledger entry + account_transfer row.
     */
    suspend fun transfer(
        fromType   : AccountType, fromId: String,
        toType     : AccountType, toId  : String,
        amount     : Double,
        notes      : String
    ): AppResult<Unit>

    // ── Ledger History ────────────────────────────────────────────────────────
    suspend fun getLedgerEntries(startMs: Long, endMs: Long): AppResult<List<LedgerEntry>>
    suspend fun getLedgerByAccount(accountType: AccountType, accountId: String, limit: Long): AppResult<List<LedgerEntry>>
    
    fun getLedgerEntriesPaged(startMs: Long, endMs: Long): Flow<PagingData<LedgerEntry>>

    // ── Aggregates ────────────────────────────────────────────────────────────
    suspend fun getTotalLiquidBalance(): AppResult<Double>

    // ── Settings ──────────────────────────────────────────────────────────────
    suspend fun getCurrencySymbol(): String
}