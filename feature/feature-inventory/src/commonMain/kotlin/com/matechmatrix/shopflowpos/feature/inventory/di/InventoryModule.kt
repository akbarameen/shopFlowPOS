package com.matechmatrix.shopflowpos.feature.inventory.di

import com.matechmatrix.shopflowpos.feature.inventory.data.repository.InventoryRepositoryImpl
import com.matechmatrix.shopflowpos.feature.inventory.domain.repository.InventoryRepository
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.DeleteProductUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.GetInventorySettingsUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.GetLowStockProductsUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.GetPagedProductsUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.SaveProductUseCase
import com.matechmatrix.shopflowpos.feature.inventory.domain.usecase.UpdateStockUseCase
import com.matechmatrix.shopflowpos.feature.inventory.presentation.InventoryViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val inventoryModule = module {

    // ── Repository ────────────────────────────────────────────────────────────
    single<InventoryRepository> { InventoryRepositoryImpl(db = get()) }

    // ── Use cases ─────────────────────────────────────────────────────────────
    factory { GetPagedProductsUseCase(get()) }
    factory { GetLowStockProductsUseCase(get()) }
    factory { GetInventorySettingsUseCase(get()) }
    factory { SaveProductUseCase(get()) }
    factory { UpdateStockUseCase(get()) }
    factory { DeleteProductUseCase(get()) }

    // ── ViewModel ─────────────────────────────────────────────────────────────
    viewModel {
        InventoryViewModel(
            getPagedProducts     = get(),
            getLowStock          = get(),
            getSettings          = get(),
            saveProduct          = get(),
            updateStockUseCase   = get(),
            deleteProductUseCase = get(),
        )
    }
}