package com.matechmatrix.shopflowpos.feature.inventory.domain.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory

interface InventoryRepository {
    suspend fun getAllProducts(): AppResult<List<Product>>
    suspend fun searchProducts(query: String): AppResult<List<Product>>
    suspend fun getProductsByCategory(category: ProductCategory): AppResult<List<Product>>
    suspend fun getLowStockProducts(): AppResult<List<Product>>
    suspend fun insertProduct(product: Product): AppResult<Unit>
    suspend fun updateProduct(product: Product): AppResult<Unit>
    suspend fun updateStock(productId: String, newStock: Int): AppResult<Unit>
    suspend fun deactivateProduct(productId: String): AppResult<Unit>
    suspend fun getShowCostPrice(): Boolean
    suspend fun getCurrencySymbol(): String
    suspend fun getAllSuppliers(): AppResult<List<Supplier>>
}
