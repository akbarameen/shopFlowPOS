package com.matechmatrix.shopflowpos.feature.customers.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.CashAccount
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.enums.AccountType

interface CustomersRepository {
    suspend fun getAllCustomers(): AppResult<List<Customer>>
    suspend fun searchCustomers(query: String): AppResult<List<Customer>>
    suspend fun getCustomerById(id: String): AppResult<Customer>
    suspend fun getCustomersWithDues(): AppResult<List<Customer>>

    suspend fun insertCustomer(customer: Customer): AppResult<Unit>
    suspend fun updateCustomer(customer: Customer): AppResult<Unit>
    suspend fun softDeleteCustomer(customerId: String): AppResult<Unit>

    /**
     * Records a payment from a customer against their outstanding balance.
     * Atomically:
     *   1. Decrements customer.outstanding_balance
     *   2. Credits the selected account (cash or bank)
     *   3. Inserts a ledger_entry
     */
    suspend fun collectDue(
        customerId     : String,
        customerName   : String,
        amount         : Double,
        accountType    : AccountType,
        accountId      : String
    ): AppResult<Unit>

    suspend fun getActiveCashAccounts(): AppResult<List<CashAccount>>
    suspend fun getActiveBankAccounts(): AppResult<List<BankAccount>>
    suspend fun getCurrencySymbol(): String
}
