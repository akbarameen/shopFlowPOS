package com.matechmatrix.shopflowpos.feature.installments.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.InstallmentFrequency
import com.matechmatrix.shopflowpos.feature.installments.domain.repository.InstallmentsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class InstallmentsRepositoryImpl(private val db: DatabaseProvider) : InstallmentsRepository {

    private fun mapPlan(r: com.matechmatrix.shopflowpos.db.Installment_plan) = InstallmentPlan(
        id                = r.id,
        planNumber        = r.plan_number,
        saleId            = r.sale_id,
        customerId        = r.customer_id,
        customerName      = r.customer_name,
        customerPhone     = r.customer_phone,
        customerCnic      = r.customer_cnic,
        customerAddress   = r.customer_address,
        productId         = r.product_id,
        productName       = r.product_name,
        imei              = r.imei,
        totalAmount       = r.total_amount,
        downPayment       = r.down_payment,
        financedAmount    = r.financed_amount,
        installmentAmount = r.installment_amount,
        totalInstallments = r.total_installments.toInt(),
        paidInstallments  = r.paid_installments.toInt(),
        paidAmount        = r.paid_amount,
        remainingAmount   = r.remaining_amount,
        frequency         = runCatching { InstallmentFrequency.valueOf(r.frequency) }.getOrDefault(InstallmentFrequency.MONTHLY),
        startDate         = r.start_date,
        nextDueDate       = r.next_due_date,
        isCompleted       = r.is_completed == 1L,
        isDefaulted       = r.is_defaulted == 1L,
        notes             = r.notes,
        createdAt         = r.created_at,
        updatedAt         = r.updated_at
    )

    private fun mapPayment(r: com.matechmatrix.shopflowpos.db.Installment_payment) = InstallmentPayment(
        id          = r.id,
        planId      = r.plan_id,
        amount      = r.amount,
        accountType = runCatching { AccountType.valueOf(r.account_type) }.getOrDefault(AccountType.CASH),
        accountId   = r.account_id,
        notes       = r.notes,
        paidAt      = r.paid_at
    )

    private fun mapCashAccount(r: com.matechmatrix.shopflowpos.db.Cash_account) = CashAccount(
        id = r.id, name = r.name, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )
    private fun mapBankAccount(r: com.matechmatrix.shopflowpos.db.Bank_account) = BankAccount(
        id = r.id, bankName = r.bank_name, accountTitle = r.account_title,
        accountNumber = r.account_number, iban = r.iban, balance = r.balance,
        isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )
    private fun mapCustomer(r: com.matechmatrix.shopflowpos.db.Customer) = Customer(
        id = r.id, name = r.name, phone = r.phone, whatsapp = r.whatsapp, cnic = r.cnic,
        email = r.email, address = r.address, city = r.city, creditLimit = r.credit_limit,
        openingBalance = r.opening_balance, outstandingBalance = r.outstanding_balance,
        totalPurchases = r.total_purchases, totalTransactions = r.total_transactions.toInt(),
        notes = r.notes, isActive = r.is_active == 1L, createdAt = r.created_at, updatedAt = r.updated_at
    )

    override suspend fun getAllPlans(): AppResult<List<InstallmentPlan>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.installmentQueries.getAllActivePlans().executeAsList().map(::mapPlan))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load plans") }
        }

    override suspend fun getActivePlans(): AppResult<List<InstallmentPlan>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.installmentQueries.getAllActivePlans().executeAsList().map(::mapPlan))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load active plans") }
        }

    override suspend fun getOverduePlans(nowMs: Long): AppResult<List<InstallmentPlan>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.installmentQueries.getOverduePlans(nowMs).executeAsList().map(::mapPlan))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load overdue plans") }
        }

    override suspend fun getPlanById(id: String): AppResult<InstallmentPlan> =
        withContext(Dispatchers.Default) {
            runCatching {
                val r = db.installmentQueries.getPlanById(id).executeAsOneOrNull()
                    ?: return@withContext AppResult.Error("Plan not found")
                AppResult.Success(mapPlan(r))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to get plan") }
        }

    override suspend fun getPlanPayments(planId: String): AppResult<List<InstallmentPayment>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.installmentQueries.getPlanPayments(planId).executeAsList().map(::mapPayment))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load payments") }
        }

    override suspend fun insertPlan(plan: InstallmentPlan): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                db.database.transaction {
                    // Generate plan number atomically
                    db.invoiceSequenceQueries.incrementSequence(now, "EMI")
                    val seq = db.invoiceSequenceQueries.getSequence("EMI").executeAsOne()
                    val planNumber = "EMI-${seq.toString().padStart(5, '0')}"

                    db.installmentQueries.insertInstallmentPlan(
                        id                 = plan.id,
                        plan_number        = planNumber,
                        sale_id            = plan.saleId,
                        customer_id        = plan.customerId,
                        customer_name      = plan.customerName,
                        customer_phone     = plan.customerPhone,
                        customer_cnic      = plan.customerCnic,
                        customer_address   = plan.customerAddress,
                        product_id         = plan.productId,
                        product_name       = plan.productName,
                        imei               = plan.imei,
                        total_amount       = plan.totalAmount,
                        down_payment       = plan.downPayment,
                        financed_amount    = plan.financedAmount,
                        installment_amount = plan.installmentAmount,
                        total_installments = plan.totalInstallments.toLong(),
                        paid_amount        = plan.downPayment,
                        remaining_amount   = plan.financedAmount,
                        frequency          = plan.frequency.name,
                        start_date         = plan.startDate,
                        next_due_date      = plan.nextDueDate,
                        notes              = plan.notes,
                        created_at         = now,
                        updated_at         = now
                    )
                }
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to create plan") }
        }

    override suspend fun recordPayment(
        plan        : InstallmentPlan,
        amount      : Double,
        accountType : AccountType,
        accountId   : String,
        nextDueDate : Long
    ): AppResult<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val now       = Clock.System.now().toEpochMilliseconds()
            val paymentId = IdGenerator.generate()
            val entryId   = IdGenerator.generate()

            db.database.transaction {
                // 1. Insert installment_payment row
                db.installmentQueries.insertInstallmentPayment(
                    id           = paymentId,
                    plan_id      = plan.id,
                    amount       = amount,
                    account_type = accountType.name,
                    account_id   = accountId,
                    notes        = null,
                    paid_at      = now
                )

                // 2. Update plan running balance
                db.installmentQueries.updatePlanOnPayment(
                    amount      = amount,
                    nextDueDate = nextDueDate,
                    now         = now,
                    planId      = plan.id
                )

                // 3. Credit the account + ledger
                when (accountType) {
                    AccountType.CASH -> {
                        val current    = db.ledgerQueries.getCashAccountById(accountId).executeAsOne()
                        val newBalance = current.balance + amount
                        db.ledgerQueries.updateCashBalance(newBalance, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id             = entryId,
                            account_type   = "CASH",
                            account_id     = accountId,
                            entry_type     = "CREDIT",
                            reference_type = "INSTALLMENT_PAYMENT",
                            reference_id   = plan.id,
                            amount         = amount,
                            balance_after  = newBalance,
                            description    = "EMI payment — ${plan.customerName} (${plan.planNumber})",
                            created_at     = now
                        )
                    }
                    AccountType.BANK -> {
                        val current    = db.ledgerQueries.getBankAccountById(accountId).executeAsOne()
                        val newBalance = current.balance + amount
                        db.ledgerQueries.updateBankBalance(newBalance, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id             = entryId,
                            account_type   = "BANK",
                            account_id     = accountId,
                            entry_type     = "CREDIT",
                            reference_type = "INSTALLMENT_PAYMENT",
                            reference_id   = plan.id,
                            amount         = amount,
                            balance_after  = newBalance,
                            description    = "EMI payment — ${plan.customerName} (${plan.planNumber})",
                            created_at     = now
                        )
                    }
                    else -> rollback()
                }

                // 4. Reduce customer outstanding if linked
                if (!plan.customerId.isNullOrBlank()) {
                    db.customerQueries.decrementCustomerOutstanding(amount, now, plan.customerId!!)
                }
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Failed to record payment") }
    }

    override suspend fun markPlanCompleted(planId: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.installmentQueries.markPlanCompleted(Clock.System.now().toEpochMilliseconds(), planId)
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to complete plan") }
        }

    override suspend fun markPlanDefaulted(planId: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.installmentQueries.markPlanDefaulted(Clock.System.now().toEpochMilliseconds(), planId)
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to mark defaulted") }
        }

    override suspend fun getAllCustomers(): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.customerQueries.getAllActiveCustomers().executeAsList().map(::mapCustomer))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load customers") }
        }

    override suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCashAccount))
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBankAccount))
            }.getOrElse { AppResult.Error(it.message ?: "Failed") }
        }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}