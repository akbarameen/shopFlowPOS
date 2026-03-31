package com.matechmatrix.shopflowpos.feature.reports.domain.usecase

import com.matechmatrix.shopflowpos.core.common.result.AppResult
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportsRepository
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct

class GetReportUseCase(private val repo: ReportsRepository) {
    suspend operator fun invoke(startMs: Long, endMs: Long): AppResult<ReportSummary> =
        repo.getReport(startMs, endMs)
}

class GetTopProductsUseCase(private val repo: ReportsRepository) {
    suspend operator fun invoke(startMs: Long, endMs: Long, limit: Int = 10): AppResult<List<TopProduct>> =
        repo.getTopProducts(startMs, endMs, limit)
}

data class ReportPageData(
    val summary     : ReportSummary,
    val topProducts : List<TopProduct>,
    val currency    : String,
    val shopName    : String
)

class LoadReportPageUseCase(private val repo: ReportsRepository) {
    suspend operator fun invoke(startMs: Long, endMs: Long): ReportPageData {
        val summary     = (repo.getReport(startMs, endMs) as? AppResult.Success)?.data ?: ReportSummary()
        val topProducts = (repo.getTopProducts(startMs, endMs, 10) as? AppResult.Success)?.data ?: emptyList()
        return ReportPageData(summary, topProducts, repo.getCurrencySymbol(), repo.getShopName())
    }
}