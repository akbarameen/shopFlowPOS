package com.matechmatrix.shopflowpos.feature.purchase.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSource

data class PurchaseCartItem(
    val productId   : String,    // blank = new product not yet in DB
    val productName : String,
    val imei        : String?   = null,
    val category    : String    = "OTHER",
    val brand       : String    = "",
    val quantity    : Int,
    val unitCost    : Double,
    val isNewProduct: Boolean   = false  // will be inserted to product table
) {
    val totalCost: Double get() = quantity * unitCost
}

data class CreatePurchaseRequest(
    val source            : PurchaseSource,
    val supplierInvoiceRef: String = "",
    val items             : List<PurchaseCartItem>,
    val discountAmount    : Double = 0.0,
    val cashAmount        : Double = 0.0,
    val cashAccountId     : String = "default_cash",
    val bankAmount        : Double = 0.0,
    val bankAccountId     : String? = null,
    val notes             : String = ""
)

interface PurchaseRepository {
    suspend fun getAllSuppliers(): AppResult<List<Supplier>>
    suspend fun getAllCustomers(): AppResult<List<Customer>>
    suspend fun searchProducts(query: String): AppResult<List<Product>>
    suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>>
    suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>>
    suspend fun getAllPurchaseOrders(): AppResult<List<PurchaseOrder>>

    /** Atomically creates the purchase order, updates stock, accounts, ledger, and contact dues. */
    suspend fun createPurchaseOrder(request: CreatePurchaseRequest): AppResult<PurchaseReceiptData>

    /** Pay an existing purchase-order due from an account. */
    suspend fun payPurchaseDue(orderId: String, amount: Double, accountType: com.matechmatrix.shopflowpos.core.model.enums.AccountType, accountId: String): AppResult<Unit>

    suspend fun getPurchaseSettings(): PurchaseSettings
}

data class PurchaseSettings(
    val shopName       : String,
    val shopAddress    : String = "",
    val shopPhone      : String = "",
    val currencySymbol : String = "Rs.",
    val cashAccounts   : List<CashAccount>,
    val bankAccounts   : List<BankAccount>
)