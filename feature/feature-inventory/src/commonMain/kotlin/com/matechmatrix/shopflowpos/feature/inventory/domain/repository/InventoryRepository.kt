package com.matechmatrix.shopflowpos.feature.inventory.domain.repository

import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {

    // ── Paged streams ────────────────────────────────────────────────────────
    /** Live paging flow — auto-refreshes when underlying DB changes. */
    fun getProductsPaged(
        query    : String        = "",
        category : ProductCategory? = null
    ): Flow<PagingData<Product>>

    // ── One-shot queries (used for summary stats, dialogs, etc.) ─────────────
    suspend fun getAllProducts(): AppResult<List<Product>>
    suspend fun getLowStockProducts(): AppResult<List<Product>>
    suspend fun getProductById(id: String): AppResult<Product>

    // ── Mutations ─────────────────────────────────────────────────────────────
    suspend fun insertProduct(product: Product): AppResult<Unit>
    suspend fun updateProduct(product: Product): AppResult<Unit>
    suspend fun updateStock(productId: String, newStock: Int): AppResult<Unit>
    suspend fun markAsSold(productId: String): AppResult<Unit>
    suspend fun deactivateProduct(productId: String): AppResult<Unit>

    // ── Settings ──────────────────────────────────────────────────────────────
    suspend fun getShowCostPrice(): Boolean
    suspend fun getCurrencySymbol(): String

    // ── Suppliers ─────────────────────────────────────────────────────────────
    suspend fun getAllSuppliers(): AppResult<List<Supplier>>
    suspend fun searchSuppliers(query: String): AppResult<List<Supplier>>
    suspend fun getSupplierById(id: String): AppResult<Supplier>
}