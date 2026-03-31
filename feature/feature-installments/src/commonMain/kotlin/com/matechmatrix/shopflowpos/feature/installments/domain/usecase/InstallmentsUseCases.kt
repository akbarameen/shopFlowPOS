package com.matechmatrix.shopflowpos.feature.installments.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.InstallmentFrequency
import com.matechmatrix.shopflowpos.feature.installments.domain.repository.InstallmentsRepository

class GetAllPlansUseCase(private val repo: InstallmentsRepository) {
    suspend operator fun invoke(): AppResult<List<InstallmentPlan>> = repo.getAllPlans()
}

class GetOverduePlansUseCase(private val repo: InstallmentsRepository) {
    suspend operator fun invoke(nowMs: Long): AppResult<List<InstallmentPlan>> = repo.getOverduePlans(nowMs)
}

class GetPlanPaymentsUseCase(private val repo: InstallmentsRepository) {
    suspend operator fun invoke(planId: String): AppResult<List<InstallmentPayment>> = repo.getPlanPayments(planId)
}

class CreateInstallmentPlanUseCase(private val repo: InstallmentsRepository) {
    suspend operator fun invoke(plan: InstallmentPlan): AppResult<Unit> {
        if (plan.customerName.isBlank()) return AppResult.Error("Customer name is required")
        if (plan.productName.isBlank()) return AppResult.Error("Product / item description is required")
        if (plan.totalAmount <= 0) return AppResult.Error("Enter a valid total amount")
        if (plan.totalInstallments < 1) return AppResult.Error("Number of installments must be at least 1")
        if (plan.downPayment < 0 || plan.downPayment >= plan.totalAmount)
            return AppResult.Error("Down payment must be between 0 and total amount")
        return repo.insertPlan(plan)
    }
}

class RecordInstallmentPaymentUseCase(private val repo: InstallmentsRepository) {
    suspend operator fun invoke(
        plan        : InstallmentPlan,
        amount      : Double,
        accountType : AccountType,
        accountId   : String
    ): AppResult<Unit> {
        if (amount <= 0) return AppResult.Error("Amount must be greater than zero")
        if (amount > plan.remainingAmount + 0.01) return AppResult.Error("Amount exceeds remaining balance")
        if (accountId.isBlank()) return AppResult.Error("Select a payment account")

        // Compute next due date based on frequency
        val nextDue = plan.nextDueDate + when (plan.frequency) {
            InstallmentFrequency.WEEKLY    -> 7L * 24 * 60 * 60 * 1000
            InstallmentFrequency.BIWEEKLY  -> 14L * 24 * 60 * 60 * 1000
            InstallmentFrequency.MONTHLY   -> 30L * 24 * 60 * 60 * 1000
            InstallmentFrequency.QUARTERLY -> 90L * 24 * 60 * 60 * 1000
        }
        return repo.recordPayment(plan, amount, accountType, accountId, nextDue)
    }
}

data class InstallmentSettings(
    val currencySymbol : String,
    val cashAccounts   : List<CashAccount>,
    val bankAccounts   : List<BankAccount>,
    val customers      : List<Customer>
)

class GetInstallmentSettingsUseCase(private val repo: InstallmentsRepository) {
    suspend operator fun invoke(): InstallmentSettings = InstallmentSettings(
        currencySymbol = repo.getCurrencySymbol(),
        cashAccounts   = (repo.getActiveCashAccounts() as? AppResult.Success)?.data ?: emptyList(),
        bankAccounts   = (repo.getActiveBankAccounts() as? AppResult.Success)?.data ?: emptyList(),
        customers      = (repo.getAllCustomers() as? AppResult.Success)?.data ?: emptyList()
    )
}