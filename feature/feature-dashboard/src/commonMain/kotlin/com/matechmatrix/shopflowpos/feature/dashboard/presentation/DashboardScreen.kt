package com.matechmatrix.shopflowpos.feature.dashboard.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

private data class QuickActionData(val emoji: String, val label: String, val bg: Color)
private val quickActions = listOf(
    QuickActionData("🛒", "New Sale",    PrimaryContainer),
    QuickActionData("📦", "Add Stock",   SuccessContainer),
    QuickActionData("💸", "Expense",     WarningContainer),
    QuickActionData("📱", "Buy Phone",   AccentContainer),
    QuickActionData("💳", "Collect Due", DangerContainer),
    QuickActionData("👤", "Customer",    InfoContainer),
    QuickActionData("🔧", "Repair",      PurpleContainer),
    QuickActionData("📊", "Reports",     PurpleContainer),
)

@Composable
fun DashboardScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : DashboardViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                DashboardEffect.GoToPOS          -> navigateChild("pos")
                DashboardEffect.GoToInventory    -> navigateChild("inventory")
                DashboardEffect.GoToTransactions -> navigateChild("transactions")
                DashboardEffect.GoToLedger       -> navigateChild("ledger")
            }
        }
    }

    val cols       = when (windowSize) { AppWindowSize.COMPACT -> 2; AppWindowSize.MEDIUM -> 3; else -> 4 }
    val isExpanded = windowSize == AppWindowSize.EXPANDED

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
            return@Box
        }
        if (isExpanded) {
            Row(Modifier.fillMaxSize().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                LazyColumn(Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { DashGreetingHeader(state, viewModel) }
                    item { DashStatsSection(state, cols = 4) }
                    item { DashRevenueCharts(state) }
                    item { DashRecentSalesCard(state) }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { DashInventoryValueCard(state) }
                    item { DashQuickActions(Modifier.fillMaxWidth(), viewModel) }
                    item { DashQuickStatsCard(state) }
                    item { DashActivityCard(state) }
                    item { DashLowStockCard(state) }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { DashGreetingHeader(state, viewModel) }
                item { DashStatsSection(state, cols) }
                item { DashInventoryValueCard(state) }
                item { DashRevenueCharts(state) }
                item { DashQuickActions(Modifier.fillMaxWidth(), viewModel) }
                item { DashRecentSalesCard(state) }
                item { DashQuickStatsCard(state) }
                item { DashLowStockCard(state) }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun DashRevenueCharts(state: DashboardState) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Weekly", "Monthly", "Yearly")

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Analytics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                // Simple Tab Selector
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(InputBgLight)
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedTab == index) Primary else Color.Transparent)
                                .clickable { selectedTab = index }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) Color.White else TextMuted
                            )
                        }
                    }
                }
            }

            val chartData = when (selectedTab) {
                0 -> state.weeklyRevenue
                1 -> state.monthlyRevenue
                else -> state.yearlyRevenue
            }

            if (chartData.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.BarChart, null, tint = TextFaint, modifier = Modifier.size(40.dp))
                        Text("No data for this period", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            } else {
                RevenueBarChart(
                    data = chartData,
                    barColor = when(selectedTab) {
                        0 -> Primary
                        1 -> Success
                        else -> Info
                    },
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
        }
    }
}

@Composable
private fun RevenueBarChart(
    data: Map<String, Double>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val maxVal = data.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    val sortedKeys = data.keys.sorted()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        sortedKeys.forEach { key ->
            val value = data[key] ?: 0.0
            val ratio = (value / maxVal).toFloat().coerceIn(0.05f, 1f)

            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Value label on top of bar
                if (value > 0) {
                    Text(
                        text = if (value >= 1000) "${(value/1000).toInt()}k" else value.toInt().toString(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = barColor,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(ratio)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(barColor, barColor.copy(0.4f))
                            )
                        )
                )
                
                Spacer(Modifier.height(6.dp))
                
                Text(
                    text = when {
                        key.contains("-") -> key.split("-").last()
                        else -> key.takeLast(2)
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DashGreetingHeader(state: DashboardState, viewModel: DashboardViewModel) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Good Morning 👋",
                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.4).sp, color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                state.shopName.ifBlank { "Today's Overview" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(InputBgLight)
                    .clickable { viewModel.onIntent(DashboardIntent.ToggleAnalyticsVisibility) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (state.analyticsVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Button(
                onClick = { viewModel.onIntent(DashboardIntent.NavigateToPOS) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("🛒 New Sale", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun DashInventoryValueCard(state: DashboardState) {
    fun fmtRs(v: Double) = if (state.analyticsVisible) CurrencyFormatter.formatRs(v) else "••••"
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Stock Valuation", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(SuccessContainer).padding(12.dp)
                ) {
                    Column {
                        Text("Total Investment", style = MaterialTheme.typography.labelSmall, color = Success.copy(0.8f))
                        Text(fmtRs(state.totalInventoryValue), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Success)
                    }
                }
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(InfoContainer).padding(12.dp)
                ) {
                    Column {
                        Text("Expected Revenue", style = MaterialTheme.typography.labelSmall, color = Info.copy(0.8f))
                        Text(fmtRs(state.totalInventorySellingValue), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Info)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashStatsSection(state: DashboardState, cols: Int) {
    fun fmt(v: Double)   = if (state.analyticsVisible) CurrencyFormatter.format(v)   else "••••"
    fun fmtRs(v: Double) = if (state.analyticsVisible) CurrencyFormatter.formatRs(v) else "••••"

    val cards = listOf(
        StatCardData("💰", fmt(state.todayRevenue),     "TOTAL SALES",    "↑ ${state.todaySalesCount} orders",  true,  isPrimary = true),
        StatCardData("📈", fmt(state.todayGrossProfit), "GROSS PROFIT",   "↑ Today's margin",                  true,  iconBg = SuccessContainer),
        StatCardData("💸", fmt(state.todayExpenses),    "EXPENSES",       "↓ Today",                           false, iconBg = DangerContainer),
        StatCardData("🏆", fmt(state.todayNetProfit),   "NET PROFIT",     if (state.todayNetProfit >= 0) "↑ After expenses" else "↓ Loss today", state.todayNetProfit >= 0, iconBg = WarningContainer),
        StatCardData("💵", fmtRs(state.cashBalance),    "CASH IN HAND",   "↑ Available",                       true,  iconBg = SuccessContainer),
        StatCardData("🏦", fmtRs(state.bankBalance),    "BANK BALANCE",   "↑ In account",                      true,  iconBg = InfoContainer),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cards.chunked(cols).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { card -> StatCard(Modifier.weight(1f), card) }
                repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private data class StatCardData(
    val emoji: String, val value: String, val label: String,
    val change: String, val changePositive: Boolean,
    val isPrimary: Boolean = false, val iconBg: Color = PrimaryContainer
)

@Composable
private fun StatCard(modifier: Modifier = Modifier, card: StatCardData) {
    if (card.isPrimary) {
        Box(
            modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(Primary, PrimaryVariant)))
                .padding(16.dp)
        ) {
            Column {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) { Text(card.emoji, fontSize = 17.sp) }
                Spacer(Modifier.height(10.dp))
                Text(card.value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-0.5).sp)
                Text(card.label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.75f), letterSpacing = 0.4.sp)
                Text(card.change, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.8f))
            }
        }
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(Modifier.padding(16.dp)) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(card.iconBg),
                    contentAlignment = Alignment.Center
                ) { Text(card.emoji, fontSize = 17.sp) }
                Spacer(Modifier.height(10.dp))
                Text(card.value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.5).sp)
                Text(card.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.4.sp)
                Text(card.change, style = MaterialTheme.typography.labelSmall, color = if (card.changePositive) Success else Danger)
            }
        }
    }
}

@Composable
private fun DashQuickActions(modifier: Modifier = Modifier, viewModel: DashboardViewModel) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quickActions.chunked(4).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        row.forEach { action ->
                            Column(
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        when (action.label) {
                                            "New Sale"    -> viewModel.onIntent(DashboardIntent.NavigateToPOS)
                                            "Add Stock"   -> viewModel.onIntent(DashboardIntent.NavigateToInventory)
                                            "Reports"     -> viewModel.onIntent(DashboardIntent.NavigateToTransactions)
                                            "Cash & Bank", "Collect Due" -> viewModel.onIntent(DashboardIntent.NavigateToLedger)
                                        }
                                    }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(action.bg),
                                    contentAlignment = Alignment.Center
                                ) { Text(action.emoji, fontSize = 20.sp) }
                                Text(
                                    action.label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashRecentSalesCard(state: DashboardState) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Recent Sales", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {
                    Text("See All", color = Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            if (state.recentSales.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    Text("No sales yet today", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.recentSales.take(5).forEach { sale -> TxItem(sale, state) }
                }
            }
        }
    }
}

@Composable
private fun TxItem(sale: Sale, state: DashboardState) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) { Text("💰", fontSize = 16.sp) }
        Column(Modifier.weight(1f)) {
            Text(sale.invoiceNumber, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(sale.customerName ?: "Walk-in Customer", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            if (state.analyticsVisible) CurrencyFormatter.formatRs(sale.totalAmount) else "••••",
            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DashQuickStatsCard(state: DashboardState) {
    fun fmt(v: Double) = if (state.analyticsVisible) CurrencyFormatter.format(v) else "••••"
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Quick Stats", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            val rows = listOf(
                Triple("Today's Revenue", fmt(state.todayRevenue),     Primary),
                Triple("Gross Profit",    fmt(state.todayGrossProfit), Success),
                Triple("Expenses",        fmt(state.todayExpenses),    Danger),
                Triple("Net Profit",      fmt(state.todayNetProfit),   if (state.todayNetProfit >= 0) Success else Danger),
            )
            rows.forEachIndexed { idx, (label, value, color) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                }
                if (idx < rows.lastIndex) Divider(color = BorderFaint, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun DashActivityCard(state: DashboardState) {
    if (state.recentSales.isEmpty() && state.lowStockProducts.isEmpty()) return
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Activity Feed", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DashLowStockCard(state: DashboardState) {
    if (state.lowStockProducts.isEmpty()) return
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Low Stock Alerts", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

