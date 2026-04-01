package com.matechmatrix.shopflowpos.feature.purchase.di

import com.matechmatrix.shopflowpos.feature.purchase.data.repository.PurchaseRepositoryImpl
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseRepository
import com.matechmatrix.shopflowpos.feature.purchase.domain.usecase.*
import com.matechmatrix.shopflowpos.feature.purchase.presentation.PurchaseViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val purchaseModule = module {
    single<PurchaseRepository> { PurchaseRepositoryImpl(db = get()) }
    factory { GetPurchaseSettingsUseCase(get()) }
    factory { CreatePurchaseOrderUseCase(get()) }
    factory { PayPurchaseDueUseCase(get()) }
    viewModel {
        PurchaseViewModel(
            getSettings = get(),
            createOrderUC = get(),
            payDueUC = get(),
            repo = get()
        )
    }
}