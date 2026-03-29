package com.matechmatrix.shopflowpos.feature.customers.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.feature.customers.domain.repository.CustomersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class CustomersRepositoryImpl(private val db: DatabaseProvider) : CustomersRepository {

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Customer) = Customer(
        id = r.id,
        name = r.name,
        phone = r.phone,
        cnic = r.cnic,
        address = r.address,
        notes = r.notes,
        dueBalance = 0.0, // Should be calculated or fetched from ledger if needed
        totalPurchases = r.total_purchases,
        totalTransactions = r.total_transactions.toInt(),
        createdAt = r.created_at
    )

    override suspend fun getAllCustomers(): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            try {
                AppResult.Success(
                    db.customerQueries.getAllCustomers().executeAsList().map(::mapRow)
                )
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to load customers")
            }
        }

    override suspend fun searchCustomers(query: String): AppResult<List<Customer>> =
        withContext(Dispatchers.Default) {
            try {
                AppResult.Success(
                    db.customerQueries.searchCustomers(query, query, query).executeAsList().map(::mapRow)
                )
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Search failed")
            }
        }

    override suspend fun insertCustomer(customer: Customer): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            try {
                val now = Clock.System.now().toEpochMilliseconds()
                db.customerQueries.insertCustomer(
                    id = IdGenerator.generate(),
                    name = customer.name,
                    phone = customer.phone,
                    cnic = customer.cnic,
                    address = customer.address,
                    notes = customer.notes,
                    total_purchases = customer.totalPurchases,
                    total_transactions = customer.totalTransactions.toLong(),
                    created_at = now
                )
                AppResult.Success(Unit)
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to add customer")
            }
        }

    override suspend fun updateCustomer(customer: Customer): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            try {
                db.customerQueries.updateCustomer(
                    name = customer.name,
                    phone = customer.phone,
                    cnic = customer.cnic,
                    address = customer.address,
                    notes = customer.notes,
                    id = customer.id
                )
                AppResult.Success(Unit)
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to update customer")
            }
        }

    override suspend fun deleteCustomer(id: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            try {
                db.customerQueries.deleteCustomer(id)
                AppResult.Success(Unit)
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to delete customer")
            }
        }

    override suspend fun collectDue(customerId: String, amount: Long): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            try {
                val now = Clock.System.now().toEpochMilliseconds()
                val current = db.ledgerQueries.getCashBalance().executeAsOne()
                db.ledgerQueries.updateCashBalance(current + amount.toDouble(), now)

                db.ledgerQueries.insertLedgerEntry(
                    id = IdGenerator.generate(),
                    type = "CREDIT",
                    amount = amount.toDouble(),
                    account_type = "CASH",
                    bank_account_id = null,
                    reference_id = customerId,
                    description = "Due collection from customer",
                    balance_after = current + amount.toDouble(),
                    created_at = now
                )
                AppResult.Success(Unit)
            } catch (e: Exception) {
                AppResult.Error(e.message ?: "Failed to collect due")
            }
        }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}
