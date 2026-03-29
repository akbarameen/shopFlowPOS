package com.matechmatrix.shopflowpos.feature.customers.di

import com.matechmatrix.shopflowpos.feature.customers.data.repository.CustomersRepositoryImpl
import com.matechmatrix.shopflowpos.feature.customers.domain.repository.CustomersRepository
import com.matechmatrix.shopflowpos.feature.customers.presentation.CustomersViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val customersModule = module {
    single<CustomersRepository> { CustomersRepositoryImpl(db = get()) }
    viewModel { CustomersViewModel(get()) }
}