package com.matechmatrix.shopflowpos.feature.pos.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CartItem
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod

interface PosRepository {
    suspend fun getAllProducts(): AppResult<List<Product>>
    suspend fun searchProducts(query: String): AppResult<List<Product>>
    suspend fun getAllCustomers(): AppResult<List<Customer>>
    suspend fun getBankAccounts(): AppResult<List<BankAccount>>
    suspend fun completeSale(
        cartItems: List<CartItem>,
        discount: Long,
        paymentMethod: PaymentMethod,
        cashAmount: Long,
        bankAmount: Long,
        bankAccountId: String?,
        customerId: String?,
        customerName: String?,
        notes: String,
        dueDate: Long?
    ): AppResult<Sale>
    suspend fun getCurrencySymbol(): String
    suspend fun getShopName(): String
}
