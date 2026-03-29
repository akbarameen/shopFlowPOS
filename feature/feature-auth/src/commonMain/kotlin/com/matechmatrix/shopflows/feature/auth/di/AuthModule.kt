package com.matechmatrix.shopflows.feature.auth.di

import com.matechmatrix.shopflows.feature.auth.repository.AuthRepositoryImpl
import com.matechmatrix.shopflows.feature.auth.domain.repository.AuthRepository
import com.matechmatrix.shopflows.feature.auth.domain.usecase.ActivateLicenseUseCase
import com.matechmatrix.shopflows.feature.auth.domain.usecase.CheckSessionUseCase
import com.matechmatrix.shopflows.feature.auth.presentation.AuthViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { AuthRepositoryImpl(db = get(), api = get()) }
    factory { ActivateLicenseUseCase(get()) }
    factory { CheckSessionUseCase(get()) }
    viewModel { AuthViewModel(get(), get()) }
}