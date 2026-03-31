package com.matechmatrix.shopflowpos.feature.reports.di

import com.matechmatrix.shopflowpos.feature.reports.data.repository.ReportsRepositoryImpl
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportsRepository
import com.matechmatrix.shopflowpos.feature.reports.domain.usecase.GetReportUseCase
import com.matechmatrix.shopflowpos.feature.reports.domain.usecase.GetTopProductsUseCase
import com.matechmatrix.shopflowpos.feature.reports.domain.usecase.LoadReportPageUseCase
import com.matechmatrix.shopflowpos.feature.reports.presentation.ReportsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val reportsModule = module {
    single<ReportsRepository> { ReportsRepositoryImpl(db = get()) }
    factory { GetReportUseCase(get()) }
    factory { GetTopProductsUseCase(get()) }
    factory { LoadReportPageUseCase(get()) }
    viewModel { ReportsViewModel(loadReport = get()) }
}