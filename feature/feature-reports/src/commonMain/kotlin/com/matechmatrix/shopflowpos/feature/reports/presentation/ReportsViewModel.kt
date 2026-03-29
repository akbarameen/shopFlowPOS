package com.matechmatrix.shopflowpos.feature.reports.presentation

import ReportPeriod
import ReportsEffect
import ReportsIntent
import ReportsState
import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportsRepository
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct
import kotlinx.datetime.Clock


class ReportsViewModel(private val repo: ReportsRepository) :
    MviViewModel<ReportsState, ReportsIntent, ReportsEffect>(ReportsState()) {

    init { onIntent(ReportsIntent.Load) }

    override suspend fun handleIntent(intent: ReportsIntent) {
        when (intent) {
            ReportsIntent.Load -> load(state.value.period)
            is ReportsIntent.SetPeriod -> { setState { copy(period = intent.p) }; load(intent.p) }
        }
    }

    private suspend fun load(period: ReportPeriod) {
        setState { copy(isLoading = true) }

        val (start, end) = when (period) {
            ReportPeriod.TODAY -> DateTimeUtils.todayRange()
            ReportPeriod.WEEK  -> DateTimeUtils.thisWeekRange()
            ReportPeriod.MONTH -> DateTimeUtils.thisMonthRange()
            ReportPeriod.YEAR  -> DateTimeUtils.thisYearRange()
        }

        val reportResult = repo.getReport(start, end)
        val productsResult = repo.getTopProducts(start, end, 5)
        val currency = repo.getCurrencySymbol()
        val shop = repo.getShopName()

        setState {
            copy(
                isLoading = false,
                summary = if (reportResult is AppResult.Success) reportResult.data else ReportSummary(),
                topProducts = if (productsResult is AppResult.Success) productsResult.data else emptyList(),
                currencySymbol = currency,
                shopName = shop
            )
        }
    }
}