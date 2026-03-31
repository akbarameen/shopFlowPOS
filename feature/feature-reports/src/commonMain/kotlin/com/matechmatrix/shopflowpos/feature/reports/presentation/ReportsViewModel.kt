package com.matechmatrix.shopflowpos.feature.reports.presentation

import com.matechmatrix.shopflowpos.core.common.base.MviViewModel
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.usecase.LoadReportPageUseCase

class ReportsViewModel(
    private val loadReport: LoadReportPageUseCase
) : MviViewModel<ReportsState, ReportsIntent, ReportsEffect>(ReportsState()) {

    init { onIntent(ReportsIntent.Load) }

    override suspend fun handleIntent(intent: ReportsIntent) {
        when (intent) {
            ReportsIntent.Load           -> load(state.value.period)
            is ReportsIntent.SetPeriod   -> { setState { copy(period = intent.p) }; load(intent.p) }
        }
    }

    private suspend fun load(period: ReportPeriod) {
        setState { copy(isLoading = true, error = null) }
        val range = when (period) {
            ReportPeriod.TODAY -> DateTimeUtils.todayRange()
            ReportPeriod.WEEK  -> DateTimeUtils.thisWeekRange()
            ReportPeriod.MONTH -> DateTimeUtils.thisMonthRange()
            ReportPeriod.YEAR  -> DateTimeUtils.thisYearRange()
        }
        try {
            val data = loadReport(range.first, range.second)
            setState {
                copy(
                    isLoading    = false,
                    summary      = data.summary,
                    topProducts  = data.topProducts,
                    currencySymbol = data.currency,
                    shopName     = data.shopName
                )
            }
        } catch (e: Exception) {
            setState { copy(isLoading = false, error = e.message, summary = ReportSummary()) }
        }
    }
}
