package com.matechmatrix.shopflowpos.feature.inventory.domain.usecase

import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.feature.inventory.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.Flow

// ─── Read ────────────────────────────────────────────────────────────────────

class GetPagedProductsUseCase(private val repo: InventoryRepository) {
    operator fun invoke(
        query    : String           = "",
        category : ProductCategory? = null
    ): Flow<PagingData<Product>> = repo.getProductsPaged(query, category)
}

class GetLowStockProductsUseCase(private val repo: InventoryRepository) {
    suspend operator fun invoke(): AppResult<List<Product>> = repo.getLowStockProducts()
}

class GetProductByIdUseCase(private val repo: InventoryRepository) {
    suspend operator fun invoke(id: String): AppResult<Product> = repo.getProductById(id)
}

// ─── Write ───────────────────────────────────────────────────────────────────

class SaveProductUseCase(private val repo: InventoryRepository) {
    /**
     * Validates and persists a product.
     * Returns [AppResult.Error] with a user-facing message on validation failure.
     */
    suspend operator fun invoke(product: Product, isNew: Boolean): AppResult<Unit> {
        // --- Validation ---
        if (product.name.isBlank())
            return AppResult.Error("Product name is required")

        if (product.sellingPrice <= 0)
            return AppResult.Error("Enter a valid sale price")

        if (product.category.hasImei && product.imei.isNullOrBlank())
            return AppResult.Error("IMEI is required for ${product.category.displayName}")

        if (product.category.hasImei && !isValidImei(product.imei!!))
            return AppResult.Error("IMEI must be 15 digits")

        // --- Persist ---
        return if (isNew) repo.insertProduct(product) else repo.updateProduct(product)
    }

    private fun isValidImei(imei: String): Boolean =
        imei.length == 15 && imei.all { it.isDigit() }
}

class UpdateStockUseCase(private val repo: InventoryRepository) {
    suspend operator fun invoke(productId: String, newStock: Int): AppResult<Unit> {
        if (newStock < 0) return AppResult.Error("Stock cannot be negative")
        return repo.updateStock(productId, newStock)
    }
}

class DeleteProductUseCase(private val repo: InventoryRepository) {
    suspend operator fun invoke(productId: String): AppResult<Unit> =
        repo.deactivateProduct(productId)
}

// ─── Settings ────────────────────────────────────────────────────────────────

class GetInventorySettingsUseCase(private val repo: InventoryRepository) {
    suspend operator fun invoke(): InventorySettings = InventorySettings(
        showCostPrice  = repo.getShowCostPrice(),
        currencySymbol = repo.getCurrencySymbol()
    )
}

data class InventorySettings(
    val showCostPrice  : Boolean,
    val currencySymbol : String
)