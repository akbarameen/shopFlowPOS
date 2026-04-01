package com.matechmatrix.shopflowpos.feature.purchase.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSource
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSourceType
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseCartItem
import com.matechmatrix.shopflowpos.feature.purchase.domain.usecase.CreatePurchaseOrderUseCase
import com.matechmatrix.shopflowpos.feature.purchase.domain.usecase.GetPurchaseSettingsUseCase
import com.matechmatrix.shopflowpos.feature.purchase.domain.usecase.PayPurchaseDueUseCase
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.CreatePurchaseRequest
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseRepository
import kotlinx.datetime.Clock

class PurchaseViewModel(
    private val getSettings  : GetPurchaseSettingsUseCase,
    private val createOrderUC: CreatePurchaseOrderUseCase,
    private val payDueUC     : PayPurchaseDueUseCase,
    private val repo         : PurchaseRepository
) : MviViewModel<PurchaseState, PurchaseIntent, PurchaseEffect>(PurchaseState()) {

    init { onIntent(PurchaseIntent.Load) }

    override suspend fun handleIntent(intent: PurchaseIntent) {
        when (intent) {
            is PurchaseIntent.SwitchTab -> {
                setState { copy(activeTab = intent.tab) }
                if (intent.tab == PurchaseTab.HISTORY) loadHistory()
            }
            PurchaseIntent.Load, PurchaseIntent.Refresh -> loadAll()

            // ── Source ────────────────────────────────────────────────────────
            is PurchaseIntent.SetSourceType -> setState {
                copy(sourceType = intent.type, sourceQuery = "", selectedSource = null, newSourcePhone = "", newSourceAddress = "", newSourceCity = "", newSourceEmail = "")
            }

            is PurchaseIntent.SetSourceQuery -> setState { copy(sourceQuery = intent.q, selectedSource = null) }

            is PurchaseIntent.SelectSupplier -> setState {
                copy(
                    selectedSource = PurchaseSource(PurchaseSourceType.SUPPLIER, intent.s.id, intent.s.name, intent.s.phone, intent.s.address, intent.s.city, intent.s.email, isNew = false),
                    sourceQuery    = intent.s.name
                )
            }

            is PurchaseIntent.SelectCustomer -> setState {
                copy(
                    selectedSource = PurchaseSource(PurchaseSourceType.CUSTOMER, intent.c.id, intent.c.name, intent.c.phone, intent.c.address, intent.c.city, intent.c.email ?: "", isNew = false),
                    sourceQuery    = intent.c.name
                )
            }

            PurchaseIntent.ClearSource -> setState { copy(selectedSource = null, sourceQuery = "", newSourcePhone = "", newSourceAddress = "", newSourceCity = "", newSourceEmail = "") }

            is PurchaseIntent.SetNewSourcePhone   -> setState { copy(newSourcePhone = intent.v) }
            is PurchaseIntent.SetNewSourceAddress -> setState { copy(newSourceAddress = intent.v) }
            is PurchaseIntent.SetNewSourceCity    -> setState { copy(newSourceCity = intent.v) }
            is PurchaseIntent.SetNewSourceEmail   -> setState { copy(newSourceEmail = intent.v) }
            is PurchaseIntent.SetSupplierInvoiceRef -> setState { copy(supplierInvoiceRef = intent.v) }

            // ── Product Sheet ─────────────────────────────────────────────────
            PurchaseIntent.ShowProductSheet -> setState {
                copy(showProductSheet = true, sheetProductSearch = "", sheetSearchResults = emptyList(),
                    sheetSelectedProduct = null, sheetProductName = "", sheetProductBrand = "",
                    sheetProductImei = "", sheetProductCategory = com.matechmatrix.shopflowpos.core.model.enums.ProductCategory.OTHER,
                    sheetProductQty = "1", sheetProductCost = "", sheetFormError = null)
            }

            PurchaseIntent.DismissProductSheet -> setState { copy(showProductSheet = false) }

            is PurchaseIntent.SetSheetSearch -> {
                setState { copy(sheetProductSearch = intent.q) }
                // Search products
                val results = (repo.searchProducts(intent.q) as? AppResult.Success)?.data ?: emptyList()
                setState { copy(sheetSearchResults = results) }
            }

            is PurchaseIntent.SelectSheetProduct -> setState {
                if (intent.product == null) {
                    // User wants to enter a brand new product manually
                    copy(sheetSelectedProduct = null, sheetProductName = sheetProductSearch,
                        sheetProductCost = "", sheetProductQty = "1")
                } else {
                    copy(sheetSelectedProduct = intent.product,
                        sheetProductName = intent.product.name,
                        sheetProductBrand = intent.product.brand,
                        sheetProductCost = intent.product.costPrice.toString(),
                        sheetProductQty = "1",
                        sheetProductImei = intent.product.imei ?: "",
                        sheetProductCategory = intent.product.category)
                }
            }

            is PurchaseIntent.SetSheetProductName     -> setState { copy(sheetProductName = intent.v) }
            is PurchaseIntent.SetSheetProductBrand    -> setState { copy(sheetProductBrand = intent.v) }
            is PurchaseIntent.SetSheetProductImei     -> setState { copy(sheetProductImei = intent.v) }
            is PurchaseIntent.SetSheetProductCategory -> setState { copy(sheetProductCategory = intent.v) }
            is PurchaseIntent.SetSheetProductQty      -> setState { copy(sheetProductQty = intent.v) }
            is PurchaseIntent.SetSheetProductCost     -> setState { copy(sheetProductCost = intent.v) }

            PurchaseIntent.AddSheetItemToCart -> {
                val s    = state.value
                val name = s.sheetProductName.trim()
                val qty  = s.sheetProductQty.toIntOrNull() ?: 0
                val cost = s.sheetProductCost.toDoubleOrNull()

                if (name.isBlank())   { setState { copy(sheetFormError = "Product name is required") }; return }
                if (qty <= 0)         { setState { copy(sheetFormError = "Quantity must be at least 1") }; return }
                if (cost == null || cost < 0) { setState { copy(sheetFormError = "Enter a valid cost price") }; return }

                val selectedProd = s.sheetSelectedProduct
                val cartItem = PurchaseCartItem(
                    productId    = selectedProd?.id ?: "",
                    productName  = name,
                    imei         = s.sheetProductImei.takeIf { it.isNotBlank() },
                    category     = s.sheetProductCategory.name,
                    brand        = s.sheetProductBrand.trim(),
                    quantity     = qty,
                    unitCost     = cost,
                    isNewProduct = selectedProd == null
                )

                // If product already in cart with same id, increase qty
                val existing = if (selectedProd != null) s.cart.indexOfFirst { it.productId == selectedProd.id } else -1
                val newCart = if (existing >= 0) {
                    s.cart.toMutableList().also { it[existing] = it[existing].copy(quantity = it[existing].quantity + qty, unitCost = cost) }
                } else {
                    s.cart + cartItem
                }

                setState { copy(cart = newCart, showProductSheet = false) }
                setEffect(PurchaseEffect.ShowToast("$name added to cart"))
            }

            // ── Cart ──────────────────────────────────────────────────────────
            is PurchaseIntent.ChangeCartQty -> {
                val item = state.value.cart.find { it.productId == intent.productId } ?: return
                val newQty = item.quantity + intent.delta
                if (newQty <= 0) onIntent(PurchaseIntent.RemoveFromCart(intent.productId))
                else setState { copy(cart = cart.map { if (it.productId == intent.productId) it.copy(quantity = newQty) else it }) }
            }

            is PurchaseIntent.RemoveFromCart ->
                setState { copy(cart = cart.filter { it.productId != intent.productId }) }

            PurchaseIntent.ClearCart -> resetOrder()

            // ── Discount / notes ──────────────────────────────────────────────
            is PurchaseIntent.SetDiscount -> setState { copy(discountAmount = intent.v) }
            is PurchaseIntent.SetNotes    -> setState { copy(notes = intent.v) }

            // ── Payment sheet ─────────────────────────────────────────────────
            PurchaseIntent.ShowPaymentSheet -> {
                if (state.value.cartIsEmpty) { setEffect(PurchaseEffect.ShowToast("Add items first")); return }
                if (state.value.sourceQuery.isBlank()) { setEffect(PurchaseEffect.ShowToast("Select a supplier or customer first")); return }
                val s = state.value
                setState {
                    copy(
                        showPaymentSheet = true,
                        cashAmount  = s.netTotal.toLong().toString(),
                        bankAmount  = "",
                        cashAccountId = cashAccounts.firstOrNull()?.id ?: "default_cash",
                        bankAccountId = bankAccounts.firstOrNull()?.id
                    )
                }
            }

            PurchaseIntent.DismissPaymentSheet -> setState { copy(showPaymentSheet = false) }
            is PurchaseIntent.SetCashAmount    -> setState { copy(cashAmount = intent.v) }
            is PurchaseIntent.SetCashAccountId -> setState { copy(cashAccountId = intent.v) }
            is PurchaseIntent.SetBankAmount    -> setState { copy(bankAmount = intent.v) }
            is PurchaseIntent.SetBankAccountId -> setState { copy(bankAccountId = intent.v) }

            PurchaseIntent.ConfirmPurchase -> doCreatePurchase()
            PurchaseIntent.DismissReceipt  -> { setState { copy(receipt = null) }; resetOrder() }
            PurchaseIntent.ClearError      -> setState { copy(error = null) }

            // ── History ───────────────────────────────────────────────────────
            is PurchaseIntent.SelectOrder      -> setState { copy(selectedOrderId = intent.id) }
            PurchaseIntent.DismissOrderDetail  -> setState { copy(selectedOrderId = null) }

            // ── Due payment ───────────────────────────────────────────────────
            is PurchaseIntent.ShowDuePayDialog -> setState {
                copy(showDuePayDialog = intent.order, duePayAmount = intent.order.dueAmount.toLong().toString(),
                    duePayAccountType = AccountType.CASH, duePayAccountId = cashAccounts.firstOrNull()?.id ?: "default_cash")
            }
            PurchaseIntent.DismissDuePayDialog -> setState { copy(showDuePayDialog = null) }
            is PurchaseIntent.SetDuePayAmount  -> setState { copy(duePayAmount = intent.v) }
            is PurchaseIntent.SetDuePayAccountType -> setState {
                val id = when (intent.v) {
                    AccountType.CASH -> cashAccounts.firstOrNull()?.id ?: "default_cash"
                    AccountType.BANK -> bankAccounts.firstOrNull()?.id ?: ""
                    else             -> ""
                }
                copy(duePayAccountType = intent.v, duePayAccountId = id)
            }
            is PurchaseIntent.SetDuePayAccountId -> setState { copy(duePayAccountId = intent.v) }
            PurchaseIntent.ExecuteDuePayment     -> doPayDue()
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private suspend fun loadAll() {
        setState { copy(isLoading = true) }
        val settings  = getSettings()
        val suppliers = (repo.getAllSuppliers() as? AppResult.Success)?.data ?: emptyList()
        val customers = (repo.getAllCustomers() as? AppResult.Success)?.data ?: emptyList()
        setState {
            copy(
                isLoading    = false,
                suppliers    = suppliers,
                customers    = customers,
                cashAccounts = settings.cashAccounts,
                bankAccounts = settings.bankAccounts,
                currencySymbol = settings.currencySymbol,
                cashAccountId  = settings.cashAccounts.firstOrNull()?.id ?: "default_cash",
                bankAccountId  = settings.bankAccounts.firstOrNull()?.id
            )
        }
    }

    private suspend fun loadHistory() {
        val orders = (repo.getAllPurchaseOrders() as? AppResult.Success)?.data ?: emptyList()
        setState { copy(purchaseOrders = orders.sortedByDescending { it.purchasedAt }) }
    }

    private fun resetOrder() {
        setState {
            copy(
                cart = emptyList(), selectedSource = null, sourceQuery = "",
                newSourcePhone = "", newSourceAddress = "", newSourceCity = "", newSourceEmail = "",
                supplierInvoiceRef = "", discountAmount = "0", notes = "",
                cashAmount = "", bankAmount = "", showPaymentSheet = false
            )
        }
    }

    private suspend fun doCreatePurchase() {
        val s = state.value
        if (s.sourceQuery.isBlank()) { setEffect(PurchaseEffect.ShowToast("Enter supplier/customer name")); return }

        // Balance check before DB call
        val cashAcc = s.selectedCashAccount
        val bankAcc = s.selectedBankAccount
        if (s.cashPaid > 0 && cashAcc != null && s.cashPaid > cashAcc.balance) {
            setEffect(PurchaseEffect.ShowToast("Insufficient cash! Available: ${s.currencySymbol} ${cashAcc.balance.toLong()}")); return
        }
        if (s.bankPaid > 0 && bankAcc != null && s.bankPaid > bankAcc.balance) {
            setEffect(PurchaseEffect.ShowToast("Insufficient bank balance! Available: ${s.currencySymbol} ${bankAcc.balance.toLong()}")); return
        }

        setState { copy(isProcessing = true) }

        // Build the PurchaseSource - if user selected from list, use that; otherwise mark as new
        val source = s.selectedSource ?: PurchaseSource(
            type    = s.sourceType,
            id      = null,
            name    = s.sourceQuery.trim(),
            phone   = s.newSourcePhone.trim(),
            address = s.newSourceAddress.trim(),
            city    = s.newSourceCity.trim(),
            email   = s.newSourceEmail.trim(),
            isNew   = true
        )

        val request = CreatePurchaseRequest(
            source             = source,
            supplierInvoiceRef = s.supplierInvoiceRef.trim(),
            items              = s.cart,
            discountAmount     = s.discount,
            cashAmount         = s.cashPaid,
            cashAccountId      = s.cashAccountId,
            bankAmount         = s.bankPaid,
            bankAccountId      = s.bankAccountId,
            notes              = s.notes.trim()
        )

        when (val r = createOrderUC(request)) {
            is AppResult.Success -> setState { copy(isProcessing = false, showPaymentSheet = false, receipt = r.data) }
            is AppResult.Error   -> setState { copy(isProcessing = false, error = r.message) }
            else                 -> setState { copy(isProcessing = false) }
        }
    }

    private suspend fun doPayDue() {
        val s     = state.value
        val order = s.showDuePayDialog ?: return
        val amount = s.duePayAmount.toDoubleOrNull()
        if (amount == null || amount <= 0) { setEffect(PurchaseEffect.ShowToast("Enter valid amount")); return }

        // Balance check
        when (s.duePayAccountType) {
            AccountType.CASH -> {
                val acc = s.cashAccounts.find { it.id == s.duePayAccountId }
                if (acc != null && amount > acc.balance) { setEffect(PurchaseEffect.ShowToast("Insufficient cash (${acc.balance.toLong()} available)")); return }
            }
            AccountType.BANK -> {
                val acc = s.bankAccounts.find { it.id == s.duePayAccountId }
                if (acc != null && amount > acc.balance) { setEffect(PurchaseEffect.ShowToast("Insufficient bank balance (${acc.balance.toLong()} available)")); return }
            }
            else -> {}
        }

        setState { copy(isDuePaying = true) }
        when (val r = payDueUC(order.id, amount, s.duePayAccountType, s.duePayAccountId)) {
            is AppResult.Success -> {
                setState { copy(isDuePaying = false, showDuePayDialog = null) }
                setEffect(PurchaseEffect.ShowToast("Paid ${s.currencySymbol} ${amount.toLong()} for ${order.poNumber}"))
                loadHistory()
            }
            is AppResult.Error -> {
                setState { copy(isDuePaying = false) }
                setEffect(PurchaseEffect.ShowToast(r.message))
            }
            else -> setState { copy(isDuePaying = false) }
        }
    }
}
