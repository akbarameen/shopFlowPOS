package com.matechmatrix.shopflowpos.feature.reports.presentation

import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct

enum class ReportPeriod(val display: String) {
    TODAY("Today"), WEEK("This Week"), MONTH("This Month"), YEAR("This Year")
}

data class ReportsState(
    val isLoading      : Boolean       = true,
    val period         : ReportPeriod  = ReportPeriod.TODAY,
    val summary        : ReportSummary = ReportSummary(),
    val topProducts    : List<TopProduct> = emptyList(),
    val currencySymbol : String        = "Rs.",
    val shopName       : String        = "",
    val error          : String?       = null
)

sealed class ReportsIntent {
    data object Load                       : ReportsIntent()
    data class  SetPeriod(val p: ReportPeriod) : ReportsIntent()
}

sealed class ReportsEffect