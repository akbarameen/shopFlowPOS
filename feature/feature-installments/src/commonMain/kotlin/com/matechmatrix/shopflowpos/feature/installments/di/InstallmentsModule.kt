package com.matechmatrix.shopflowpos.feature.installments.di

import com.matechmatrix.shopflowpos.feature.installments.data.repository.InstallmentsRepositoryImpl
import com.matechmatrix.shopflowpos.feature.installments.domain.repository.InstallmentsRepository
import com.matechmatrix.shopflowpos.feature.installments.presentation.InstallmentsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val installmentsModule = module {
    single<InstallmentsRepository> { InstallmentsRepositoryImpl(db = get()) }
    viewModel { InstallmentsViewModel(get()) }
}