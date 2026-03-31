package com.matechmatrix.shopflowpos.feature.ledger.presentation

import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType

data class LedgerState(
    val isLoading       : Boolean            = true,
    val cashAccounts    : List<CashAccount>  = emptyList(),
    val bankAccounts    : List<BankAccount>  = emptyList(),
    val ledgerEntries   : List<LedgerEntry>  = emptyList(),
    val currencySymbol  : String             = "Rs.",
    val error           : String?            = null,

    // Adjust cash dialog (handles any cash account)
    val showCashDialog      : Boolean        = false,
    val adjustingCashAccount: CashAccount?   = null,
    val cashInputAmount     : String         = "",

    // Add cash account dialog
    val showAddCashDialog   : Boolean        = false,
    val addCashName         : String         = "",
    val addCashBalance      : String         = "0",
    val addCashError        : String?        = null,

    // Bank account dialog
    val showBankDialog  : Boolean            = false,
    val editingBank     : BankAccount?       = null,
    val bankFormTitle   : String             = "",
    val bankFormBankName: String             = "",
    val bankFormAccNum  : String             = "",
    val bankFormIban    : String             = "",
    val bankFormBalance : String             = "0",
    val bankFormError   : String?            = null,
    val isSaving        : Boolean            = false,

    // Adjust bank balance inline
    val adjustingBankId : String?            = null,
    val bankBalanceInput: String             = "",

    // Transfer dialog
    val showTransferDialog   : Boolean       = false,
    val transferFromType     : AccountType   = AccountType.CASH,
    val transferFromId       : String        = "",
    val transferToType       : AccountType   = AccountType.BANK,
    val transferToId         : String        = "",
    val transferAmount       : String        = "",
    val transferNotes        : String        = "",
    val transferError        : String?       = null,
    val isTransferring       : Boolean       = false,

    // Delete confirm
    val showDeleteBankId: String?            = null,
) {
    val totalCash   : Double get() = cashAccounts.sumOf { it.balance }
    val totalBank   : Double get() = bankAccounts.sumOf { it.balance }
    val totalBalance: Double get() = totalCash + totalBank

    val defaultCashAccount: CashAccount? get() = cashAccounts.firstOrNull { it.id == "default_cash" } ?: cashAccounts.firstOrNull()
}

sealed class LedgerIntent {
    data object Load                                                           : LedgerIntent()

    // Cash adjust
    data class  ShowCashDialog(val account: CashAccount)                      : LedgerIntent()
    data class  SetCashInput(val amount: String)                               : LedgerIntent()
    data object SaveCashBalance                                                : LedgerIntent()
    data object DismissCashDialog                                              : LedgerIntent()

    // Add cash account
    data object ShowAddCashDialog                                              : LedgerIntent()
    data object DismissAddCashDialog                                           : LedgerIntent()
    data class  SetAddCashName(val v: String)                                  : LedgerIntent()
    data class  SetAddCashBalance(val v: String)                               : LedgerIntent()
    data object SaveAddCashAccount                                             : LedgerIntent()

    // Bank
    data object ShowAddBankDialog                                              : LedgerIntent()
    data class  ShowEditBankDialog(val account: BankAccount)                   : LedgerIntent()
    data object DismissBankDialog                                              : LedgerIntent()
    data object SaveBankAccount                                                : LedgerIntent()
    data class  ConfirmDeleteBank(val id: String)                              : LedgerIntent()
    data object DeleteBankAccount                                              : LedgerIntent()

    // Inline bank balance adjust
    data class  StartAdjustBankBalance(val id: String, val current: Double)    : LedgerIntent()
    data class  SetBankBalanceInput(val v: String)                             : LedgerIntent()
    data object SaveBankBalance                                                : LedgerIntent()
    data object CancelAdjustBankBalance                                        : LedgerIntent()

    // Transfer
    data object ShowTransferDialog                                             : LedgerIntent()
    data object DismissTransferDialog                                          : LedgerIntent()
    data class  SetTransferFromType(val v: AccountType)                        : LedgerIntent()
    data class  SetTransferFromId(val v: String)                               : LedgerIntent()
    data class  SetTransferToType(val v: AccountType)                          : LedgerIntent()
    data class  SetTransferToId(val v: String)                                 : LedgerIntent()
    data class  SetTransferAmount(val v: String)                               : LedgerIntent()
    data class  SetTransferNotes(val v: String)                                : LedgerIntent()
    data object ExecuteTransfer                                                : LedgerIntent()

    // Bank form fields
    data class BankFormTitle  (val v: String) : LedgerIntent()
    data class BankFormBankName(val v: String): LedgerIntent()
    data class BankFormAccNum (val v: String) : LedgerIntent()
    data class BankFormIban   (val v: String) : LedgerIntent()
    data class BankFormBalance(val v: String) : LedgerIntent()
}

sealed class LedgerEffect {
    data class ShowToast(val message: String) : LedgerEffect()
}