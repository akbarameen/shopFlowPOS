package com.matechmatrix.shopflowpos.feature.ledger.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.ledger.domain.repository.LedgerRepository

class GetLedgerOverviewUseCase(private val repo: LedgerRepository) {
    data class Overview(
        val cashAccounts  : List<CashAccount>,
        val bankAccounts  : List<BankAccount>,
        val ledgerEntries : List<LedgerEntry>,
        val totalLiquid   : Double,
        val currencySymbol: String
    )
    suspend operator fun invoke(historyStartMs: Long): Overview {
        val cash     = (repo.getAllCashAccounts() as? AppResult.Success)?.data ?: emptyList()
        val banks    = (repo.getAllBankAccounts() as? AppResult.Success)?.data ?: emptyList()
        val entries  = (repo.getLedgerEntries(historyStartMs, Long.MAX_VALUE) as? AppResult.Success)?.data ?: emptyList()
        val total    = (repo.getTotalLiquidBalance() as? AppResult.Success)?.data ?: 0.0
        return Overview(cash, banks, entries.sortedByDescending { it.createdAt }, total, repo.getCurrencySymbol())
    }
}

class AdjustCashBalanceUseCase(private val repo: LedgerRepository) {
    suspend operator fun invoke(accountId: String, newBalance: Double): AppResult<Unit> {
        if (newBalance < 0) return AppResult.Error("Balance cannot be negative")
        return repo.adjustCashBalance(accountId, newBalance)
    }
}

class AdjustBankBalanceUseCase(private val repo: LedgerRepository) {
    suspend operator fun invoke(id: String, newBalance: Double): AppResult<Unit> {
        if (newBalance < 0) return AppResult.Error("Balance cannot be negative")
        return repo.adjustBankBalance(id, newBalance)
    }
}

class SaveBankAccountUseCase(private val repo: LedgerRepository) {
    suspend operator fun invoke(account: BankAccount, isNew: Boolean): AppResult<Unit> {
        if (account.accountTitle.isBlank()) return AppResult.Error("Account title is required")
        if (account.bankName.isBlank())     return AppResult.Error("Bank name is required")
        return if (isNew) repo.addBankAccount(account) else repo.updateBankAccount(account)
    }
}

class TransferBetweenAccountsUseCase(private val repo: LedgerRepository) {
    suspend operator fun invoke(
        fromType : AccountType, fromId: String,
        toType   : AccountType, toId  : String,
        amount   : Double, notes: String
    ): AppResult<Unit> {
        if (amount <= 0) return AppResult.Error("Amount must be greater than zero")
        if (fromId == toId && fromType == toType) return AppResult.Error("Cannot transfer to the same account")
        return repo.transfer(fromType, fromId, toType, toId, amount, notes)
    }
}