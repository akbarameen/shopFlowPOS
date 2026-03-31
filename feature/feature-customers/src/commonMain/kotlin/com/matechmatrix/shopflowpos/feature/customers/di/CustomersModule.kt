package com.matechmatrix.shopflowpos.feature.customers.di

// ════════════════════════════════════════════════════════════════════════════
// feature/customers/di/CustomersModule.kt
// ════════════════════════════════════════════════════════════════════════════

import com.matechmatrix.shopflowpos.feature.customers.data.repository.CustomersRepositoryImpl
import com.matechmatrix.shopflowpos.feature.customers.domain.repository.CustomersRepository
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.CollectCustomerDueUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.GetAllCustomersUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.GetCustomerByIdUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.GetCustomerSettingsUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.GetCustomersWithDuesUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.SaveCustomerUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.SearchCustomersUseCase
import com.matechmatrix.shopflowpos.feature.customers.domain.usecase.SoftDeleteCustomerUseCase
import com.matechmatrix.shopflowpos.feature.customers.presentation.CustomersViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val customersModule = module {

    // ── Repository ────────────────────────────────────────────────────────────
    single<CustomersRepository> { CustomersRepositoryImpl(db = get()) }

    // ── Use Cases ─────────────────────────────────────────────────────────────
    factory { GetAllCustomersUseCase(get()) }
    factory { SearchCustomersUseCase(get()) }
    factory { GetCustomerByIdUseCase(get()) }
    factory { GetCustomersWithDuesUseCase(get()) }
    factory { GetCustomerSettingsUseCase(get()) }
    factory { SaveCustomerUseCase(get()) }
    factory { SoftDeleteCustomerUseCase(get()) }
    factory { CollectCustomerDueUseCase(get()) }

    // ── ViewModel ─────────────────────────────────────────────────────────────
    viewModel {
        CustomersViewModel(
            getAllCustomers = get(),
            getSettings    = get(),
            saveCustomer   = get(),
            deleteCustomer = get(),
            collectDue     = get()
        )
    }
}


// ════════════════════════════════════════════════════════════════════════════
// feature/inventory/data/repository/InventoryRepositoryImpl.kt  (UPDATED PARTS)
// Only the changed sections shown — the rest of the file stays identical.
// ════════════════════════════════════════════════════════════════════════════

// ── Updated Supplier mapper (replaces the one in your existing impl) ──────────

/*
private fun mapSupplier(r: com.matechmatrix.shopflowpos.db.Supplier) = Supplier(
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
    updatedAt          = r.updated_at
)
*/

// ── Updated getAllSuppliers() implementation ───────────────────────────────────

/*
override suspend fun getAllSuppliers(): AppResult<List<Supplier>> =
    withContext(Dispatchers.Default) {
        runCatching {
            AppResult.Success(
                db.supplierQueries.getAllActiveSuppliers().executeAsList().map(::mapSupplier)
            )
        }.getOrElse { AppResult.Error(it.message ?: "Failed to load suppliers") }
    }
*/

// ── InventoryRepository interface — add supplier search ──────────────────────

/*
// Add to InventoryRepository interface:
suspend fun searchSuppliers(query: String): AppResult<List<Supplier>>
suspend fun getSupplierById(id: String): AppResult<Supplier>

// Implement in InventoryRepositoryImpl:
override suspend fun searchSuppliers(query: String): AppResult<List<Supplier>> =
    withContext(Dispatchers.Default) {
        runCatching {
            AppResult.Success(
                db.supplierQueries.searchSuppliers(query, query).executeAsList().map(::mapSupplier)
            )
        }.getOrElse { AppResult.Error(it.message ?: "Search failed") }
    }

override suspend fun getSupplierById(id: String): AppResult<Supplier> =
    withContext(Dispatchers.Default) {
        runCatching {
            val row = db.supplierQueries.getSupplierById(id).executeAsOneOrNull()
                ?: return@withContext AppResult.Error("Supplier not found")
            AppResult.Success(mapSupplier(row))
        }.getOrElse { AppResult.Error(it.message ?: "Failed to get supplier") }
    }
*/