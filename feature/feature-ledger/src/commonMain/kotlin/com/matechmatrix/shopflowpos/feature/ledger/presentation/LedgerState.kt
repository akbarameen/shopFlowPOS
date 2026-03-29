package com.matechmatrix.shopflowpos.feature.ledger.presentation

import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.LedgerEntry

data class LedgerState(
    val isLoading: Boolean = true,
    val cashBalance: Double = 0.0,
    val bankAccounts: List<BankAccount> = emptyList(),
    val ledgerEntries: List<LedgerEntry> = emptyList(),
    val currencySymbol: String = "Rs.",
    // Cash edit dialog
    val showCashDialog: Boolean = false,
    val cashInputAmount: String = "",
    // Bank account dialog
    val showBankDialog: Boolean = false,
    val editingBank: BankAccount? = null,
    val bankFormName: String = "",
    val bankFormBankName: String = "",
    val bankFormAccNumber: String = "",
    val bankFormBalance: String = "",
    val bankFormIsDefault: Boolean = false,
    val bankFormError: String? = null,
    // Confirm delete
    val showDeleteBankId: String? = null,
    val error: String? = null
) {
    val totalBalance: Double get() = cashBalance + bankAccounts.sumOf { it.balance }
}

sealed class LedgerIntent {
    data object Load : LedgerIntent()
    // Cash
    data object ShowCashDialog : LedgerIntent()
    data class SetCashInput(val amount: String) : LedgerIntent()
    data object SaveCashBalance : LedgerIntent()
    data object DismissCashDialog : LedgerIntent()
    // Bank
    data object ShowAddBankDialog : LedgerIntent()
    data class ShowEditBankDialog(val account: BankAccount) : LedgerIntent()
    data object DismissBankDialog : LedgerIntent()
    data object SaveBankAccount : LedgerIntent()
    data class ConfirmDeleteBank(val id: String) : LedgerIntent()
    data object DeleteBankAccount : LedgerIntent()
    data class UpdateBankBalance(val id: String, val newBalance: Double) : LedgerIntent()
    // Form
    data class BankFormName(val v: String) : LedgerIntent()
    data class BankFormBankName(val v: String) : LedgerIntent()
    data class BankFormAccNumber(val v: String) : LedgerIntent()
    data class BankFormBalance(val v: String) : LedgerIntent()
    data class BankFormIsDefault(val v: Boolean) : LedgerIntent()
}

sealed class LedgerEffect {
    data class ShowToast(val message: String) : LedgerEffect()
}
