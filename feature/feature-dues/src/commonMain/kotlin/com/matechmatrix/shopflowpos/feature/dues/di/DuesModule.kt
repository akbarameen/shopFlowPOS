package com.matechmatrix.shopflowpos.feature.dues.di

import com.matechmatrix.shopflowpos.feature.dues.data.repository.DuesRepositoryImpl
import com.matechmatrix.shopflowpos.feature.dues.domain.repository.DuesRepository
import com.matechmatrix.shopflowpos.feature.dues.domain.usecase.*
import com.matechmatrix.shopflowpos.feature.dues.presentation.DuesViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val duesModule = module {
    single<DuesRepository> { DuesRepositoryImpl(db = get()) }
    factory { LoadDuesPageUseCase(get()) }
    factory { CollectSaleDueUseCase(get()) }
    factory { PayPurchaseDueFromDuesUseCase(get()) }
    viewModel { DuesViewModel(loadPage = get(), collectSaleDue = get(), payPurchaseDue = get()) }
}