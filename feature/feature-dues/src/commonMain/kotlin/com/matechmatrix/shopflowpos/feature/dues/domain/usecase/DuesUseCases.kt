package com.matechmatrix.shopflowpos.feature.dues.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.dues.domain.repository.DuesRepository

data class DuesPageData(
    val customersWithDues  : List<Customer>,
    val salesWithDue       : List<Sale>,
    val suppliersWithDues  : List<Supplier>,
    val purchaseOrdersWithDue: List<PurchaseOrder>,
    val cashAccounts       : List<CashAccount>,
    val bankAccounts       : List<BankAccount>,
    val currencySymbol     : String
)

class LoadDuesPageUseCase(private val repo: DuesRepository) {
    suspend operator fun invoke(): DuesPageData {
        fun <T> AppResult<T>.orEmpty(empty: T): T = if (this is AppResult.Success) data else empty
        return DuesPageData(
            customersWithDues   = repo.getCustomersWithDues().orEmpty(emptyList()),
            salesWithDue        = repo.getSalesWithDue().orEmpty(emptyList()),
            suppliersWithDues   = repo.getSuppliersWithDues().orEmpty(emptyList()),
            purchaseOrdersWithDue = repo.getPurchaseOrdersWithDue().orEmpty(emptyList()),
            cashAccounts        = repo.getActiveCashAccounts().orEmpty(emptyList()),
            bankAccounts        = repo.getActiveBankAccounts().orEmpty(emptyList()),
            currencySymbol      = repo.getCurrencySymbol()
        )
    }
}

class CollectSaleDueUseCase(private val repo: DuesRepository) {
    suspend operator fun invoke(
        sale        : Sale,
        amount      : Double,
        accountType : AccountType,
        accountId   : String
    ): AppResult<Unit> {
        if (amount <= 0) return AppResult.Error("Amount must be greater than zero")
        if (amount > sale.dueAmount + 0.01) return AppResult.Error("Amount exceeds due (${sale.dueAmount.toLong()})")
        if (accountId.isBlank()) return AppResult.Error("Select a payment account")
        return repo.collectSaleDue(sale, amount, accountType, accountId)
    }
}

class PayPurchaseDueFromDuesUseCase(private val repo: DuesRepository) {
    suspend operator fun invoke(
        order       : PurchaseOrder,
        amount      : Double,
        accountType : AccountType,
        accountId   : String
    ): AppResult<Unit> {
        if (amount <= 0) return AppResult.Error("Amount must be greater than zero")
        if (amount > order.dueAmount + 0.01) return AppResult.Error("Amount exceeds due (${order.dueAmount.toLong()})")
        if (accountId.isBlank()) return AppResult.Error("Select a payment account")
        return repo.payPurchaseDue(order, amount, accountType, accountId)
    }
}