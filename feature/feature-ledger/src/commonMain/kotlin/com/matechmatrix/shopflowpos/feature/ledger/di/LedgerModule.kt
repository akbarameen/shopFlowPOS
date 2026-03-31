package com.matechmatrix.shopflowpos.feature.ledger.di

import com.matechmatrix.shopflowpos.feature.ledger.data.repository.LedgerRepositoryImpl
import com.matechmatrix.shopflowpos.feature.ledger.domain.repository.LedgerRepository
import com.matechmatrix.shopflowpos.feature.ledger.domain.usecase.*
import com.matechmatrix.shopflowpos.feature.ledger.presentation.LedgerViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val ledgerModule = module {
    single<LedgerRepository> { LedgerRepositoryImpl(db = get()) }
    factory { GetLedgerOverviewUseCase(get()) }
    factory { AdjustCashBalanceUseCase(get()) }
    factory { AdjustBankBalanceUseCase(get()) }
    factory { SaveBankAccountUseCase(get()) }
    factory { TransferBetweenAccountsUseCase(get()) }
    viewModel {
        LedgerViewModel(
            getOverview   = get(),
            adjustCash    = get(),
            adjustBank    = get(),
            saveBankAccUC = get(),
            transferUC    = get(),
            repo          = get()
        )
    }
}