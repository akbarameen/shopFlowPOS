package com.matechmatrix.shopflowpos.feature.inventory.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import com.matechmatrix.shopflowpos.feature.inventory.domain.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class InventoryRepositoryImpl(private val db: DatabaseProvider) : InventoryRepository {

    private fun mapRow(row: com.matechmatrix.shopflowpos.db.Product) = Product(
        id                = row.id,
        name              = row.name,
        brand             = row.brand,
        model             = row.model,
        imei              = row.imei,
        barcode           = row.barcode,
        category          = runCatching { ProductCategory.valueOf(row.category) }.getOrDefault(ProductCategory.OTHER),
        condition         = runCatching { ProductCondition.valueOf(row.condition_type) }.getOrDefault(ProductCondition.NEW),
        ptaStatus         = row.pta_status,
        costPrice         = row.cost_price,
        sellingPrice      = row.selling_price,
        stock             = row.stock.toInt(),
        lowStockThreshold = row.low_stock_threshold.toInt(),
        description       = row.notes,
        isActive          = row.is_active == 1L,
        createdAt         = row.created_at,
        updatedAt         = row.updated_at
    )

    override suspend fun getAllProducts(): AppResult<List<Product>> = withContext(Dispatchers.Default) {
        try {
            AppResult.Success(db.productQueries.getAllActive().executeAsList().map(::mapRow))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load products")
        }
    }

    override suspend fun searchProducts(query: String): AppResult<List<Product>> = withContext(Dispatchers.Default) {
        try {
            AppResult.Success(db.productQueries.searchByName(query, query, query, query).executeAsList().map(::mapRow))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Search failed")
        }
    }

    override suspend fun getProductsByCategory(category: ProductCategory): AppResult<List<Product>> = withContext(Dispatchers.Default) {
        try {
            AppResult.Success(db.productQueries.getByCategory(category.name).executeAsList().map(::mapRow))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to filter products")
        }
    }

    override suspend fun getLowStockProducts(): AppResult<List<Product>> = withContext(Dispatchers.Default) {
        try {
            AppResult.Success(db.productQueries.getLowStock().executeAsList().map(::mapRow))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to get low stock")
        }
    }

    override suspend fun insertProduct(product: Product): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val id = if (product.id.isBlank()) IdGenerator.generate() else product.id
            db.productQueries.insertProduct(
                id                  = id,
                name                = product.name,
                brand               = product.brand,
                model               = product.model,
                imei                = product.imei,
                cost_price          = product.costPrice,
                selling_price       = product.sellingPrice,
                stock               = product.stock.toLong(),
                low_stock_threshold = product.lowStockThreshold.toLong(),
                condition_type      = product.condition.name,
                category            = product.category.name,
                pta_status          = product.ptaStatus,
                image_uri           = product.imageUri,
                barcode             = product.barcode,
                notes               = product.description,
                is_active           = 1L,
                created_at          = if (product.createdAt > 0) product.createdAt else now,
                updated_at          = now
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to add product")
        }
    }

    override suspend fun updateProduct(product: Product): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.productQueries.updateProduct(
                name                = product.name,
                brand               = product.brand,
                model               = product.model,
                cost_price          = product.costPrice,
                selling_price       = product.sellingPrice,
                stock               = product.stock.toLong(),
                low_stock_threshold = product.lowStockThreshold.toLong(),
                condition_type      = product.condition.name,
                category            = product.category.name,
                pta_status          = product.ptaStatus,
                notes               = product.description,
                updated_at          = now,
                id                  = product.id
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update product")
        }
    }

    override suspend fun updateStock(productId: String, newStock: Int): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.productQueries.updateStock(newStock.toLong(), now, productId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update stock")
        }
    }

    override suspend fun deactivateProduct(productId: String): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.productQueries.softDelete(now, productId)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to deactivate product")
        }
    }

    override suspend fun getShowCostPrice(): Boolean = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("show_cost_price").executeAsOneOrNull() == "true"
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }

    override suspend fun getAllSuppliers(): AppResult<List<Supplier>> = withContext(Dispatchers.Default) {
        try {
            val rows = db.supplierQueries.getAllSuppliers().executeAsList()
            AppResult.Success(rows.map { r ->
                Supplier(
                    id = r.id,
                    name = r.name,
                    phone = r.phone,
                    address = r.address,
                    email = r.email,
                    notes = r.notes,
                    createdAt = r.created_at
                )
            })
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load suppliers")
        }
    }
}
