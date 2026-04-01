package com.matechmatrix.shopflowpos.feature.purchase.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSourceType
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.CreatePurchaseRequest
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseRepository
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseSettings

class GetPurchaseSettingsUseCase(private val repo: PurchaseRepository) {
    suspend operator fun invoke(): PurchaseSettings = repo.getPurchaseSettings()
}

class CreatePurchaseOrderUseCase(private val repo: PurchaseRepository) {
    suspend operator fun invoke(request: CreatePurchaseRequest): AppResult<PurchaseReceiptData> {
        // Validation
        if (request.source.name.isBlank())
            return AppResult.Error("${if (request.source.type == PurchaseSourceType.SUPPLIER) "Supplier" else "Customer"} name is required")
        if (request.items.isEmpty())
            return AppResult.Error("Add at least one product")
        if (request.items.any { it.productName.isBlank() })
            return AppResult.Error("All items must have a product name")
        if (request.items.any { it.quantity <= 0 })
            return AppResult.Error("All quantities must be at least 1")
        if (request.items.any { it.unitCost < 0 })
            return AppResult.Error("Unit cost cannot be negative")

        val subtotal  = request.items.sumOf { it.totalCost }
        val total     = (subtotal - request.discountAmount).coerceAtLeast(0.0)
        val totalPaid = request.cashAmount + request.bankAmount
        if (totalPaid > total + 0.01)
            return AppResult.Error("Payment (${totalPaid.toLong()}) exceeds total (${total.toLong()})")
        if (request.cashAmount > 0 && request.cashAccountId.isBlank())
            return AppResult.Error("Select a cash account")
        if (request.bankAmount > 0 && request.bankAccountId.isNullOrBlank())
            return AppResult.Error("Select a bank account")
        return repo.createPurchaseOrder(request)
    }
}

class PayPurchaseDueUseCase(private val repo: PurchaseRepository) {
    suspend operator fun invoke(orderId: String, amount: Double, accountType: AccountType, accountId: String): AppResult<Unit> {
        if (amount <= 0) return AppResult.Error("Amount must be greater than zero")
        if (accountId.isBlank()) return AppResult.Error("Select a payment account")
        return repo.payPurchaseDue(orderId, amount, accountType, accountId)
    }
}