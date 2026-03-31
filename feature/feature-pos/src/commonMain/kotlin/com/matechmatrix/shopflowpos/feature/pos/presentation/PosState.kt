package com.matechmatrix.shopflowpos.feature.pos.presentation

import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

// ─── State ───────────────────────────────────────────────────────────────────

data class PosState(
    // Products
    val isLoading         : Boolean          = true,
    val pagedProducts     : Flow<PagingData<Product>> = emptyFlow(),
    val searchQuery       : String           = "",
    val selectedCategory  : ProductCategory? = null,

    // Cart
    val cart              : List<CartItem>   = emptyList(),
    val discountAmount    : String           = "0",

    // Customer
    val customers         : List<Customer>   = emptyList(),
    val selectedCustomer  : Customer?        = null,

    // Payment
    val cashAccounts      : List<CashAccount>  = emptyList(),
    val bankAccounts      : List<BankAccount>  = emptyList(),
    val selectedCashAccountId : String       = "default_cash",
    val selectedBankAccount : BankAccount?   = null,
    val paymentMethod     : PaymentMethod    = PaymentMethod.CASH,
    val cashPaid          : String           = "",
    val bankPaid          : String           = "",
    val dueDate           : Long?            = null,
    val notes             : String           = "",

    // Shop / Settings
    val shopName          : String           = "",
    val currencySymbol    : String           = "Rs.",
    val taxRate           : Double           = 0.0,

    // Sheets & Dialogs
    val showCheckoutSheet : Boolean          = false,
    val receiptData       : ReceiptData?     = null,   // non-null = show receipt dialog
    val error             : String?          = null,
    val isProcessing      : Boolean          = false,
) {
    // Derived values
    val subtotal   : Double get() = cart.sumOf { it.lineTotal }
    val discount   : Double get() = discountAmount.toDoubleOrNull() ?: 0.0
    val afterDiscount: Double get() = (subtotal - discount).coerceAtLeast(0.0)
    val taxAmount  : Double get() = afterDiscount * (taxRate / 100.0)
    val netTotal   : Double get() = afterDiscount + taxAmount
    val itemCount  : Int    get() = cart.sumOf { it.quantity }
    val cartIsEmpty: Boolean get() = cart.isEmpty()

    val cashChange : Double get() {
        val paid = (cashPaid.toDoubleOrNull() ?: 0.0) + (bankPaid.toDoubleOrNull() ?: 0.0)
        return (paid - netTotal).coerceAtLeast(0.0)
    }

    val dueAfterPayment: Double get() {
        val paid = (cashPaid.toDoubleOrNull() ?: 0.0) + (bankPaid.toDoubleOrNull() ?: 0.0)
        return (netTotal - paid).coerceAtLeast(0.0)
    }

    val selectedCashAccount: CashAccount?
        get() = cashAccounts.find { it.id == selectedCashAccountId }

    val showSuccessDialog : Boolean get() = receiptData != null
}

// ─── Intent ──────────────────────────────────────────────────────────────────

sealed class PosIntent {
    data object Load                                            : PosIntent()
    data class  Search(val query: String)                       : PosIntent()
    data class  FilterCategory(val category: ProductCategory?)  : PosIntent()

    // Cart
    data class  AddToCart(val product: Product)                 : PosIntent()
    data class  RemoveFromCart(val productId: String)           : PosIntent()
    data class  ChangeQty(val productId: String, val delta: Int): PosIntent()
    data class  SetItemDiscount(val productId: String, val discount: Double) : PosIntent()
    data object ClearCart                                       : PosIntent()

    // Checkout
    data object OpenCheckout                                    : PosIntent()
    data object DismissCheckout                                 : PosIntent()
    data class  SetDiscount(val amount: String)                 : PosIntent()
    data class  SelectCustomer(val customer: Customer?)         : PosIntent()
    data class  SelectPaymentMethod(val method: PaymentMethod)  : PosIntent()
    data class  SelectCashAccount(val id: String)               : PosIntent()
    data class  SelectBankAccount(val account: BankAccount?)    : PosIntent()
    data class  SetCashPaid(val amount: String)                 : PosIntent()
    data class  SetBankPaid(val amount: String)                 : PosIntent()
    data class  SetDueDate(val date: Long?)                     : PosIntent()
    data class  SetNotes(val text: String)                      : PosIntent()
    data object CompleteSale                                    : PosIntent()

    // Post-sale
    data object DismissReceipt                                  : PosIntent()
    data object ClearError                                      : PosIntent()
}

// ─── Effect ───────────────────────────────────────────────────────────────────

sealed class PosEffect {
    data class ShowToast(val message: String) : PosEffect()
}