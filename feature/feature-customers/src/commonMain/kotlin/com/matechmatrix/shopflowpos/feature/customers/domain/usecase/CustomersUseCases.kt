package com.matechmatrix.shopflowpos.feature.customers.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CashAccount
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.feature.customers.domain.repository.CustomersRepository

// ── Read ─────────────────────────────────────────────────────────────────────

class GetAllCustomersUseCase(private val repo: CustomersRepository) {
    suspend operator fun invoke(): AppResult<List<Customer>> = repo.getAllCustomers()
}

class SearchCustomersUseCase(private val repo: CustomersRepository) {
    /** Returns all customers when query is blank; filtered results otherwise. */
    suspend operator fun invoke(query: String): AppResult<List<Customer>> =
        if (query.isBlank()) repo.getAllCustomers()
        else repo.searchCustomers(query)
}

class GetCustomerByIdUseCase(private val repo: CustomersRepository) {
    suspend operator fun invoke(id: String): AppResult<Customer> = repo.getCustomerById(id)
}

class GetCustomersWithDuesUseCase(private val repo: CustomersRepository) {
    suspend operator fun invoke(): AppResult<List<Customer>> = repo.getCustomersWithDues()
}

// ── Write ────────────────────────────────────────────────────────────────────

class SaveCustomerUseCase(private val repo: CustomersRepository) {
    suspend operator fun invoke(customer: Customer, isNew: Boolean): AppResult<Unit> {
        if (customer.name.isBlank())
            return AppResult.Error("Customer name is required")
        if (customer.phone.isNotBlank() && customer.phone.length < 7)
            return AppResult.Error("Enter a valid phone number")
        if (customer.cnic != null && customer.cnic!!.length !in 13..14)
            return AppResult.Error("CNIC must be 13 digits")
        if (customer.creditLimit < 0)
            return AppResult.Error("Credit limit cannot be negative")

        return if (isNew) repo.insertCustomer(customer) else repo.updateCustomer(customer)
    }
}

class SoftDeleteCustomerUseCase(private val repo: CustomersRepository) {
    /** Soft-deletes a customer. Their sale & ledger history is preserved. */
    suspend operator fun invoke(customerId: String): AppResult<Unit> =
        repo.softDeleteCustomer(customerId)
}

class CollectCustomerDueUseCase(private val repo: CustomersRepository) {
    suspend operator fun invoke(
        customerId   : String,
        customerName : String,
        amount       : Double,
        accountType  : AccountType,
        accountId    : String
    ): AppResult<Unit> {
        if (amount <= 0) return AppResult.Error("Amount must be greater than zero")
        if (accountId.isBlank()) return AppResult.Error("Select a payment account")
        return repo.collectDue(customerId, customerName, amount, accountType, accountId)
    }
}

// ── Settings ─────────────────────────────────────────────────────────────────

class GetCustomerSettingsUseCase(private val repo: CustomersRepository) {
    suspend operator fun invoke(): CustomerSettings = CustomerSettings(
        currencySymbol     = repo.getCurrencySymbol(),
        cashAccounts       = (repo.getActiveCashAccounts() as? AppResult.Success)?.data ?: emptyList(),
        bankAccounts       = (repo.getActiveBankAccounts() as? AppResult.Success)?.data ?: emptyList()
    )
}

data class CustomerSettings(
    val currencySymbol : String,
    val cashAccounts   : List<CashAccount>,
    val bankAccounts   : List<BankAccount>
)