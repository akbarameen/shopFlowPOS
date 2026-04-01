package com.matechmatrix.shopflowpos.feature.pos.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.CartItem
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.CompleteSaleUseCase
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.GetPosCustomersUseCase
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.GetPosProductsPagedUseCase
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.GetPosSettingsUseCase

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class PosViewModel(
    private val getProductsPaged : GetPosProductsPagedUseCase,
    private val getCustomers     : GetPosCustomersUseCase,
    private val getSettings      : GetPosSettingsUseCase,
    private val completeSaleUC   : CompleteSaleUseCase,
) : MviViewModel<PosState, PosIntent, PosEffect>(PosState()) {

    private val searchFlow     = MutableStateFlow("")
    private val categoryFlow   = MutableStateFlow<ProductCategory?>(null)
    private val refreshTrigger = MutableStateFlow(0)

    init {
        // Wire paginated product flow — reacts to search + category + refresh trigger
        combine(
            searchFlow.debounce(300).distinctUntilChanged(),
            categoryFlow,
            refreshTrigger
        ) { q, cat, _ -> q to cat }
            .flatMapLatest { (q, cat) ->
                getProductsPaged(q, cat).cachedIn(viewModelScope)
            }
            .onEach { paged -> setState { copy(pagedProducts = flowOf(paged)) } }
            .launchIn(viewModelScope)

        onIntent(PosIntent.Load)
    }

    override suspend fun handleIntent(intent: PosIntent) {
        when (intent) {

            PosIntent.Load -> loadData()
            
//            PosIntent.Refresh -> refreshTrigger.value++

            // ── Search / Filter ───────────────────────────────────────────────
            is PosIntent.Search -> {
                searchFlow.value = intent.query
                setState { copy(searchQuery = intent.query) }
            }

            is PosIntent.FilterCategory -> {
                categoryFlow.value = intent.category
                setState { copy(selectedCategory = intent.category) }
            }

            // ── Cart ──────────────────────────────────────────────────────────
            is PosIntent.AddToCart -> addToCart(intent.product)

            is PosIntent.RemoveFromCart -> setState {
                copy(cart = cart.filter { it.product.id != intent.productId })
            }

            is PosIntent.ChangeQty -> {
                val item = state.value.cart.find { it.product.id == intent.productId } ?: return
                val newQty = item.quantity + intent.delta
                when {
                    newQty <= 0 -> onIntent(PosIntent.RemoveFromCart(intent.productId))
                    else -> {
                        val capped = if (item.product.isImeiTracked) 1
                        else newQty.coerceAtMost(item.product.stock)
                        if (!item.product.isImeiTracked && capped < newQty)
                            setEffect(PosEffect.ShowToast("Only ${item.product.stock} available"))
                        setState {
                            copy(cart = cart.map {
                                if (it.product.id == intent.productId) it.copy(quantity = capped) else it
                            })
                        }
                    }
                }
            }

            is PosIntent.SetItemDiscount -> setState {
                copy(cart = cart.map {
                    if (it.product.id == intent.productId) it.copy(discount = intent.discount.coerceAtLeast(0.0))
                    else it
                })
            }

            PosIntent.ClearCart -> resetCart()

            // ── Checkout sheet ────────────────────────────────────────────────
            PosIntent.OpenCheckout -> {
                if (state.value.cartIsEmpty) {
                    setEffect(PosEffect.ShowToast("Add items to cart first"))
                    return
                }
                val s = state.value
                setState {
                    copy(
                        showCheckoutSheet = true,
                        cashPaid = if (s.paymentMethod == PaymentMethod.CASH || s.paymentMethod == PaymentMethod.SPLIT)
                            s.netTotal.toLong().toString() else "",
                        bankPaid = if (s.paymentMethod == PaymentMethod.BANK)
                            s.netTotal.toLong().toString() else ""
                    )
                }
            }

            PosIntent.DismissCheckout -> setState { copy(showCheckoutSheet = false) }

            is PosIntent.SetDiscount -> setState { copy(discountAmount = intent.amount) }

            is PosIntent.SelectCustomer -> setState {
                copy(selectedCustomer = intent.customer)
            }

            is PosIntent.SelectPaymentMethod -> {
                val s = state.value
                setState {
                    copy(
                        paymentMethod = intent.method,
                        cashPaid = when (intent.method) {
                            PaymentMethod.CASH  -> s.netTotal.toLong().toString()
                            PaymentMethod.SPLIT -> ""
                            else               -> ""
                        },
                        bankPaid = when (intent.method) {
                            PaymentMethod.BANK  -> s.netTotal.toLong().toString()
                            PaymentMethod.SPLIT -> ""
                            else               -> ""
                        }
                    )
                }
            }

            is PosIntent.SelectCashAccount  -> setState { copy(selectedCashAccountId = intent.id) }
            is PosIntent.SelectBankAccount  -> setState { copy(selectedBankAccount = intent.account) }
            is PosIntent.SetCashPaid        -> setState { copy(cashPaid = intent.amount) }
            is PosIntent.SetBankPaid        -> setState { copy(bankPaid = intent.amount) }
            is PosIntent.SetDueDate         -> setState { copy(dueDate = intent.date) }
            is PosIntent.SetNotes           -> setState { copy(notes = intent.text) }

            PosIntent.CompleteSale -> doCompleteSale()

            // ── Post-sale ─────────────────────────────────────────────────────
            PosIntent.DismissReceipt -> {
                setState { copy(receiptData = null) }
                resetCart()
                refreshTrigger.value++ // REFRESH PRODUCTS (Stock changed)
            }

            PosIntent.ClearError -> setState { copy(error = null) }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private suspend fun loadData() {
        setState { copy(isLoading = true) }
        val settings  = getSettings()
        val customers = (getCustomers() as? AppResult.Success)?.data ?: emptyList()
        setState {
            copy(
                isLoading          = false,
                customers          = customers,
                cashAccounts       = settings.cashAccounts,
                bankAccounts       = settings.bankAccounts,
                selectedCashAccountId = settings.cashAccounts.firstOrNull()?.id ?: "default_cash",
                selectedBankAccount   = settings.bankAccounts.firstOrNull(),
                shopName           = settings.shopName,
                currencySymbol     = settings.currencySymbol,
                taxRate            = settings.taxRate
            )
        }
    }

    private fun addToCart(product: Product) {
        if (product.stock <= 0 && !product.isImeiTracked) {
            setEffect(PosEffect.ShowToast("${product.name} is out of stock"))
            return
        }
        val s        = state.value
        val existing = s.cart.find { it.product.id == product.id }
        if (existing != null) {
            if (product.isImeiTracked) {
                setEffect(PosEffect.ShowToast("IMEI item already in cart"))
                return
            }
            if (existing.quantity >= product.stock) {
                setEffect(PosEffect.ShowToast("Only ${product.stock} available"))
                return
            }
            setState { copy(cart = cart.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }) }
        } else {
            setState { copy(cart = cart + CartItem(product, 1)) }
        }
    }

    private fun resetCart() {
        setState {
            copy(
                cart = emptyList(), discountAmount = "0",
                selectedCustomer = null, notes = "",
                cashPaid = "", bankPaid = "", dueDate = null,
                paymentMethod = PaymentMethod.CASH
            )
        }
    }

    private suspend fun doCompleteSale() {
        val s = state.value
        setState { copy(isProcessing = true) }

        val result = completeSaleUC(
            cart           = s.cart,
            discountAmount = s.discount,
            paymentMethod  = s.paymentMethod,
            cashAmount     = s.cashPaid.toDoubleOrNull() ?: 0.0,
            cashAccountId  = s.selectedCashAccountId,
            bankAmount     = s.bankPaid.toDoubleOrNull() ?: 0.0,
            bankAccountId  = s.selectedBankAccount?.id,
            customer       = s.selectedCustomer,
            walkinName     = "",
            walkinPhone    = "",
            dueDate        = s.dueDate,
            notes          = s.notes,
            taxRate        = s.taxRate
        )

        when (result) {
            is AppResult.Success -> {
                setState {
                    copy(
                        isProcessing      = false,
                        showCheckoutSheet = false,
                        receiptData       = result.data
                    )
                }
            }
            is AppResult.Error -> setState {
                copy(isProcessing = false, error = result.message)
            }
            else -> setState { copy(isProcessing = false) }
        }
    }
}
