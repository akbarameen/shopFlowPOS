import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct

enum class ReportPeriod { TODAY, WEEK, MONTH, YEAR }

data class ReportsState(
    val isLoading: Boolean = true,
    val period: ReportPeriod = ReportPeriod.TODAY,
    val summary: ReportSummary = ReportSummary(),
    val topProducts: List<TopProduct> = emptyList(),
    val currencySymbol: String = "Rs.",
    val shopName: String = "",
    val error: String? = null
)

sealed class ReportsIntent {
    data object Load : ReportsIntent()
    data class SetPeriod(val p: ReportPeriod) : ReportsIntent()
}

sealed class ReportsEffect

