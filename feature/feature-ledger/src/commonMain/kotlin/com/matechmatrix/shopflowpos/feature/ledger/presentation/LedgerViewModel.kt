package com.matechmatrix.shopflowpos.feature.ledger.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.ledger.domain.usecase.*
import kotlinx.datetime.Clock

private const val HISTORY_DAYS = 30L
private const val MS_PER_DAY   = 86_400_000L

class LedgerViewModel(
    private val getOverview   : GetLedgerOverviewUseCase,
    private val adjustCash    : AdjustCashBalanceUseCase,
    private val adjustBank    : AdjustBankBalanceUseCase,
    private val saveBankAccUC : SaveBankAccountUseCase,
    private val transferUC    : TransferBetweenAccountsUseCase,
    private val repo          : com.matechmatrix.shopflowpos.feature.ledger.domain.repository.LedgerRepository
) : MviViewModel<LedgerState, LedgerIntent, LedgerEffect>(LedgerState()) {

    init { onIntent(LedgerIntent.Load) }

    override suspend fun handleIntent(intent: LedgerIntent) {
        when (intent) {
            LedgerIntent.Load -> load()

            // ── Cash adjust ────────────────────────────────────────────────────
            is LedgerIntent.ShowCashDialog -> setState {
                copy(showCashDialog = true, adjustingCashAccount = intent.account, cashInputAmount = intent.account.balance.toString())
            }
            is LedgerIntent.SetCashInput   -> setState { copy(cashInputAmount = intent.amount) }
            LedgerIntent.DismissCashDialog -> setState { copy(showCashDialog = false, adjustingCashAccount = null) }
            LedgerIntent.SaveCashBalance   -> {
                val acc    = state.value.adjustingCashAccount ?: return
                val amount = state.value.cashInputAmount.toDoubleOrNull()
                if (amount == null || amount < 0) { setEffect(LedgerEffect.ShowToast("Enter a valid amount")); return }
                when (val r = adjustCash(acc.id, amount)) {
                    is AppResult.Success -> {
                        setState { copy(showCashDialog = false) }
                        setEffect(LedgerEffect.ShowToast("${acc.name} updated to ${state.value.currencySymbol} $amount"))
                        load()
                    }
                    is AppResult.Error -> setEffect(LedgerEffect.ShowToast(r.message))
                    else -> {}
                }
            }

            // ── Add cash account ───────────────────────────────────────────────
            LedgerIntent.ShowAddCashDialog    -> setState { copy(showAddCashDialog = true, addCashName = "", addCashBalance = "0", addCashError = null) }
            LedgerIntent.DismissAddCashDialog -> setState { copy(showAddCashDialog = false) }
            is LedgerIntent.SetAddCashName    -> setState { copy(addCashName = intent.v) }
            is LedgerIntent.SetAddCashBalance -> setState { copy(addCashBalance = intent.v) }
            LedgerIntent.SaveAddCashAccount   -> {
                val s = state.value
                if (s.addCashName.isBlank()) { setState { copy(addCashError = "Name is required") }; return }
                val balance = s.addCashBalance.toDoubleOrNull() ?: 0.0
                when (val r = repo.addCashAccount(s.addCashName.trim(), balance)) {
                    is AppResult.Success -> {
                        setState { copy(showAddCashDialog = false) }
                        setEffect(LedgerEffect.ShowToast("Cash account added"))
                        load()
                    }
                    is AppResult.Error -> setState { copy(addCashError = r.message) }
                    else -> {}
                }
            }

            // ── Bank account ───────────────────────────────────────────────────
            LedgerIntent.ShowAddBankDialog -> setState {
                copy(showBankDialog = true, editingBank = null, bankFormTitle = "", bankFormBankName = "", bankFormAccNum = "", bankFormIban = "", bankFormBalance = "0", bankFormError = null)
            }
            is LedgerIntent.ShowEditBankDialog -> setState {
                val b = intent.account
                copy(showBankDialog = true, editingBank = b, bankFormTitle = b.accountTitle, bankFormBankName = b.bankName, bankFormAccNum = b.accountNumber, bankFormIban = b.iban ?: "", bankFormBalance = b.balance.toString(), bankFormError = null)
            }
            LedgerIntent.DismissBankDialog -> setState { copy(showBankDialog = false, editingBank = null) }

            LedgerIntent.SaveBankAccount -> {
                val s = state.value
                setState { copy(isSaving = true, bankFormError = null) }
                val account = BankAccount(
                    id            = s.editingBank?.id ?: IdGenerator.generate(),
                    bankName      = s.bankFormBankName.trim(),
                    accountTitle  = s.bankFormTitle.trim(),
                    accountNumber = s.bankFormAccNum.trim(),
                    iban          = s.bankFormIban.takeIf { it.isNotBlank() },
                    balance       = s.bankFormBalance.toDoubleOrNull() ?: 0.0,
                    isActive      = true,
                    createdAt     = s.editingBank?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
                    updatedAt     = Clock.System.now().toEpochMilliseconds()
                )
                when (val r = saveBankAccUC(account, s.editingBank == null)) {
                    is AppResult.Success -> {
                        setState { copy(isSaving = false, showBankDialog = false) }
                        setEffect(LedgerEffect.ShowToast(if (s.editingBank == null) "Bank account added" else "Account updated"))
                        load()
                    }
                    is AppResult.Error -> setState { copy(isSaving = false, bankFormError = r.message) }
                    else -> setState { copy(isSaving = false) }
                }
            }

            is LedgerIntent.ConfirmDeleteBank -> setState { copy(showDeleteBankId = intent.id.ifBlank { null }) }
            LedgerIntent.DeleteBankAccount    -> {
                val id = state.value.showDeleteBankId ?: return
                setState { copy(showDeleteBankId = null) }
                repo.deactivateBankAccount(id)
                setEffect(LedgerEffect.ShowToast("Bank account removed"))
                load()
            }

            // ── Inline bank balance ────────────────────────────────────────────
            is LedgerIntent.StartAdjustBankBalance -> setState { copy(adjustingBankId = intent.id, bankBalanceInput = intent.current.toString()) }
            is LedgerIntent.SetBankBalanceInput    -> setState { copy(bankBalanceInput = intent.v) }
            LedgerIntent.CancelAdjustBankBalance   -> setState { copy(adjustingBankId = null) }
            LedgerIntent.SaveBankBalance           -> {
                val id  = state.value.adjustingBankId ?: return
                val amt = state.value.bankBalanceInput.toDoubleOrNull()
                if (amt == null || amt < 0) { setEffect(LedgerEffect.ShowToast("Enter a valid amount")); return }
                when (adjustBank(id, amt)) {
                    is AppResult.Success -> {
                        setState { copy(adjustingBankId = null) }
                        setEffect(LedgerEffect.ShowToast("Balance updated"))
                        load()
                    }
                    is AppResult.Error -> setEffect(LedgerEffect.ShowToast("Failed to update"))
                    else -> {}
                }
            }

            // ── Transfer ────────────────────────────────────────────────────────
            LedgerIntent.ShowTransferDialog -> setState {
                val defaultCash = cashAccounts.firstOrNull()?.id ?: "default_cash"
                val defaultBank = bankAccounts.firstOrNull()?.id ?: ""
                copy(showTransferDialog = true, transferFromType = AccountType.CASH, transferFromId = defaultCash, transferToType = AccountType.BANK, transferToId = defaultBank, transferAmount = "", transferNotes = "", transferError = null)
            }
            LedgerIntent.DismissTransferDialog -> setState { copy(showTransferDialog = false, transferError = null) }
            is LedgerIntent.SetTransferFromType -> setState { copy(transferFromType = intent.v) }
            is LedgerIntent.SetTransferFromId   -> setState { copy(transferFromId = intent.v) }
            is LedgerIntent.SetTransferToType   -> setState { copy(transferToType = intent.v) }
            is LedgerIntent.SetTransferToId     -> setState { copy(transferToId = intent.v) }
            is LedgerIntent.SetTransferAmount   -> setState { copy(transferAmount = intent.v) }
            is LedgerIntent.SetTransferNotes    -> setState { copy(transferNotes = intent.v) }
            LedgerIntent.ExecuteTransfer        -> doTransfer()

            // ── Form field bindings ────────────────────────────────────────────
            is LedgerIntent.BankFormTitle    -> setState { copy(bankFormTitle = intent.v) }
            is LedgerIntent.BankFormBankName -> setState { copy(bankFormBankName = intent.v) }
            is LedgerIntent.BankFormAccNum   -> setState { copy(bankFormAccNum = intent.v) }
            is LedgerIntent.BankFormIban     -> setState { copy(bankFormIban = intent.v) }
            is LedgerIntent.BankFormBalance  -> setState { copy(bankFormBalance = intent.v) }
        }
    }

    private suspend fun load() {
        setState { copy(isLoading = true) }
        val startMs = Clock.System.now().toEpochMilliseconds() - HISTORY_DAYS * MS_PER_DAY
        val overview = getOverview(startMs)
        setState {
            copy(
                isLoading     = false,
                cashAccounts  = overview.cashAccounts,
                bankAccounts  = overview.bankAccounts,
                ledgerEntries = overview.ledgerEntries,
                currencySymbol = overview.currencySymbol
            )
        }
    }

    private suspend fun doTransfer() {
        val s = state.value
        val amount = s.transferAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) { setState { copy(transferError = "Enter a valid amount") }; return }
        if (s.transferFromId.isBlank()) { setState { copy(transferError = "Select source account") }; return }
        if (s.transferToId.isBlank()) { setState { copy(transferError = "Select destination account") }; return }

        setState { copy(isTransferring = true, transferError = null) }
        when (val r = transferUC(s.transferFromType, s.transferFromId, s.transferToType, s.transferToId, amount, s.transferNotes)) {
            is AppResult.Success -> {
                setState { copy(isTransferring = false, showTransferDialog = false) }
                setEffect(LedgerEffect.ShowToast("Transfer of ${s.currencySymbol} ${amount.toLong()} completed"))
                load()
            }
            is AppResult.Error -> setState { copy(isTransferring = false, transferError = r.message) }
            else               -> setState { copy(isTransferring = false) }
        }
    }
}