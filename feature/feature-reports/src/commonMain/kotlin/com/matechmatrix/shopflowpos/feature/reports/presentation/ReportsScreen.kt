package com.matechmatrix.shopflowpos.feature.reports.presentation

import ReportsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.feature.reports.domain.repository.TopProduct
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
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
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = hPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Period filter chips ──────────────────────────────────────────────
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ReportPeriod.values().toList()) { p ->
                val selected = state.period == p
                Surface(
                    onClick = { viewModel.onIntent(ReportsIntent.SetPeriod(p)) },
                    shape  = RoundedCornerShape(100.dp),
                    color  = if (selected) Primary else MaterialTheme.colorScheme.surface,
                    border = if (selected) null
                    else BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        p.name.lowercase().replaceFirstChar { it.uppercase() },
                        Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Column
        }

        when (windowSize) {
            AppWindowSize.COMPACT  -> ReportsCompact(state)
            AppWindowSize.MEDIUM   -> ReportsMedium(state)
            AppWindowSize.EXPANDED -> ReportsExpanded(state)
        }
    }
}

// ── COMPACT — stacked cards ───────────────────────────────────────────────────
@Composable
private fun ReportsCompact(state: ReportsState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding      = PaddingValues(bottom = 88.dp)
    ) {
        item { PLHeroCard(state, Modifier.fillMaxWidth()) }
        item { PaymentBreakdownCard(state) }
        if (state.topProducts.isNotEmpty()) {
            item { TopProductsCard(state) }
        }
    }
}

// ── MEDIUM — 2-column metric grid + full-width detailed cards ─────────────────
@Composable
private fun ReportsMedium(state: ReportsState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding      = PaddingValues(bottom = 24.dp)
    ) {
        item { PLHeroCard(state, Modifier.fillMaxWidth()) }

        // 2-column metric cards
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier    = Modifier.weight(1f),
                    label       = "Cash Sales",
                    value       = CurrencyFormatter.formatRs(state.summary.cashRevenue),
                    icon        = Icons.Rounded.Payments,
                    color       = Success
                )
                MetricCard(
                    modifier    = Modifier.weight(1f),
                    label       = "Bank / Transfer",
                    value       = CurrencyFormatter.formatRs(state.summary.bankRevenue),
                    icon        = Icons.Rounded.AccountBalance,
                    color       = Info
                )
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label    = "Total Expenses",
                    value    = CurrencyFormatter.formatRs(state.summary.expenses),
                    icon     = Icons.Rounded.Money,
                    color    = Danger
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    label    = "Net Profit",
                    value    = CurrencyFormatter.formatRs(state.summary.netProfit),
                    icon     = Icons.Rounded.TrendingUp,
                    color    = if (state.summary.netProfit >= 0) Success else Danger
                )
            }
        }
        if (state.topProducts.isNotEmpty()) {
            item { TopProductsCard(state) }
        }
    }
}

// ── EXPANDED — desktop dashboard ──────────────────────────────────────────────
@Composable
private fun ReportsExpanded(state: ReportsState) {
    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left column — KPIs + payment split
        LazyColumn(
            modifier            = Modifier.weight(0.55f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(bottom = 24.dp)
        ) {
            item { PLHeroCard(state, Modifier.fillMaxWidth()) }
            // 2-col KPI grid
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(Modifier.weight(1f), "Revenue", CurrencyFormatter.formatRs(state.summary.revenue), Icons.Rounded.TrendingUp, Success)
                    MetricCard(Modifier.weight(1f), "Gross Profit", CurrencyFormatter.formatRs(state.summary.grossProfit), Icons.Rounded.Analytics, Primary)
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(Modifier.weight(1f), "Expenses", CurrencyFormatter.formatRs(state.summary.expenses), Icons.Rounded.Money, Danger)
                    MetricCard(Modifier.weight(1f), "Returns", CurrencyFormatter.formatRs(state.summary.returnsAmount), Icons.Rounded.AssignmentReturn, Warning)
                }
            }
            item { PaymentBreakdownCard(state) }
        }

        // Right column — Top products table
        Card(
            modifier  = Modifier.weight(0.45f).fillMaxHeight(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Top Products", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

                if (state.topProducts.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No product data", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Table header
                    Row(Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("#", Modifier.width(28.dp), style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Product", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Qty", Modifier.width(40.dp), style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Revenue", Modifier.width(80.dp), style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        val maxRevenue = state.topProducts.maxOfOrNull { it.revenue } ?: 1L
                        itemsIndexed(state.topProducts) { idx, product ->
                            TopProductTableRow(product, maxRevenue, idx + 1, idx % 2 == 0)
                        }
                    }
                }
            }
        }
    }
}

// ── P&L Hero card ─────────────────────────────────────────────────────────────
@Composable
private fun PLHeroCard(state: ReportsState, modifier: Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Primary, PrimaryVariant)))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Analytics, null, tint = Color.White.copy(0.7f),
                    modifier = Modifier.size(16.dp))
                Text("${state.shopName} — Financial Report",
                    style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PLItem("Revenue",      state.summary.revenue,     Color.White)
                PLItem("Gross Profit", state.summary.grossProfit, Color.White)
                PLItem("Expenses",     state.summary.expenses,    Color.White.copy(0.7f))
            }
            HorizontalDivider(color = Color.White.copy(0.2f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Net Profit", style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(0.7f))
                    Text(
                        CurrencyFormatter.formatRs(state.summary.netProfit),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color      = if (state.summary.netProfit >= 0) Color(0xFF00F5A0) else Color(0xFFFF6B6B)
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(100.dp), color = Color.White.copy(0.15f)) {
                        Text("${state.summary.salesCount} sales",
                            Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                    if (state.summary.returnsAmount > 0) {
                        Text("${CurrencyFormatter.formatRs(state.summary.returnsAmount)} returned",
                            style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.5f))
                    }
                }
            }
        }
    }
}

// ── Metric card ───────────────────────────────────────────────────────────────
@Composable
private fun MetricCard(
    modifier: Modifier, label: String, value: String,
    icon: ImageVector, color: Color
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = color, modifier = Modifier.size(20.dp)) }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(value, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold, color = color)
                Text(label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ── Payment breakdown card ────────────────────────────────────────────────────
@Composable
private fun PaymentBreakdownCard(state: ReportsState) {
    Card(
        Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Payment Breakdown", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

            PaymentRow("Cash Sales",    state.summary.cashRevenue, Icons.Rounded.Payments,       Success)
            PaymentRow("Bank/Transfer", state.summary.bankRevenue, Icons.Rounded.AccountBalance, Info)

            val total = state.summary.cashRevenue + state.summary.bankRevenue
            if (total > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                val cashPct = (state.summary.cashRevenue.toFloat() / total).coerceIn(0.01f, 0.99f)
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
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PaymentRow(label: String, amount: Long, icon: ImageVector, color: Color) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(color.copy(0.12f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
            }
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(CurrencyFormatter.formatRs(amount), style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold, color = color)
    }
}

// ── Top products card ─────────────────────────────────────────────────────────
@Composable
private fun TopProductsCard(state: ReportsState) {
    Card(
        Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Top Products", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            val maxRevenue = state.topProducts.maxOfOrNull { it.revenue } ?: 1L
            state.topProducts.forEachIndexed { idx, product ->
                TopProductRow(product, maxRevenue, idx + 1)
                if (idx < state.topProducts.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                }
            }
        }
    }
}

// ── Top product row (cards) ───────────────────────────────────────────────────
@Composable
private fun TopProductRow(product: TopProduct, maxRevenue: Long, rank: Int) {
    val progress = if (maxRevenue > 0) (product.revenue.toFloat() / maxRevenue).coerceIn(0f, 1f) else 0f
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) { Text("$rank", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(product.name, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text("×${product.qty}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            LinearProgressIndicator(
                progress  = { progress },
                modifier  = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color     = Primary,
                trackColor = PrimaryContainer
            )
        }
        Text(CurrencyFormatter.formatCompact(product.revenue.toDouble()),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
    }
}

// ── Top product table row (desktop right panel) ───────────────────────────────
@Composable
private fun TopProductTableRow(product: TopProduct, maxRevenue: Long, rank: Int, isEven: Boolean) {
    val progress = if (maxRevenue > 0) (product.revenue.toFloat() / maxRevenue).coerceIn(0f, 1f) else 0f
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (isEven) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("$rank", Modifier.width(28.dp), style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, color = Primary)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(product.name, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            LinearProgressIndicator(
                progress  = { progress },
                modifier  = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                color     = Primary,
                trackColor = PrimaryContainer
            )
        }
        Text("×${product.qty}", Modifier.width(40.dp), style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(CurrencyFormatter.formatCompact(product.revenue.toDouble()), Modifier.width(80.dp),
            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Primary)
    }
}

@Composable
private fun PLItem(label: String, value: Long, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(CurrencyFormatter.formatCompact(value.toDouble()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.6f))
    }
}