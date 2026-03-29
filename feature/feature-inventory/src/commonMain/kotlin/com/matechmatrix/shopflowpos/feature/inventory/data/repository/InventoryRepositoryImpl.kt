package com.matechmatrix.shopflowpos.feature.inventory.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import com.matechmatrix.shopflowpos.feature.inventory.data.paging.ProductPagingSource
import com.matechmatrix.shopflowpos.feature.inventory.domain.repository.InventoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

private const val PAGE_SIZE = 30

class InventoryRepositoryImpl(private val db: DatabaseProvider) : InventoryRepository {

    // ── Row mapper ───────────────────────────────────────────────────────────

    private fun mapRow(row: com.matechmatrix.shopflowpos.db.Product) = Product(
        id                = row.id,
        name              = row.name,
        brand             = row.brand,
        model             = row.model,
        imei              = row.imei,
        barcode           = row.barcode,
        category          = runCatching { ProductCategory.valueOf(row.category) }
            .getOrDefault(ProductCategory.OTHER),
        condition         = runCatching { ProductCondition.valueOf(row.condition_type) }
            .getOrDefault(ProductCondition.NEW),
        ptaStatus         = row.pta_status,
        costPrice         = row.cost_price,
        sellingPrice      = row.selling_price,
        stock             = row.stock.toInt(),
        lowStockThreshold = row.low_stock_threshold.toInt(),
        description       = row.notes,
        isActive          = row.is_active == 1L,
        createdAt         = row.created_at,
        updatedAt         = row.updated_at,
        imageUri          = row.image_uri,
        color             = row.color,
        storageGb         = row.storage_gb?.toInt(),
        ramGb             = row.ram_gb?.toInt(),
        romGb             = row.rom_gb?.toInt(),
        batteryMah        = row.battery_mah?.toInt(),
        screenSizeInch    = row.screen_size_inch?.toFloat(),
        processor         = row.processor,
    )

    // ── Paged stream ─────────────────────────────────────────────────────────

    override fun getProductsPaged(
        query    : String,
        category : ProductCategory?
    ): Flow<PagingData<Product>> = Pager(
        config = PagingConfig(
            pageSize         = PAGE_SIZE,
            prefetchDistance = PAGE_SIZE / 2,
            enablePlaceholders = false
        )
    ) {
        ProductPagingSource(
            queries  = db.productQueries,
            query    = query,
            category = category,
            mapper   = ::mapRow
        )
    }.flow.flowOn(Dispatchers.Default)

    // ── One-shot queries ──────────────────────────────────────────────────────

    override suspend fun getAllProducts(): AppResult<List<Product>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.productQueries.getAllActive(limit = Long.MAX_VALUE, offset = 0)
                        .executeAsList().map(::mapRow)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load products") }
        }

    override suspend fun getLowStockProducts(): AppResult<List<Product>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.productQueries.getLowStock().executeAsList().map(::mapRow))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to get low stock") }
        }

    override suspend fun getProductById(id: String): AppResult<Product> =
        withContext(Dispatchers.Default) {
            runCatching {
                val row = db.productQueries.getById(id).executeAsOneOrNull()
                    ?: return@withContext AppResult.Error("Product not found")
                AppResult.Success(mapRow(row))
            }.getOrElse { AppResult.Error(it.message ?: "Failed to get product") }
        }

    // ── Mutations ─────────────────────────────────────────────────────────────

    override suspend fun insertProduct(product: Product): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                val id  = if (product.id.isBlank()) IdGenerator.generate() else product.id
                db.productQueries.insertProduct(
                    id               = id,
                    name             = product.name,
                    brand            = product.brand,
                    model            = product.model,
                    imei             = product.imei,
                    cost_price       = product.costPrice,
                    selling_price    = product.sellingPrice,
                    stock            = product.stock.toLong(),
                    low_stock_threshold = product.lowStockThreshold.toLong(),
                    condition_type   = product.condition.name,
                    category         = product.category.name,
                    pta_status       = product.ptaStatus,
                    image_uri        = product.imageUri,
                    barcode          = product.barcode,
                    notes            = product.description,
                    is_active        = 1L,
                    created_at       = if (product.createdAt > 0) product.createdAt else now,
                    updated_at       = now,
                    color            = product.color,
                    storage_gb       = product.storageGb?.toLong(),
                    ram_gb           = product.ramGb?.toLong(),
                    rom_gb           = product.romGb?.toLong(),
                    battery_mah      = product.batteryMah?.toLong(),
                    screen_size_inch = product.screenSizeInch?.toDouble(),
                    processor        = product.processor,
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to add product") }
        }

    override suspend fun updateProduct(product: Product): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                db.productQueries.updateProduct(
                    name             = product.name,
                    brand            = product.brand,
                    model            = product.model,
                    cost_price       = product.costPrice,
                    selling_price    = product.sellingPrice,
                    stock            = product.stock.toLong(),
                    low_stock_threshold = product.lowStockThreshold.toLong(),
                    condition_type   = product.condition.name,
                    category         = product.category.name,
                    pta_status       = product.ptaStatus,
                    notes            = product.description,
                    color            = product.color,
                    storage_gb       = product.storageGb?.toLong(),
                    ram_gb           = product.ramGb?.toLong(),
                    rom_gb           = product.romGb?.toLong(),
                    battery_mah      = product.batteryMah?.toLong(),
                    screen_size_inch = product.screenSizeInch?.toDouble(),
                    processor        = product.processor,
                    updated_at       = now,
                    id               = product.id,
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to update product") }
        }

    override suspend fun updateStock(productId: String, newStock: Int): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.productQueries.updateStock(
                    newStock.toLong(),
                    Clock.System.now().toEpochMilliseconds(),
                    productId
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to update stock") }
        }

    override suspend fun markAsSold(productId: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.productQueries.markAsSold(Clock.System.now().toEpochMilliseconds(), productId)
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to mark as sold") }
        }

    override suspend fun deactivateProduct(productId: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.productQueries.softDelete(Clock.System.now().toEpochMilliseconds(), productId)
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to deactivate product") }
        }

    // ── Settings ──────────────────────────────────────────────────────────────

    override suspend fun getShowCostPrice(): Boolean = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("show_cost_price").executeAsOneOrNull() == "true"
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }

    // ── Suppliers ─────────────────────────────────────────────────────────────

    override suspend fun getAllSuppliers(): AppResult<List<Supplier>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(db.supplierQueries.getAllSuppliers().executeAsList().map { r ->
                    Supplier(
                        id        = r.id,
                        name      = r.name,
                        phone     = r.phone,
                        address   = r.address,
                        email     = r.email,
                        notes     = r.notes,
                        createdAt = r.created_at
                    )
                })
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load suppliers") }
        }
}