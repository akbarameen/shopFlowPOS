package com.matechmatrix.shopflowpos.feature.transactions.di

import com.matechmatrix.shopflowpos.feature.transactions.data.repository.TransactionsRepositoryImpl
import com.matechmatrix.shopflowpos.feature.transactions.domain.repository.TransactionsRepository
import com.matechmatrix.shopflowpos.feature.transactions.presentation.TransactionsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val transactionsModule = module {
    single<TransactionsRepository> { TransactionsRepositoryImpl(db = get()) }
    viewModel { TransactionsViewModel(get()) }
}