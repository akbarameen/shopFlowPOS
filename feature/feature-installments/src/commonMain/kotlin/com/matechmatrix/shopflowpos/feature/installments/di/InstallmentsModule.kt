package com.matechmatrix.shopflowpos.feature.installments.di

import com.matechmatrix.shopflowpos.feature.installments.data.repository.InstallmentsRepositoryImpl
import com.matechmatrix.shopflowpos.feature.installments.domain.repository.InstallmentsRepository
import com.matechmatrix.shopflowpos.feature.installments.domain.usecase.*
import com.matechmatrix.shopflowpos.feature.installments.presentation.InstallmentsViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val installmentsModule = module {
    single<InstallmentsRepository> { InstallmentsRepositoryImpl(db = get()) }
    factory { GetAllPlansUseCase(get()) }
    factory { GetOverduePlansUseCase(get()) }
    factory { GetPlanPaymentsUseCase(get()) }
    factory { CreateInstallmentPlanUseCase(get()) }
    factory { RecordInstallmentPaymentUseCase(get()) }
    factory { GetInstallmentSettingsUseCase(get()) }
    viewModel {
        InstallmentsViewModel(
            getAllPlans     = get(),
            getSettings    = get(),
            createPlan     = get(),
            recordPayment  = get(),
            getPlanPayments = get()
        )
    }
}