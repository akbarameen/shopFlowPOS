package com.matechmatrix.shopflowpos.feature.repairs.di

import com.matechmatrix.shopflowpos.feature.repairs.data.repository.RepairsRepositoryImpl
import com.matechmatrix.shopflowpos.feature.repairs.domain.repository.RepairsRepository
import com.matechmatrix.shopflowpos.feature.repairs.presentation.RepairsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repairsModule = module {
    single<RepairsRepository> { RepairsRepositoryImpl(db = get()) }
    viewModel { RepairsViewModel(get()) }
}