package com.matechmatrix.shopflowpos.feature.salesreturn.di

import com.matechmatrix.shopflowpos.feature.salesreturn.data.repository.SalesReturnRepositoryImpl
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.SalesReturnRepository
import com.matechmatrix.shopflowpos.feature.salesreturn.presentation.SalesReturnViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val salesReturnModule = module {
    single<SalesReturnRepository> { SalesReturnRepositoryImpl(db = get()) }
    viewModel { SalesReturnViewModel(get()) }
}