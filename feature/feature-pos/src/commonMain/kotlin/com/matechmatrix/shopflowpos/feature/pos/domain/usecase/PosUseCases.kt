package com.matechmatrix.shopflowpos.feature.pos.domain.usecase

import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.CompleteSaleRequest
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosRepository
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosSettings
import kotlinx.coroutines.flow.Flow

// ── Read ─────────────────────────────────────────────────────────────────────

class GetPosProductsPagedUseCase(private val repo: PosRepository) {
    operator fun invoke(
        query    : String           = "",
        category : ProductCategory? = null
    ): Flow<PagingData<Product>> = repo.getProductsPaged(query, category)
}

class GetPosCustomersUseCase(private val repo: PosRepository) {
    suspend operator fun invoke(): AppResult<List<Customer>> = repo.getAllCustomers()
}

class GetPosSettingsUseCase(private val repo: PosRepository) {
    suspend operator fun invoke(): PosSettings = repo.getPosSettings()
}

// ── Write ────────────────────────────────────────────────────────────────────

class CompleteSaleUseCase(private val repo: PosRepository) {

    suspend operator fun invoke(
        cart          : List<CartItem>,
        discountAmount: Double,
        paymentMethod : PaymentMethod,
        cashAmount    : Double,
        cashAccountId : String,
        bankAmount    : Double,
        bankAccountId : String?,
        customer      : Customer?,
        walkinName    : String,
        walkinPhone   : String,
        dueDate       : Long?,
        notes         : String,
        taxRate       : Double
    ): AppResult<ReceiptData> {
        // ── Validate ──────────────────────────────────────────────────────────
        if (cart.isEmpty())
            return AppResult.Error("Cart is empty")

        if (paymentMethod == PaymentMethod.CREDIT && customer == null)
            return AppResult.Error("Select a customer for credit sale")

        val subtotal = cart.sumOf { it.lineTotal }
        val afterDiscount = (subtotal - discountAmount).coerceAtLeast(0.0)
        val taxAmount = afterDiscount * (taxRate / 100.0)
        val total = afterDiscount + taxAmount

        val totalPaid = cashAmount + bankAmount
        val due = (total - totalPaid).coerceAtLeast(0.0)

        if (paymentMethod != PaymentMethod.CREDIT && due > 0 && customer == null)
            return AppResult.Error("Select a customer to save a due balance")

        if (customer != null && customer.hasCredit && !customer.canExtendCredit) {
            val remaining = customer.creditLimit - customer.outstandingBalance
            return AppResult.Error("Credit limit exceeded. Available: ${remaining.toInt()}")
        }

        if (paymentMethod == PaymentMethod.BANK && bankAccountId == null)
            return AppResult.Error("Select a bank account for bank payment")

        if (paymentMethod == PaymentMethod.SPLIT && bankAmount > 0 && bankAccountId == null)
            return AppResult.Error("Select a bank account for split payment")

        return repo.completeSale(
            CompleteSaleRequest(
                cart           = cart,
                discountAmount = discountAmount,
                taxRate        = taxRate,
                customer       = customer,
                walkinName     = walkinName.ifBlank { "Walk-in Customer" },
                walkinPhone    = walkinPhone,
                cashAmount     = if (paymentMethod == PaymentMethod.BANK) 0.0 else cashAmount,
                cashAccountId  = cashAccountId,
                bankAmount     = if (paymentMethod == PaymentMethod.CASH) 0.0 else bankAmount,
                bankAccountId  = bankAccountId,
                dueDate        = if (due > 0) dueDate else null,
                notes          = notes
            )
        )
    }
}