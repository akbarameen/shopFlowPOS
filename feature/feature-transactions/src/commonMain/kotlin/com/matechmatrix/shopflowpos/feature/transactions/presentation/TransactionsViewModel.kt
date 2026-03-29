package com.matechmatrix.shopflowpos.feature.transactions.presentation

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.feature.transactions.domain.repository.TransactionsRepository
import kotlin.time.Clock

class TransactionsViewModel(
    private val repo: TransactionsRepository
) : MviViewModel<TransactionsState, TransactionsIntent, TransactionsEffect>(TransactionsState()) {

    init {
        onIntent(TransactionsIntent.Load)
    }

    override suspend fun handleIntent(intent: TransactionsIntent) {
        when (intent) {
            TransactionsIntent.Load -> loadSales(state.value.dateFilter)
            is TransactionsIntent.SetFilter -> {
                setState { copy(dateFilter = intent.filter) }
                loadSales(intent.filter)
            }
        }
    }

    private suspend fun loadSales(filter: TxDateFilter) {
        setState { copy(isLoading = true) }
        val currency = repo.getCurrencySymbol()
        val now = Clock.System.now().toEpochMilliseconds()
        val range = when (filter) {
            TxDateFilter.TODAY -> DateTimeUtils.todayRange()
            TxDateFilter.WEEK  -> DateTimeUtils.thisWeekRange()
            TxDateFilter.MONTH -> DateTimeUtils.thisMonthRange()
            TxDateFilter.ALL   -> 0L to now
        }
        val start = range.first
        val end = range.second

        when (val r = repo.getSalesByDateRange(start, end)) {
            is AppResult.Success -> {
                val sorted = r.data.sortedByDescending { it.soldAt }
                setState {
                    copy(
                        isLoading = false, sales = sorted,
                        totalRevenue = sorted.sumOf { it.totalAmount },
                        totalCount = sorted.size,
                        currencySymbol = currency
                    )
                }
            }
            is AppResult.Error -> setState { copy(isLoading = false, error = r.message) }
            else -> setState { copy(isLoading = false) }
        }
    }
}
