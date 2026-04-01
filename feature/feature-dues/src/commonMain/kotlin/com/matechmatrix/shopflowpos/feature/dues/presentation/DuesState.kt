package com.matechmatrix.shopflowpos.feature.dues.presentation

import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType

enum class DuesTab { RECEIVABLE, PAYABLE }   // Receivable = customer owes me; Payable = I owe supplier

data class DuesState(
    val isLoading      : Boolean = true,
    val activeTab      : DuesTab = DuesTab.RECEIVABLE,
    val currencySymbol : String  = "Rs.",
    val error          : String? = null,

    // Receivable — customer owes me
    val customersWithDues: List<Customer>      = emptyList(),
    val salesWithDue     : List<Sale>          = emptyList(),
    val selectedCustomerId: String?            = null,     // filter sales by customer

    // Payable — I owe supplier
    val suppliersWithDues   : List<Supplier>       = emptyList(),
    val purchaseOrdersWithDue: List<PurchaseOrder> = emptyList(),
    val selectedSupplierId  : String?              = null,

    // Payment dialog (shared for both sides)
    val payDialog        : PayDialogState?    = null,
    val cashAccounts     : List<CashAccount>  = emptyList(),
    val bankAccounts     : List<BankAccount>  = emptyList(),
    val isProcessing     : Boolean            = false
) {
    val totalReceivable: Double get() = customersWithDues.sumOf { it.outstandingBalance }
    val totalPayable   : Double get() = suppliersWithDues.sumOf { it.outstandingBalance }

    val filteredSales: List<Sale> get() =
        if (selectedCustomerId == null) salesWithDue
        else salesWithDue.filter { it.customerId == selectedCustomerId }

    val filteredPurchaseOrders: List<PurchaseOrder> get() =
        if (selectedSupplierId == null) purchaseOrdersWithDue
        else purchaseOrdersWithDue.filter { it.supplierId == selectedSupplierId }
}

data class PayDialogState(
    val type          : DuesTab,               // RECEIVABLE = collect; PAYABLE = pay
    val referenceId   : String,                // sale.id or purchaseOrder.id
    val partyName     : String,                // customer or supplier name
    val invoiceRef    : String,                // invoice number or PO number
    val dueAmount     : Double,
    val amount        : String        = "",
    val accountType   : AccountType   = AccountType.CASH,
    val accountId     : String        = "default_cash",
    val error         : String?       = null
)

sealed class DuesIntent {
    data object Load                                              : DuesIntent()
    data class  SwitchTab(val tab: DuesTab)                      : DuesIntent()

    // Receivable filters
    data class  FilterByCustomer(val customerId: String?)        : DuesIntent()

    // Payable filters
    data class  FilterBySupplier(val supplierId: String?)        : DuesIntent()

    // Payment dialog
    data class  ShowCollectDialog(val sale: Sale)                : DuesIntent()
    data class  ShowPayDialog(val order: PurchaseOrder)          : DuesIntent()
    data object DismissPayDialog                                  : DuesIntent()
    data class  SetAmount(val v: String)                         : DuesIntent()
    data class  SetAccountType(val v: AccountType)               : DuesIntent()
    data class  SetAccountId(val v: String)                      : DuesIntent()
    data object ConfirmPayment                                   : DuesIntent()
}

sealed class DuesEffect {
    data class ShowToast(val message: String) : DuesEffect()
}