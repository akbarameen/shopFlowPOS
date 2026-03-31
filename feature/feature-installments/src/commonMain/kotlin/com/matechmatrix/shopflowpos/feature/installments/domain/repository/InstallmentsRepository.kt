package com.matechmatrix.shopflowpos.feature.installments.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType

interface InstallmentsRepository {
    suspend fun getAllPlans(): AppResult<List<InstallmentPlan>>
    suspend fun getActivePlans(): AppResult<List<InstallmentPlan>>
    suspend fun getOverduePlans(nowMs: Long): AppResult<List<InstallmentPlan>>
    suspend fun getPlanById(id: String): AppResult<InstallmentPlan>
    suspend fun getPlanPayments(planId: String): AppResult<List<InstallmentPayment>>

    suspend fun insertPlan(plan: InstallmentPlan): AppResult<Unit>
    suspend fun markPlanCompleted(planId: String): AppResult<Unit>
    suspend fun markPlanDefaulted(planId: String): AppResult<Unit>

    /**
     * Atomically:
     *   1. insertInstallmentPayment
     *   2. updatePlanOnPayment (paid_amount, remaining, next_due_date, is_completed)
     *   3. updateAccountBalance + insertLedgerEntry (CREDIT, INSTALLMENT_PAYMENT)
     *   4. decrementCustomerOutstanding if plan.customerId != null
     */
    suspend fun recordPayment(
        plan        : InstallmentPlan,
        amount      : Double,
        accountType : AccountType,
        accountId   : String,
        nextDueDate : Long
    ): AppResult<Unit>

    suspend fun getAllCustomers(): AppResult<List<Customer>>
    suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>>
    suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>>
    suspend fun getCurrencySymbol(): String
}