package com.matechmatrix.shopflowpos.feature.pos.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CartItem
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import kotlinx.coroutines.flow.Flow

interface PosRepository {
    // ── Products (paged) ──────────────────────────────────────────────────────
    fun getProductsPaged(
        query    : String           = "",
        category : ProductCategory? = null
    ): Flow<PagingData<Product>>

    // ── Reference data ────────────────────────────────────────────────────────
    suspend fun getAllCustomers(): AppResult<List<Customer>>
    suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>>
    suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>>

    // ── Sale transaction ──────────────────────────────────────────────────────
    /**
     * Atomically:
     *   1. Increments invoice_sequence → invoice number
     *   2. Inserts sale + sale_items
     *   3. Inserts sale_payment row(s)
     *   4. Updates account balances + inserts ledger_entry row(s)
     *   5. Decrements / marks-sold product stock
     *   6. Inserts stock_movement row(s)
     *   7. Updates customer stats if customerId != null
     *
     * Returns the fully constructed [ReceiptData] so the UI can display the receipt.
     */
    suspend fun completeSale(request: CompleteSaleRequest): AppResult<ReceiptData>

    // ── Settings ──────────────────────────────────────────────────────────────
    suspend fun getPosSettings(): PosSettings
}

// ── Checkout request model ───────────────────────────────────────────────────

data class CompleteSaleRequest(
    val cart          : List<CartItem>,
    val discountAmount: Double        = 0.0,
    val taxRate       : Double        = 0.0,  // percentage e.g. 17.0 for 17% GST
    // Customer snapshot — null for walk-in
    val customer      : Customer?     = null,
    val walkinName    : String        = "Walk-in Customer",
    val walkinPhone   : String        = "",
    // Payment breakdown
    val cashAmount    : Double        = 0.0,
    val cashAccountId : String        = "default_cash",
    val bankAmount    : Double        = 0.0,
    val bankAccountId : String?       = null,
    val dueDate       : Long?         = null,
    val notes         : String        = ""
)

// ── POS settings ─────────────────────────────────────────────────────────────

data class PosSettings(
    val shopName       : String,
    val shopAddress    : String,
    val shopPhone      : String,
    val currencySymbol : String,
    val taxRate        : Double,        // from settings key 'tax_rate'
    val cashAccounts   : List<CashAccount>,
    val bankAccounts   : List<BankAccount>
)
