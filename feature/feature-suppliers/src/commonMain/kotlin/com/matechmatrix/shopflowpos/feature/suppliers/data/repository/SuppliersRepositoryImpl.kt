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

    // ── Row mapper ───────────────────────────────────────────────────────────

    private fun mapRow(r: com.matechmatrix.shopflowpos.db.Supplier) = Supplier(
        id                 = r.id,
        name               = r.name,
        phone              = r.phone,
        whatsapp           = r.whatsapp,
        email              = r.email,
        address            = r.address,
        city               = r.city,
        ntn                = r.ntn,
        openingBalance     = r.opening_balance,
        outstandingBalance = r.outstanding_balance,
        totalPurchased     = r.total_purchased,
        notes              = r.notes,
        isActive           = r.is_active == 1L,
        createdAt          = r.created_at,
        updatedAt          = r.updated_at,
    )

    // ── Queries ──────────────────────────────────────────────────────────────

    override suspend fun getAllSuppliers(): AppResult<List<Supplier>> =
        withContext(Dispatchers.Default) {
            runCatching {
                AppResult.Success(
                    db.supplierQueries.getAllActiveSuppliers().executeAsList().map(::mapRow)
                )
            }.getOrElse { AppResult.Error(it.message ?: "Failed to load suppliers") }
        }

    override suspend fun insertSupplier(supplier: Supplier): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                val id  = if (supplier.id.isBlank()) IdGenerator.generate() else supplier.id
                db.supplierQueries.insertSupplier(
                    id                  = id,
                    name                = supplier.name,
                    phone               = supplier.phone,
                    whatsapp            = supplier.whatsapp,
                    email               = supplier.email,
                    address             = supplier.address,
                    city                = supplier.city,
                    ntn                 = supplier.ntn,
                    opening_balance     = supplier.openingBalance,
                    outstanding_balance = supplier.openingBalance,   // starts equal to opening
                    notes               = supplier.notes,
                    created_at          = now,
                    updated_at          = now,
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to add supplier") }
        }

    override suspend fun updateSupplier(supplier: Supplier): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                val now = Clock.System.now().toEpochMilliseconds()
                // updateSupplier in schema: name, phone, whatsapp, email, address, city, ntn, notes, updated_at, id
                db.supplierQueries.updateSupplier(
                    name       = supplier.name,
                    phone      = supplier.phone,
                    whatsapp   = supplier.whatsapp,
                    email      = supplier.email,
                    address    = supplier.address,
                    city       = supplier.city,
                    ntn        = supplier.ntn,
                    notes      = supplier.notes,
                    updated_at = now,
                    id         = supplier.id,
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to update supplier") }
        }

    override suspend fun softDeleteSupplier(id: String): AppResult<Unit> =
        withContext(Dispatchers.Default) {
            runCatching {
                db.supplierQueries.softDeleteSupplier(
                    Clock.System.now().toEpochMilliseconds(),
                    id
                )
                AppResult.Success(Unit)
            }.getOrElse { AppResult.Error(it.message ?: "Failed to delete supplier") }
        }

    override suspend fun getCurrencySymbol(): String =
        withContext(Dispatchers.Default) {
            db.settingsQueries.getSetting("currency_symbol").executeAsOneOrNull() ?: "Rs."
        }
}