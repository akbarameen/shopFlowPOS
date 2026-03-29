package com.matechmatrix.shopflowpos.feature.suppliers.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Supplier

interface SuppliersRepository {
    suspend fun getAllSuppliers(): AppResult<List<Supplier>>
    suspend fun insertSupplier(supplier: Supplier): AppResult<Unit>
    suspend fun updateSupplier(supplier: Supplier): AppResult<Unit>
    suspend fun deleteSupplier(id: String): AppResult<Unit>
    suspend fun getCurrencySymbol(): String
}