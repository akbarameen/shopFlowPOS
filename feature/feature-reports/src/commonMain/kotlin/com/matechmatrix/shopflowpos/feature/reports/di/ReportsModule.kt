package com.matechmatrix.shopflowpos.feature.reports.di

import com.matechmatrix.shopflowpos.feature.reports.data.repository.ReportsRepositoryImpl
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportsRepository
import com.matechmatrix.shopflowpos.feature.reports.presentation.ReportsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val reportsModule = module {
    single<ReportsRepository> { ReportsRepositoryImpl(db = get()) }
    viewModel { ReportsViewModel(get()) }
}