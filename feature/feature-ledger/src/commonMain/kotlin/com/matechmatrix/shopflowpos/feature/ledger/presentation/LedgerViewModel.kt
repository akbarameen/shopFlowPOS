package com.matechmatrix.shopflowpos.feature.ledger.presentation

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.feature.ledger.domain.repository.LedgerRepository
import kotlinx.datetime.Clock

class LedgerViewModel(
    private val repo: LedgerRepository
) : MviViewModel<LedgerState, LedgerIntent, LedgerEffect>(LedgerState()) {

    init {
        onIntent(LedgerIntent.Load)
    }

    override suspend fun handleIntent(intent: LedgerIntent) {
        when (intent) {
            LedgerIntent.Load -> loadAll()

            LedgerIntent.ShowCashDialog -> setState {
                copy(showCashDialog = true, cashInputAmount = cashBalance.toString())
            }
            is LedgerIntent.SetCashInput -> setState { copy(cashInputAmount = intent.amount) }
            LedgerIntent.DismissCashDialog -> setState { copy(showCashDialog = false) }
            LedgerIntent.SaveCashBalance -> {
                val amount = currentState.cashInputAmount.toDoubleOrNull()
                if (amount == null || amount < 0) {
                    setEffect(LedgerEffect.ShowToast("Enter a valid amount"))
                    return
                }
                repo.setCashBalance(amount)
                setState { copy(showCashDialog = false, cashBalance = amount) }
                setEffect(LedgerEffect.ShowToast("Cash balance updated"))
                loadAll()
            }

            LedgerIntent.ShowAddBankDialog -> setState {
                copy(showBankDialog = true, editingBank = null, bankFormName = "", bankFormBankName = "",
                    bankFormAccNumber = "", bankFormBalance = "0", bankFormIsDefault = false, bankFormError = null)
            }
            is LedgerIntent.ShowEditBankDialog -> setState {
                val b = intent.account
                copy(showBankDialog = true, editingBank = b, bankFormName = b.accountTitle,
                    bankFormBankName = b.bankName, bankFormAccNumber = b.accountNumber,
                    bankFormBalance = b.balance.toString(), bankFormIsDefault = false, bankFormError = null)
            }
            LedgerIntent.DismissBankDialog -> setState { copy(showBankDialog = false, editingBank = null) }

            LedgerIntent.SaveBankAccount -> saveBankAccount()

            is LedgerIntent.ConfirmDeleteBank -> setState { copy(showDeleteBankId = intent.id) }
            LedgerIntent.DeleteBankAccount -> {
                val id = currentState.showDeleteBankId ?: return
                setState { copy(showDeleteBankId = null) }
                repo.deleteBankAccount(id)
                loadAll()
            }

            is LedgerIntent.UpdateBankBalance -> {
                repo.updateBankBalance(intent.id, intent.newBalance)
                loadAll()
            }

            is LedgerIntent.BankFormName      -> setState { copy(bankFormName = intent.v) }
            is LedgerIntent.BankFormBankName  -> setState { copy(bankFormBankName = intent.v) }
            is LedgerIntent.BankFormAccNumber -> setState { copy(bankFormAccNumber = intent.v) }
            is LedgerIntent.BankFormBalance   -> setState { copy(bankFormBalance = intent.v) }
            is LedgerIntent.BankFormIsDefault -> setState { copy(bankFormIsDefault = intent.v) }
        }
    }

    private suspend fun loadAll() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        
        val cash = when (val r = repo.getCashBalance()) {
            is AppResult.Success -> r.data
            else -> 0.0
        }
        
        val banks = when (val r = repo.getBankAccounts()) {
            is AppResult.Success -> r.data
            else -> emptyList()
        }
        
        val range = DateTimeUtils.todayRange()
        val entries = when (val r = repo.getLedgerEntries(range.first - 30L * 86400000L, range.second)) {
            is AppResult.Success -> r.data
            else -> emptyList()
        }
        
        setState {
            copy(
                isLoading = false, 
                cashBalance = cash, 
                bankAccounts = banks,
                ledgerEntries = entries.sortedByDescending { it.createdAt }, 
                currencySymbol = currency
            )
        }
    }

    private suspend fun saveBankAccount() {
        val s = currentState
        if (s.bankFormName.isBlank()) { 
            setState { copy(bankFormError = "Account title required") }
            return 
        }
        if (s.bankFormBankName.isBlank()) { 
            setState { copy(bankFormError = "Bank name required") }
            return 
        }
        
        val balance = s.bankFormBalance.toDoubleOrNull() ?: 0.0
        val account = BankAccount(
            id = s.editingBank?.id ?: IdGenerator.generate(),
            accountTitle = s.bankFormName.trim(),
            bankName = s.bankFormBankName.trim(),
            accountNumber = s.bankFormAccNumber.trim(),
            balance = balance,
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        
        val result = if (s.editingBank != null) repo.updateBankAccount(account) else repo.addBankAccount(account)
        when (result) {
            is AppResult.Success -> {
                setState { copy(showBankDialog = false, editingBank = null) }
                setEffect(LedgerEffect.ShowToast(if (s.editingBank != null) "Account updated" else "Account added"))
                loadAll()
            }
            is AppResult.Error -> setState { copy(bankFormError = result.message) }
            else -> {}
        }
    }
}
