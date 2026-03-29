package com.matechmatrix.shopflowpos.feature.ledger.domain.repository
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.LedgerEntry

interface LedgerRepository {
    suspend fun getCashBalance(): AppResult<Double>
    suspend fun setCashBalance(amount: Double): AppResult<Unit>
    suspend fun getBankAccounts(): AppResult<List<BankAccount>>
    suspend fun addBankAccount(account: BankAccount): AppResult<Unit>
    suspend fun updateBankAccount(account: BankAccount): AppResult<Unit>
    suspend fun deleteBankAccount(id: String): AppResult<Unit>
    suspend fun updateBankBalance(id: String, newBalance: Double): AppResult<Unit>
    suspend fun getLedgerEntries(startMs: Long, endMs: Long): AppResult<List<LedgerEntry>>
    suspend fun isFirstRun(): Boolean
    suspend fun getCurrencySymbol(): String
}