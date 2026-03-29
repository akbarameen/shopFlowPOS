package com.matechmatrix.shopflowpos.feature.pos.di

import com.matechmatrix.shopflowpos.feature.pos.data.repository.PosRepositoryImpl
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosRepository
import com.matechmatrix.shopflowpos.feature.pos.presentation.PosViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val posModule = module {
    single<PosRepository> { PosRepositoryImpl(db = get()) }
    viewModel { PosViewModel(get()) }
}