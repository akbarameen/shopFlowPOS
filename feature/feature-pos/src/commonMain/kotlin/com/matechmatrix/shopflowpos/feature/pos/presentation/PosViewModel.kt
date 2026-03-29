package com.matechmatrix.shopflowpos.feature.pos.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosRepository

class PosViewModel(
    private val repo: PosRepository
) : MviViewModel<PosState, PosIntent, PosEffect>(PosState()) {

    init { onIntent(PosIntent.Load) }

    override suspend fun handleIntent(intent: PosIntent) {
        when (intent) {
            PosIntent.Load -> loadData()

            is PosIntent.Search -> {
                setState { copy(searchQuery = intent.query) }
                applyFilters()
            }
            PosIntent.ClearError -> setState { copy(error = null) }
            is PosIntent.FilterCategory -> {
                setState { copy(selectedCategory = intent.category) }
                applyFilters()
            }

            is PosIntent.AddToCart -> addToCart(intent.product)

            is PosIntent.RemoveFromCart -> setState {
                copy(cart = cart.filter { it.product.id != intent.productId })
            }

            is PosIntent.ChangeQty -> {
                val s = state.value
                val item = s.cart.find { it.product.id == intent.productId } ?: return
                val newQty = item.quantity + intent.delta
                
                if (newQty <= 0) {
                    onIntent(PosIntent.RemoveFromCart(intent.productId))
                } else {
                    val finalQty = newQty.coerceAtMost(item.product.stock)
                    if (finalQty < newQty) {
                        setEffect(PosEffect.ShowToast("Only ${item.product.stock} in stock"))
                    }
                    setState {
                        copy(cart = cart.map {
                            if (it.product.id == intent.productId) it.copy(quantity = finalQty) else it
                        })
                    }
                }
            }

            is PosIntent.SetDiscount -> setState { copy(discountAmount = intent.amount) }

            is PosIntent.SelectCustomer -> setState { 
                copy(selectedCustomer = intent.customer, customerName = intent.customer?.name ?: "") 
            }
            
            is PosIntent.SetCustomerName -> setState { copy(customerName = intent.name) }

            is PosIntent.SelectPaymentMethod -> {
                val s = state.value
                setState {
                    copy(
                        paymentMethod = intent.method,
                        cashPaid      = if (intent.method == PaymentMethod.CASH) netTotal.toString() else "",
                        bankPaid      = if (intent.method == PaymentMethod.BANK_TRANSFER) netTotal.toString() else ""
                    )
                }
            }

            is PosIntent.SelectBankAccount -> setState { copy(selectedBankAccount = intent.account) }
            is PosIntent.SetCashPaid       -> setState { copy(cashPaid = intent.amount) }
            is PosIntent.SetBankPaid       -> setState { copy(bankPaid = intent.amount) }
            is PosIntent.SetDueDate        -> setState { copy(dueDate = intent.date) }
            is PosIntent.SetNotes          -> setState { copy(notes = intent.text) }

            PosIntent.OpenCheckout -> {
                if (state.value.cart.isEmpty()) {
                    setEffect(PosEffect.ShowToast("Cart is empty"))
                    return
                }
                setState {
                    copy(
                        showCheckoutSheet = true,
                        cashPaid = if (paymentMethod == PaymentMethod.CASH) netTotal.toString() else "",
                        bankPaid = if (paymentMethod == PaymentMethod.BANK_TRANSFER) netTotal.toString() else ""
                    )
                }
            }

            PosIntent.DismissCheckout -> setState { copy(showCheckoutSheet = false) }

            PosIntent.CompleteSale -> completeSale()

            PosIntent.DismissSuccess -> {
                setState { copy(showSuccessDialog = false, lastSale = null) }
                onIntent(PosIntent.ClearCart)
            }

            PosIntent.ClearCart -> setState {
                copy(
                    cart = emptyList(), discountAmount = "0",
                    selectedCustomer = null, customerName = "", notes = "", 
                    cashPaid = "", bankPaid = "", dueDate = null,
                    paymentMethod = PaymentMethod.CASH
                )
            }
        }
    }

    private suspend fun loadData() {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        when (val r = repo.getAllProducts()) {
            is AppResult.Success -> {
                val customers   = (repo.getAllCustomers() as? AppResult.Success)?.data ?: emptyList()
                val bankAccts   = (repo.getBankAccounts() as? AppResult.Success)?.data ?: emptyList()
                setState {
                    copy(
                        isLoading = false, products = r.data, filteredProducts = r.data,
                        customers = customers, bankAccounts = bankAccts,
                        selectedBankAccount = bankAccts.firstOrNull { it.id == state.value.selectedBankAccount?.id } ?: bankAccts.firstOrNull(),
                        currencySymbol = currency
                    )
                }
            }
            is AppResult.Error -> setState { copy(isLoading = false, error = r.message) }
            else -> setState { copy(isLoading = false) }
        }
    }

    private fun applyFilters() {
        val s = state.value
        val filtered = s.products
            .filter {
                if (s.searchQuery.isBlank()) true
                else it.name.contains(s.searchQuery, ignoreCase = true) || it.barcode?.contains(s.searchQuery) == true
            }
            .filter { if (s.selectedCategory == null) true else it.category == s.selectedCategory }
        setState { copy(filteredProducts = filtered) }
    }

    private fun addToCart(product: Product) {
        val s = state.value
        val existing = s.cart.find { it.product.id == product.id }
        if (existing != null) {
            if (existing.quantity >= product.stock) {
                setEffect(PosEffect.ShowToast("Only ${product.stock} in stock"))
                return
            }
            setState { copy(cart = cart.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }) }
        } else {
            if (product.stock == 0) {
                setEffect(PosEffect.ShowToast("Out of stock"))
                return
            }
            setState { copy(cart = cart + CartItem(product, 1)) }
        }
    }

    private suspend fun completeSale() {
        val s = state.value
        setState { copy(isProcessing = true) }

        val cashAmt = s.cashPaid.toDoubleOrNull()?.toLong() ?: 0L
        val bankAmt = s.bankPaid.toDoubleOrNull()?.toLong() ?: 0L
        val totalPaid = cashAmt + bankAmt

        // Map our local CartItem to core CartItem
        val coreCartItems = s.cart.map {
            com.matechmatrix.shopflowpos.core.model.CartItem(product = it.product, quantity = it.quantity)
        }

        val result = repo.completeSale(
            cartItems       = coreCartItems,
            discount        = s.discount,
            paymentMethod   = s.paymentMethod,
            cashAmount      = cashAmt,
            bankAmount      = bankAmt,
            bankAccountId   = s.selectedBankAccount?.id,
            customerId      = s.selectedCustomer?.id,
            customerName    = s.customerName.takeIf { it.isNotBlank() },
            notes           = s.notes,
            dueDate         = s.dueDate
        )

        when (result) {
            is AppResult.Success -> setState {
                copy(isProcessing = false, showCheckoutSheet = false, showSuccessDialog = true, lastSale = result.data)
            }
            is AppResult.Error   -> setState { copy(isProcessing = false, error = result.message) }
            else -> setState { copy(isProcessing = false) }
        }
    }
}
