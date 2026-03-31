package com.matechmatrix.shopflowpos.feature.expenses.di

import com.matechmatrix.shopflowpos.feature.expenses.data.repository.ExpensesRepositoryImpl
import com.matechmatrix.shopflowpos.feature.expenses.domain.repository.ExpensesRepository
import com.matechmatrix.shopflowpos.feature.expenses.presentation.ExpensesViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val expensesModule = module {
    single<ExpensesRepository> { ExpensesRepositoryImpl(db = get()) }
    viewModel { ExpensesViewModel(repo = get()) }
}