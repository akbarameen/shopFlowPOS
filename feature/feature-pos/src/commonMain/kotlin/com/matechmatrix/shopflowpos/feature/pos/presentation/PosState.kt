package com.matechmatrix.shopflowpos.feature.pos.presentation

import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory

data class PosState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: ProductCategory? = null,
    val cart: List<CartItem> = emptyList(),
    val discountAmount: String = "0",
    val customers: List<Customer> = emptyList(),
    val selectedCustomer: Customer? = null,
    val customerName: String = "",
    val bankAccounts: List<BankAccount> = emptyList(),
    val selectedBankAccount: BankAccount? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val cashPaid: String = "",
    val bankPaid: String = "",
    val dueDate: Long? = null,
    val notes: String = "",
    val showCheckoutSheet: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val lastSale: Sale? = null,
    val currencySymbol: String = "Rs.",
    val error: String? = null,
    val isProcessing: Boolean = false
) {
    val cartTotal: Double get() = cart.sumOf { it.lineTotal }
    val discount: Long    get() = discountAmount.toLongOrNull() ?: 0L
    val netTotal: Double get() = (cartTotal - discount).coerceAtLeast(0.0)
    val itemCount: Int    get() = cart.sumOf { it.quantity }
    val cashChange: Double get() = ((cashPaid.toDoubleOrNull() ?: 0.0) + (bankPaid.toDoubleOrNull() ?: 0.0)) - netTotal
}

sealed class PosIntent {
    data object Load : PosIntent()
    data class Search(val query: String) : PosIntent()
    data class FilterCategory(val category: ProductCategory?) : PosIntent()
    data class AddToCart(val product: Product) : PosIntent()
    data class RemoveFromCart(val productId: String) : PosIntent()
    data class ChangeQty(val productId: String, val delta: Int) : PosIntent()
    data class SetDiscount(val amount: String) : PosIntent()
    data class SelectCustomer(val customer: Customer?) : PosIntent()
    data class SetCustomerName(val name: String) : PosIntent()
    data class SelectPaymentMethod(val method: PaymentMethod) : PosIntent()
    data class SelectBankAccount(val account: BankAccount?) : PosIntent()
    data class SetCashPaid(val amount: String) : PosIntent()
    data class SetBankPaid(val amount: String) : PosIntent()
    data class SetDueDate(val date: Long?) : PosIntent()
    data class SetNotes(val text: String) : PosIntent()
    data object OpenCheckout : PosIntent()
    data object DismissCheckout : PosIntent()
    data object CompleteSale : PosIntent()
    data object DismissSuccess : PosIntent()
    data object ClearCart : PosIntent()
    data object ClearError : PosIntent()
}

sealed class PosEffect {
    data class ShowToast(val message: String) : PosEffect()
}
