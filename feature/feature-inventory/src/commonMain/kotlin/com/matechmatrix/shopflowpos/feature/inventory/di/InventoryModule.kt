package com.matechmatrix.shopflowpos.feature.inventory.di

import com.matechmatrix.shopflowpos.feature.inventory.data.repository.InventoryRepositoryImpl
import com.matechmatrix.shopflowpos.feature.inventory.domain.repository.InventoryRepository
import com.matechmatrix.shopflowpos.feature.inventory.presentation.InventoryViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val inventoryModule = module {
    single<InventoryRepository> { InventoryRepositoryImpl(db = get()) }
    viewModel { InventoryViewModel(get()) }
}