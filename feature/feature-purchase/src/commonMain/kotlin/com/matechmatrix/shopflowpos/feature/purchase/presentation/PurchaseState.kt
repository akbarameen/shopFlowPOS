package com.matechmatrix.shopflowpos.feature.purchase.presentation

import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSource
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSourceType
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseCartItem

enum class PurchaseTab { NEW_ORDER, HISTORY }

// ── State ─────────────────────────────────────────────────────────────────────

data class PurchaseState(
    val isLoading       : Boolean          = true,
    val activeTab       : PurchaseTab      = PurchaseTab.NEW_ORDER,
    val currencySymbol  : String           = "Rs.",
    val error           : String?          = null,

    // Reference lists
    val suppliers       : List<Supplier>   = emptyList(),
    val customers       : List<Customer>   = emptyList(),
    val cashAccounts    : List<CashAccount>  = emptyList(),
    val bankAccounts    : List<BankAccount>  = emptyList(),

    // Source selection
    val sourceType      : PurchaseSourceType = PurchaseSourceType.SUPPLIER,
    val sourceQuery     : String           = "",   // search text
    val selectedSource  : PurchaseSource?  = null, // null = not yet confirmed
    // New source form (when typing a new name not in list)
    val newSourcePhone  : String           = "",
    val newSourceAddress: String           = "",
    val newSourceCity   : String           = "",
    val newSourceEmail  : String           = "",
    val supplierInvoiceRef: String         = "",

    // Cart
    val cart            : List<PurchaseCartItem> = emptyList(),

    // Product bottom sheet (similar to inventory add form)
    val showProductSheet: Boolean          = false,
    // Product sheet form state
    val sheetProductSearch  : String       = "",
    val sheetSearchResults  : List<Product> = emptyList(),
    val sheetSelectedProduct: Product?     = null,  // from search; null = new product
    val sheetProductName    : String       = "",
    val sheetProductBrand   : String       = "",
    val sheetProductImei    : String       = "",
    val sheetProductCategory: ProductCategory = ProductCategory.OTHER,
    val sheetProductQty     : String       = "1",
    val sheetProductCost    : String       = "",
    val sheetFormError      : String?      = null,

    // Discount
    val discountAmount  : String           = "0",
    val notes           : String           = "",

    // Payment
    val showPaymentSheet: Boolean          = false,
    val cashAmount      : String           = "",
    val cashAccountId   : String           = "default_cash",
    val bankAmount      : String           = "",
    val bankAccountId   : String?          = null,
    val isProcessing    : Boolean          = false,

    // Receipt
    val receipt         : PurchaseReceiptData? = null,

    // History
    val purchaseOrders  : List<PurchaseOrder>  = emptyList(),
    val selectedOrderId : String?          = null,

    // Due payment from history
    val showDuePayDialog: PurchaseOrder?   = null,
    val duePayAmount    : String           = "",
    val duePayAccountType: AccountType     = AccountType.CASH,
    val duePayAccountId : String           = "default_cash",
    val isDuePaying     : Boolean          = false,
) {
    val subtotal    : Double get() = cart.sumOf { it.totalCost }
    val discount    : Double get() = discountAmount.toDoubleOrNull() ?: 0.0
    val netTotal    : Double get() = (subtotal - discount).coerceAtLeast(0.0)
    val cashPaid    : Double get() = cashAmount.toDoubleOrNull() ?: 0.0
    val bankPaid    : Double get() = bankAmount.toDoubleOrNull() ?: 0.0
    val totalPaid   : Double get() = cashPaid + bankPaid
    val dueAfterPay : Double get() = (netTotal - totalPaid).coerceAtLeast(0.0)
    val itemCount   : Int    get() = cart.sumOf { it.quantity }
    val cartIsEmpty : Boolean get() = cart.isEmpty()

    val selectedCashAccount: CashAccount? get() = cashAccounts.find { it.id == cashAccountId }
    val selectedBankAccount: BankAccount? get() = bankAccounts.find { it.id == bankAccountId }

    // Filtered lists for source search
    val filteredSuppliers: List<Supplier>
        get() = if (sourceQuery.isBlank()) suppliers
        else suppliers.filter { it.name.contains(sourceQuery, true) || it.phone.contains(sourceQuery) }

    val filteredCustomers: List<Customer>
        get() = if (sourceQuery.isBlank()) customers
        else customers.filter { it.name.contains(sourceQuery, true) || it.phone.contains(sourceQuery) }

    val isNewSource: Boolean
        get() = selectedSource?.isNew == true || (sourceQuery.isNotBlank() && selectedSource == null)
}

// ── Intent ────────────────────────────────────────────────────────────────────

sealed class PurchaseIntent {
    data class  SwitchTab(val tab: PurchaseTab)                       : PurchaseIntent()
    data object Load                                                   : PurchaseIntent()
    data object Refresh                                                : PurchaseIntent()

    // Source
    data class  SetSourceType(val type: PurchaseSourceType)           : PurchaseIntent()
    data class  SetSourceQuery(val q: String)                         : PurchaseIntent()
    data class  SelectSupplier(val s: Supplier)                       : PurchaseIntent()
    data class  SelectCustomer(val c: Customer)                       : PurchaseIntent()
    data object ClearSource                                           : PurchaseIntent()
    data class  SetNewSourcePhone(val v: String)                      : PurchaseIntent()
    data class  SetNewSourceAddress(val v: String)                    : PurchaseIntent()
    data class  SetNewSourceCity(val v: String)                       : PurchaseIntent()
    data class  SetNewSourceEmail(val v: String)                      : PurchaseIntent()
    data class  SetSupplierInvoiceRef(val v: String)                  : PurchaseIntent()

    // Product sheet
    data object ShowProductSheet                                       : PurchaseIntent()
    data object DismissProductSheet                                   : PurchaseIntent()
    data class  SetSheetSearch(val q: String)                         : PurchaseIntent()
    data class  SelectSheetProduct(val product: Product?)             : PurchaseIntent()  // null = clear/new
    data class  SetSheetProductName(val v: String)                    : PurchaseIntent()
    data class  SetSheetProductBrand(val v: String)                   : PurchaseIntent()
    data class  SetSheetProductImei(val v: String)                    : PurchaseIntent()
    data class  SetSheetProductCategory(val v: ProductCategory)       : PurchaseIntent()
    data class  SetSheetProductQty(val v: String)                     : PurchaseIntent()
    data class  SetSheetProductCost(val v: String)                    : PurchaseIntent()
    data object AddSheetItemToCart                                    : PurchaseIntent()

    // Cart
    data class  ChangeCartQty(val productId: String, val delta: Int)  : PurchaseIntent()
    data class  RemoveFromCart(val productId: String)                 : PurchaseIntent()
    data object ClearCart                                             : PurchaseIntent()

    // Discount / notes
    data class  SetDiscount(val v: String)                            : PurchaseIntent()
    data class  SetNotes(val v: String)                               : PurchaseIntent()

    // Payment
    data object ShowPaymentSheet                                      : PurchaseIntent()
    data object DismissPaymentSheet                                   : PurchaseIntent()
    data class  SetCashAmount(val v: String)                          : PurchaseIntent()
    data class  SetCashAccountId(val v: String)                       : PurchaseIntent()
    data class  SetBankAmount(val v: String)                          : PurchaseIntent()
    data class  SetBankAccountId(val v: String?)                      : PurchaseIntent()
    data object ConfirmPurchase                                       : PurchaseIntent()

    // Receipt / errors
    data object DismissReceipt                                        : PurchaseIntent()
    data object ClearError                                            : PurchaseIntent()

    // History
    data class  SelectOrder(val id: String)                           : PurchaseIntent()
    data object DismissOrderDetail                                    : PurchaseIntent()

    // Due payment
    data class  ShowDuePayDialog(val order: PurchaseOrder)            : PurchaseIntent()
    data object DismissDuePayDialog                                   : PurchaseIntent()
    data class  SetDuePayAmount(val v: String)                        : PurchaseIntent()
    data class  SetDuePayAccountType(val v: AccountType)              : PurchaseIntent()
    data class  SetDuePayAccountId(val v: String)                     : PurchaseIntent()
    data object ExecuteDuePayment                                     : PurchaseIntent()
}

sealed class PurchaseEffect {
    data class ShowToast(val message: String) : PurchaseEffect()
}