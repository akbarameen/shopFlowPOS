package com.matechmatrix.shopflowpos.feature.suppliers.di

import com.matechmatrix.shopflowpos.feature.suppliers.data.repository.SuppliersRepositoryImpl
import com.matechmatrix.shopflowpos.feature.suppliers.domain.repository.SuppliersRepository
import com.matechmatrix.shopflowpos.feature.suppliers.presentation.SuppliersViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val suppliersModule = module {
    single<SuppliersRepository> { SuppliersRepositoryImpl(db = get()) }
    viewModel { SuppliersViewModel(get()) }
}