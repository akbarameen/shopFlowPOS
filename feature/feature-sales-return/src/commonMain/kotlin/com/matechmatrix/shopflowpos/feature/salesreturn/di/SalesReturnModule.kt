package com.matechmatrix.shopflowpos.feature.salesreturn.di

import com.matechmatrix.shopflowpos.feature.salesreturn.data.repository.SalesReturnRepositoryImpl
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.repository.SalesReturnRepository
import com.matechmatrix.shopflowpos.feature.salesreturn.domain.usecase.*
import com.matechmatrix.shopflowpos.feature.salesreturn.presentation.SalesReturnViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val salesReturnModule = module {
    single<SalesReturnRepository> { SalesReturnRepositoryImpl(db = get()) }
    factory { GetReturnsByDateRangeUseCase(get()) }
    factory { LookupSaleByInvoiceUseCase(get()) }
    factory { ProcessSaleReturnUseCase(get()) }
    factory { GetSalesReturnSettingsUseCase(get()) }
    viewModel {
        SalesReturnViewModel(
            getReturns    = get(),
            getSettings   = get(),
            lookupSale    = get(),
            processReturn = get()
        )
    }
}