// ════════════════════════════════════════════════════════════════════════════
// feature/customers/data/repository/CustomersRepositoryImpl.kt
// ════════════════════════════════════════════════════════════════════════════
package com.matechmatrix.shopflowpos.feature.customers.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils.currentTime
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CashAccount
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.customers.domain.repository.CustomersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class CustomersRepositoryImpl(private val db: DatabaseProvider) : CustomersRepository {

    // ── Row Mapper ───────────────────────────────────────────────────────────

    private fun mapCustomer(r: com.matechmatrix.shopflowpos.db.Customer) = Customer(
        id                 = r.id,
        name               = r.name,
        phone              = r.phone,
        whatsapp           = r.whatsapp,
        cnic               = r.cnic,
        email              = r.email,
        address            = r.address,
        city               = r.city,
        creditLimit        = r.credit_limit,
        openingBalance     = r.opening_balance,
        outstandingBalance = r.outstanding_balance,
        totalPurchases     = r.total_purchases,
        totalTransactions  = r.total_transactions.toInt(),
        notes              = r.notes,
        isActive           = r.is_active == 1L,
        createdAt          = r.created_at,
        updatedAt          = r.updated_at
    )

    private fun mapCashAccount(r: com.matechmatrix.shopflowpos.db.Cash_account) = CashAccount(
        id        = r.id,
        name      = r.name,
        balance   = r.balance,
        isActive  = r.is_active == 1L,
        createdAt = r.created_at,
        updatedAt = r.updated_at
    )

    private fun mapBankAccount(r: com.matechmatrix.shopflowpos.db.Bank_account) = BankAccount(
        id            = r.id,
        bankName      = r.bank_name,
        accountTitle  = r.account_title,
        accountNumber = r.account_number,
        iban          = r.iban,
        balance       = r.balance,
        isActive      = r.is_active == 1L,
        createdAt     = r.created_at,
        updatedAt     = r.updated_at
    )

    // ── Read ─────────────────────────────────────────────────────────────────

    override suspend fun getAllCustomers(): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.customerQueries.getAllActiveCustomers().executeAsList().map(::mapCustomer)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load customers") }
        }

    override suspend fun searchCustomers(query: String): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.customerQueries.searchCustomers(query).executeAsList().map(::mapCustomer)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Search failed") }
        }

    override suspend fun getCustomerById(id: String): AppResult<Customer> =
        withContext(Dispatchers.Default) {
            runCatching {
                val row = db.customerQueries.getCustomerById(id).executeAsOneOrNull()
                    ?: return@withContext AppResult.Error("Customer not found")
                AppResult.Success(mapCustomer(row))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to get customer") }
        }

    override suspend fun getCustomersWithDues(): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.customerQueries.getCustomersWithDues().executeAsList().map(::mapCustomer)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load dues") }
        }

    // ── Write ─────────────────────────────────────────────────────────────────

    override suspend fun insertCustomer(customer: Customer): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                db.customerQueries.insertCustomer(
                    id                  = customer.id,
                    name                = customer.name,
                    phone               = customer.phone,
                    whatsapp            = customer.whatsapp,
                    cnic                = customer.cnic,
                    email               = customer.email,
                    address             = customer.address,
                    city                = customer.city,
                    credit_limit        = customer.creditLimit,
                    opening_balance     = customer.openingBalance,
                    outstanding_balance = customer.openingBalance, // start with opening
                    notes               = customer.notes,
                    created_at          = now,
                    updated_at          = now
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to add customer") }
        }

    override suspend fun updateCustomer(customer: Customer): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                db.customerQueries.updateCustomer(
                    name         = customer.name,
                    phone        = customer.phone,
                    whatsapp     = customer.whatsapp,
                    cnic         = customer.cnic,
                    email        = customer.email,
                    address      = customer.address,
                    city         = customer.city,
                    credit_limit = customer.creditLimit,
                    notes        = customer.notes,
                    updated_at   = now,
                    id           = customer.id
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to update customer") }
        }

    override suspend fun softDeleteCustomer(customerId: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.customerQueries.softDeleteCustomer(
                    updated_at = Clock.System.now().toEpochMilliseconds(),
                    id         = customerId
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to delete customer") }
        }

    // ── Collect Due ───────────────────────────────────────────────────────────
    /**
     * Records a customer payment atomically:
     *   1. Decrements customer.outstanding_balance
     *   2. Credits the selected account balance
     *   3. Inserts a ledger_entry
     *
     * NOTE: SQLDelight KMP transaction API: db.database.transaction { ... }
     * Adapt `db.database` to however your DatabaseProvider exposes the underlying DB.
     */
    override suspend fun collectDue(
        customerId   : String,
        customerName : String,
        amount       : Double,
        accountType  : AccountType,
        accountId    : String
    ): AppResult<Unit> = withContext(Dispatchers.Default) {
        runCatching {
            val now     = Clock.System.now().toEpochMilliseconds()
            val entryId = IdGenerator.generate()

            // All three mutations run inside a single SQLite transaction
            db.database.transaction {
                // 1. Update customer outstanding balance
                db.customerQueries.decrementCustomerOutstanding(
                    amount     = amount,
                    now        = now,
                    customerId = customerId
                )

                // 2. Credit the account + 3. Insert ledger entry
                when (accountType) {
                    AccountType.CASH -> {
                        val current = db.ledgerQueries.getCashAccountById(accountId)
                            .executeAsOne().balance
                        val newBalance = current + amount
                        db.ledgerQueries.updateCashBalance(newBalance, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id             = entryId,
                            account_type   = "CASH",
                            account_id     = accountId,
                            entry_type     = "CREDIT",
                            reference_type = "SALE_PAYMENT",
                            reference_id   = customerId,
                            amount         = amount,
                            balance_after  = newBalance,
                            description    = "Due collection — $customerName",
                            created_at     = now
                        )
                    }
                    AccountType.BANK -> {
                        val current = db.ledgerQueries.getBankAccountById(accountId)
                            .executeAsOne().balance
                        val newBalance = current + amount
                        db.ledgerQueries.updateBankBalance(newBalance, now, accountId)
                        db.ledgerQueries.insertLedgerEntry(
                            id             = entryId,
                            account_type   = "BANK",
                            account_id     = accountId,
                            entry_type     = "CREDIT",
                            reference_type = "SALE_PAYMENT",
                            reference_id   = customerId,
                            amount         = amount,
                            balance_after  = newBalance,
                            description    = "Due collection — $customerName",
                            created_at     = now
                        )
                    }
                    else -> {
                        // MOBILE_WALLET: future implementation
                        rollback()
                        throw UnsupportedOperationException("Mobile wallet payment not yet supported")
                    }
                }
            }
            AppResult.Success(Unit)
        }.getOrElse { AppResult.Error(it.message ?: "Failed to collect payment") }
    }

    // ── Accounts ──────────────────────────────────────────────────────────────

    override suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getAllActiveCashAccounts().executeAsList().map(::mapCashAccount)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load cash accounts") }
        }

    override suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.ledgerQueries.getAllActiveBankAccounts().executeAsList().map(::mapBankAccount)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load bank accounts") }
        }

    // ── Settings ──────────────────────────────────────────────────────────────

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}