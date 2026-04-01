package com.matechmatrix.shopflowpos.feature.dues.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType

interface DuesRepository {
    // ── Customer dues (they owe me) ───────────────────────────────────────────
    suspend fun getCustomersWithDues(): AppResult<List<Customer>>
    suspend fun getSalesWithDue(customerId: String? = null): AppResult<List<Sale>>

    /**
     * Collect a payment from a customer against a specific sale's due.
     * Enforces: amount ≤ sale.dueAmount AND amount ≤ account.balance
     */
    suspend fun collectSaleDue(
        sale        : Sale,
        amount      : Double,
        accountType : AccountType,
        accountId   : String
    ): AppResult<Unit>

    // ── Supplier dues (I owe them) ────────────────────────────────────────────
    suspend fun getSuppliersWithDues(): AppResult<List<Supplier>>
    suspend fun getPurchaseOrdersWithDue(supplierId: String? = null): AppResult<List<PurchaseOrder>>

    /** Pay a supplier due — mirrors payPurchaseDue in PurchaseRepository. */
    suspend fun payPurchaseDue(
        purchaseOrder: PurchaseOrder,
        amount       : Double,
        accountType  : AccountType,
        accountId    : String
    ): AppResult<Unit>

    // ── Accounts ──────────────────────────────────────────────────────────────
    suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>>
    suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>>
    suspend fun getCurrencySymbol(): String
}