package com.matechmatrix.shopflowpos.feature.pos.di

import com.matechmatrix.shopflowpos.feature.pos.data.repository.PosRepositoryImpl
import com.matechmatrix.shopflowpos.feature.pos.domain.repository.PosRepository
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.CompleteSaleUseCase
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.GetPosCustomersUseCase
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.GetPosProductsPagedUseCase
import com.matechmatrix.shopflowpos.feature.pos.domain.usecase.GetPosSettingsUseCase
import com.matechmatrix.shopflowpos.feature.pos.presentation.PosViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val posModule = module {

    // ── Repository ─────────────────────────────────────────────────────────────
    single<PosRepository> { PosRepositoryImpl(db = get()) }

    // ── Use Cases ──────────────────────────────────────────────────────────────
    factory { GetPosProductsPagedUseCase(get()) }
    factory { GetPosCustomersUseCase(get()) }
    factory { GetPosSettingsUseCase(get()) }
    factory { CompleteSaleUseCase(get()) }

    // ── ViewModel ──────────────────────────────────────────────────────────────
    viewModel {
        PosViewModel(
            getProductsPaged = get(),
            getCustomers     = get(),
            getSettings      = get(),
            completeSaleUC   = get()
        )
    }
}