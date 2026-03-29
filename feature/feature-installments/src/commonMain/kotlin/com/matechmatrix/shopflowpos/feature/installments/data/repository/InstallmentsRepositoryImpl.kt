package com.matechmatrix.shopflowpos.feature.installments.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.Installment
import com.matechmatrix.shopflowpos.feature.installments.domain.repository.InstallmentsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class InstallmentsRepositoryImpl(private val db: DatabaseProvider) : InstallmentsRepository {

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Installment) = Installment(
        id = r.id,
        customerId = r.customer_id,
        customerName = r.customer_name,
        productId = r.product_id,
        productName = r.product_name,
        totalAmount = r.total_amount,
        downPayment = r.down_payment,
        remainingAmount = r.remaining_amount,
        monthlyAmount = r.monthly_amount,
        totalMonths = r.total_months.toInt(),
        paidMonths = r.paid_months.toInt(),
        startDate = r.start_date,
        nextDueDate = r.next_due_date,
        isCompleted = r.is_completed == 1L,
        createdAt = r.created_at
    )

    override suspend fun getAllInstallments(): AppResult<List<Installment>> = withContext(Dispatchers.IO) {
        try {
            val list = db.installmentQueries.getAllInstallments().executeAsList().map(::mapRow)
            AppResult.Success(list)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load installments")
        }
    }

    override suspend fun insertInstallment(installment: Installment): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.installmentQueries.insertInstallment(
                id = installment.id,
                customer_id = installment.customerId,
                customer_name = installment.customerName,
                product_id = installment.productId,
                product_name = installment.productName,
                total_amount = installment.totalAmount,
                down_payment = installment.downPayment,
                remaining_amount = installment.remainingAmount,
                monthly_amount = installment.monthlyAmount,
                total_months = installment.totalMonths.toLong(),
                paid_months = 0L,
                start_date = installment.startDate,
                next_due_date = installment.nextDueDate,
                is_completed = 0L,
                created_at = installment.createdAt
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to save installment")
        }
    }

    override suspend fun recordPayment(installmentId: String, nextDueDate: Long): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            db.transaction {
                val current = db.installmentQueries.getInstallmentById(installmentId).executeAsOne()

                // 1. Update Installment Table
                db.installmentQueries.updateInstallmentPayment(
                    remaining_amount = current.monthly_amount, // Deducts monthly amount from total remaining
                    next_due_date = nextDueDate,
                    id = installmentId
                )

                // 2. Update Cash Balance and Ledger
                val now = Clock.System.now().toEpochMilliseconds()
                val currentCash = db.ledgerQueries.getCashBalance().executeAsOneOrNull() ?: 0.0
                val newBalance = currentCash + current.monthly_amount

                db.ledgerQueries.updateCashBalance(newBalance, now)
                db.ledgerQueries.insertLedgerEntry(
                    id = IdGenerator.generate(),
                    type = "CREDIT",
                    amount = current.monthly_amount,
                    account_type = "CASH",
                    bank_account_id = null,
                    reference_id = installmentId,
                    description = "Installment Payment: ${current.customer_name}",
                    balance_after = newBalance,
                    created_at = now
                )
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to record payment")
        }
    }

    override suspend fun deleteInstallment(id: String): AppResult<Unit> = withContext(Dispatchers.IO) {
        // Note: You need a deleteInstallment query in your .sq file
        AppResult.Error("Delete query not implemented in .sq")
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.IO) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}