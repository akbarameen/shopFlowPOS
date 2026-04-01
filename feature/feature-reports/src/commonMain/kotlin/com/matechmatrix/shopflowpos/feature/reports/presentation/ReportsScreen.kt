// ════════════════════════════════════════════════════════════════════════════
// feature/reports/presentation/ReportsScreen.kt
//
// ❓ Do you need a ReportsPagingSource? NO.
//    Reports are aggregate queries (SUM, COUNT, GROUP BY) — they return a
//    handful of rows, not a list to paginate. The only list is topProducts
//    which is capped at 10. Paging sources are for user-facing data lists
//    (inventory, customers, ledger history). Reports stay as single-shot
//    suspend queries.
// ════════════════════════════════════════════════════════════════════════════
package com.matechmatrix.shopflowpos.feature.reports.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.ReportSummary
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReportsScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : ReportsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val hPad = when (windowSize) {
        AppWindowSize.EXPANDED -> 28.dp
        AppWindowSize.MEDIUM   -> 20.dp
        AppWindowSize.COMPACT  -> 16.dp
    }

    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            .padding(horizontal = hPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
//        // ── Header ──────────────────────────────────────────────────────────
//        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
//            Column {
//                Text("Reports", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
//                Text(state.period.display, style = MaterialTheme.typography.bodySmall, color = TextMuted)
//            }
//        }

        // ── Period filter chips ──────────────────────────────────────────────
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReportPeriod.entries.forEach { p ->
                item(key = p.name) {
                    val selected = state.period == p
                    Surface(
                        onClick = { viewModel.onIntent(ReportsIntent.SetPeriod(p)) },
                        shape   = RoundedCornerShape(100.dp),
                        color   = if (selected) Primary else MaterialTheme.colorScheme.surface,
                        border  = if (selected) null else BorderStroke(1.5.dp, BorderColor)
                    ) {
                        Text(
                            p.display,
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold,
                            color = if (selected) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // ── Content ──────────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingView() }
            return@Column
        }

        when (windowSize) {
            AppWindowSize.COMPACT  -> ReportsCompact(state)
            AppWindowSize.MEDIUM   -> ReportsMedium(state)
            AppWindowSize.EXPANDED -> ReportsExpanded(state)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Layout variants
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReportsCompact(state: ReportsState) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
        item { PLHeroCard(state) }
        item { PaymentBreakdownCard(state) }
        item { ReturnsCard(state) }
        if (state.topProducts.isNotEmpty()) item { TopProductsCard(state) }
    }
}

@Composable
private fun ReportsMedium(state: ReportsState) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { PLHeroCard(state) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(Modifier.weight(1f), "Cash Sales",    fmt(state.summary.cashRevenue), Icons.Rounded.Payments,       Success)
                MetricCard(Modifier.weight(1f), "Bank / Transfer", fmt(state.summary.bankRevenue), Icons.Rounded.AccountBalance, Info)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(Modifier.weight(1f), "Expenses",   fmt(state.summary.expenses),  Icons.Rounded.Money,     Danger)
                MetricCard(Modifier.weight(1f), "Net Profit", fmt(state.summary.netProfit), Icons.Rounded.TrendingUp, if (state.summary.netProfit >= 0) Success else Danger)
            }
        }
        item { ReturnsCard(state) }
        if (state.topProducts.isNotEmpty()) item { TopProductsCard(state) }
    }
}

@Composable
private fun ReportsExpanded(state: ReportsState) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        // Left column
        LazyColumn(
            modifier            = Modifier.weight(0.55f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(bottom = 24.dp)
        ) {
            item { PLHeroCard(state) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(Modifier.weight(1f), "Revenue",      fmt(state.summary.revenue),     Icons.Rounded.TrendingUp,    Success)
                    MetricCard(Modifier.weight(1f), "Gross Profit", fmt(state.summary.grossProfit), Icons.Rounded.Analytics,     Primary)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(Modifier.weight(1f), "Expenses",  fmt(state.summary.expenses),     Icons.Rounded.Money,           Danger)
                    MetricCard(Modifier.weight(1f), "Dues",      fmt(state.summary.totalDues),    Icons.Rounded.AccountBalanceWallet, Warning)
                }
            }
            item { PaymentBreakdownCard(state) }
            item { ReturnsCard(state) }
        }

        // Right column — top products table
        Card(
            modifier  = Modifier.weight(0.45f).fillMaxHeight(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Top Products", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                if (state.topProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.BarChart, null, tint = TextFaint, modifier = Modifier.size(40.dp))
                            Text("No product data", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                } else {
                    // Table header
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("#",        Modifier.width(28.dp),  style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("Product",  Modifier.weight(1f),    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("Qty",      Modifier.width(40.dp),  style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("Revenue",  Modifier.width(80.dp),  style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextMuted)
                        Text("Profit",   Modifier.width(72.dp),  style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextMuted, textAlign = TextAlign.End)
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        val maxRevenue = state.topProducts.maxOfOrNull { it.revenue } ?: 1.0
                        itemsIndexed(state.topProducts, key = { _, p -> p.productId }) { idx, product ->
                            TopProductTableRow(product, maxRevenue, idx + 1, idx % 2 == 0)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// P&L Hero Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PLHeroCard(state: ReportsState) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Primary, PrimaryVariant))).padding(22.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Analytics, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
                Text(
                    "${state.shopName} · ${state.period.display}",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f)
                )
            }

            // Three KPIs
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PLItem("Revenue",      state.summary.revenue,     Color.White)
                PLItem("Gross Profit", state.summary.grossProfit, Color.White)
                PLItem("Expenses",     state.summary.expenses,    Color.White.copy(0.7f))
            }

            HorizontalDivider(color = Color.White.copy(0.2f))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Net Profit", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
                    Text(
                        fmt(state.summary.netProfit),
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold,
                        color = if (state.summary.netProfit >= 0) Color(0xFF00F5A0) else Color(0xFFFF6B6B)
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(shape = RoundedCornerShape(100.dp), color = Color.White.copy(0.15f)) {
                        Text(
                            "${state.summary.salesCount} sales",
                            Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall, color = Color.White
                        )
                    }
                    // Profit margin badge
                    val margin = state.summary.profitMargin
                    Surface(shape = RoundedCornerShape(100.dp), color = Color.White.copy(0.15f)) {
                        Text(
                            "${margin.toInt()}% margin",
                            Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall, color = Color.White
                        )
                    }
                    if (state.summary.totalDues > 0) {
                        Text(
                            "Dues: ${fmt(state.summary.totalDues)}",
                            style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PLItem(label: String, value: Double, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(fmtCompact(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Metric Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MetricCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = color)
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment Breakdown Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PaymentBreakdownCard(state: ReportsState) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Payment Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            PaymentRow("Cash Sales",     state.summary.cashRevenue, Icons.Rounded.Payments,       Success)
            PaymentRow("Bank / Transfer",state.summary.bankRevenue, Icons.Rounded.AccountBalance, Info)

            val total = state.summary.cashRevenue + state.summary.bankRevenue
            if (total > 0) {
                HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                val cashPct = (state.summary.cashRevenue / total).toFloat().coerceIn(0.01f, 0.99f)
                Row(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))) {
                    Box(Modifier.weight(cashPct).fillMaxHeight().background(Success))
                    Box(Modifier.weight(1f - cashPct).fillMaxHeight().background(Info))
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    LegendDot(Success, "${(cashPct * 100).toInt()}% Cash")
                    LegendDot(Info,    "${((1f - cashPct) * 100).toInt()}% Bank")
                }
            }
        }
    }
}

@Composable
private fun PaymentRow(label: String, amount: Double, icon: ImageVector, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.12f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
        }
        Text(fmt(amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Returns Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReturnsCard(state: ReportsState) {
    if (state.summary.returnCount == 0L) return
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = WarningContainer), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(38.dp).clip(RoundedCornerShape(11.dp)).background(Warning.copy(0.15f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.AssignmentReturn, null, tint = Warning, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text("Returns / Refunds", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Warning)
                    Text("${state.summary.returnCount} return${if (state.summary.returnCount != 1L) "s" else ""}", style = MaterialTheme.typography.labelSmall, color = Warning.copy(0.7f))
                }
            }
            Text("-${fmt(state.summary.returnsAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Danger)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Top Products Card (compact/medium)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TopProductsCard(state: ReportsState) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Top Products", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            val maxRevenue = state.topProducts.maxOfOrNull { it.revenue } ?: 1.0
            state.topProducts.forEachIndexed { idx, product ->
                TopProductRow(product, maxRevenue, idx + 1)
                if (idx < state.topProducts.lastIndex) HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
private fun TopProductRow(product: TopProduct, maxRevenue: Double, rank: Int) {
    val progress = (product.revenue / maxRevenue.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer), contentAlignment = Alignment.Center) {
            Text("$rank", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(product.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("×${product.qty}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)), color = Primary, trackColor = PrimaryContainer)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(fmtCompact(product.revenue), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
            Text("GP: ${fmtCompact(product.grossProfit)}", style = MaterialTheme.typography.labelSmall, color = if (product.grossProfit >= 0) Success else Danger)
        }
    }
}

// Desktop table row
@Composable
private fun TopProductTableRow(product: TopProduct, maxRevenue: Double, rank: Int, isEven: Boolean) {
    val progress = (product.revenue / maxRevenue.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    Row(
        Modifier.fillMaxWidth()
            .background(if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(0.35f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("$rank", Modifier.width(28.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(product.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = Primary, trackColor = PrimaryContainer)
        }
        Text("×${product.qty}", Modifier.width(40.dp), style = MaterialTheme.typography.labelSmall, color = TextMuted, textAlign = TextAlign.Center)
        Text(fmtCompact(product.revenue), Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
        Text(fmtCompact(product.grossProfit), Modifier.width(72.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (product.grossProfit >= 0) Success else Danger, textAlign = TextAlign.End)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Local formatters (keep off CurrencyFormatter to avoid coupling)
// ─────────────────────────────────────────────────────────────────────────────
private fun fmt(v: Double)        =  CurrencyFormatter.formatRs(v)
private fun fmtCompact(v: Double) = CurrencyFormatter.formatCompact(v)