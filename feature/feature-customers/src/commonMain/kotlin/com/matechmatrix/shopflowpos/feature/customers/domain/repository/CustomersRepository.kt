package com.matechmatrix.shopflowpos.feature.customers.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Customer

interface CustomersRepository {
    suspend fun getAllCustomers(): AppResult<List<Customer>>
    suspend fun searchCustomers(query: String): AppResult<List<Customer>>
    suspend fun insertCustomer(customer: Customer): AppResult<Unit>
    suspend fun updateCustomer(customer: Customer): AppResult<Unit>
    suspend fun deleteCustomer(id: String): AppResult<Unit>
    suspend fun collectDue(customerId: String, amount: Long): AppResult<Unit>
    suspend fun getCurrencySymbol(): String
}
