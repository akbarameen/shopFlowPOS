package com.matechmatrix.shopflowpos.feature.dashboard.di

import com.matechmatrix.shopflowpos.feature.dashboard.data.repository.DashboardRepositoryImpl
import com.matechmatrix.shopflowpos.feature.dashboard.domain.repository.DashboardRepository
import com.matechmatrix.shopflowpos.feature.dashboard.domain.usecase.GetDashboardStatsUseCase
import com.matechmatrix.shopflowpos.feature.dashboard.presentation.DashboardViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dashboardModule = module {
    single<DashboardRepository> { DashboardRepositoryImpl(db = get()) }
    factory { GetDashboardStatsUseCase(get()) }
    viewModel { DashboardViewModel(get()) }
}