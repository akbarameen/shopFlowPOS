package com.matechmatrix.shopflowpos.feature.suppliers.data.repository

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.IdGenerator
import com.matechmatrix.shopflowpos.core.database.DatabaseProvider
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.feature.suppliers.domain.repository.SuppliersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class SuppliersRepositoryImpl(private val db: DatabaseProvider) : SuppliersRepository {

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Supplier) = Supplier(
        id = r.id,
        name = r.name,
        phone = r.phone,
        email = r.email,
        address = r.address,
        balance = 0.0, // Schema doesn't have balance, would need a separate ledger/transaction logic
        notes = r.notes,
        createdAt = r.created_at
    )

    override suspend fun getAllSuppliers(): AppResult<List<Supplier>> = withContext(Dispatchers.Default) {
        try {
            AppResult.Success(db.supplierQueries.getAllSuppliers().executeAsList().map(::mapRow))
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to load suppliers")
        }
    }

    override suspend fun insertSupplier(supplier: Supplier): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            db.supplierQueries.insertSupplier(
                id = IdGenerator.generate(),
                name = supplier.name,
                phone = supplier.phone,
                address = supplier.address,
                email = supplier.email,
                notes = supplier.notes,
                created_at = now
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to insert supplier")
        }
    }

    override suspend fun updateSupplier(supplier: Supplier): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            db.supplierQueries.updateSupplier(
                name = supplier.name,
                phone = supplier.phone,
                address = supplier.address,
                email = supplier.email,
                notes = supplier.notes,
                id = supplier.id
            )
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to update supplier")
        }
    }

    override suspend fun deleteSupplier(id: String): AppResult<Unit> = withContext(Dispatchers.Default) {
        try {
            db.supplierQueries.deleteSupplier(id)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Failed to delete supplier")
        }
    }

    override suspend fun getCurrencySymbol(): String = withContext(Dispatchers.Default) {
        db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
    }
}
