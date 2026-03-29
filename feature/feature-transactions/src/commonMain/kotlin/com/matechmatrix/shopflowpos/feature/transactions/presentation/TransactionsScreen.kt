package com.matechmatrix.shopflowpos.feature.transactions.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.Sale
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : TransactionsViewModel = koinViewModel()
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
            items(TxDateFilter.entries) { f ->
                val selected = state.dateFilter == f
                Surface(
                    onClick = { viewModel.onIntent(TransactionsIntent.SetFilter(f)) },
                    shape  = RoundedCornerShape(100.dp),
                    color  = if (selected) Primary else MaterialTheme.colorScheme.surface,
                    border = if (selected) null
                    else BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Text(
                        f.name.lowercase().replaceFirstChar { it.uppercase() },
                        Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (selected) Color.White
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Summary card ────────────────────────────────────────────────────
        if (!state.isLoading) {
            Card(
                Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TxSumItem("${state.totalCount}", "Sales", Primary)
                    VerticalDivider(Modifier.height(36.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    TxSumItem(CurrencyFormatter.formatCompact(state.totalRevenue), "Revenue", Success)
                }
            }
        }

        // ── Main content ────────────────────────────────────────────────────
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }

            state.sales.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.Receipt, null, tint = Primary, modifier = Modifier.size(32.dp)) }
                    Text("No transactions found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text("Transactions will appear here once you make sales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            else -> when (windowSize) {
                AppWindowSize.COMPACT  -> TxCompactList(state)
                AppWindowSize.MEDIUM   -> TxMediumGrid(state)
                AppWindowSize.EXPANDED -> TxDesktopTable(state)
            }
        }
    }
}

// ── Summary item ──────────────────────────────────────────────────────────────
@Composable
private fun TxSumItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── COMPACT — vertical card list ─────────────────────────────────────────────
@Composable
private fun TxCompactList(state: TransactionsState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        items(state.sales, key = { it.id }) { sale ->
            TxCard(sale, state.currencySymbol)
        }
    }
}

// ── MEDIUM — 2-column card grid ───────────────────────────────────────────────
@Composable
private fun TxMediumGrid(state: TransactionsState) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding        = PaddingValues(bottom = 88.dp)
    ) {
        items(state.sales, key = { it.id }) { sale ->
            TxCard(sale, state.currencySymbol)
        }
    }
}

// ── EXPANDED — desktop data table ────────────────────────────────────────────
@Composable
private fun TxDesktopTable(state: TransactionsState) {
    Card(
        Modifier.fillMaxSize(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // Header
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TxTableHeader("Invoice",  0.22f)
                TxTableHeader("Date",     0.20f)
                TxTableHeader("Method",   0.20f)
                TxTableHeader("Amount",   0.20f, TextAlign.End)
                TxTableHeader("Status",   0.18f, TextAlign.Center)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                itemsIndexed(state.sales, key = { _, s -> s.id }) { idx, sale ->
                    TxTableRow(sale, state.currencySymbol, idx % 2 == 0)
                    HorizontalDivider(
                        color    = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.TxTableHeader(
    label: String, weight: Float,
    align: TextAlign = TextAlign.Start
) {
    Text(
        label.uppercase(),
        modifier   = Modifier.weight(weight),
        style      = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.6.sp,
        textAlign  = align
    )
}

@Composable
private fun TxTableRow(sale: Sale, currencySymbol: String, isEven: Boolean) {
    val (payColor, payBg) = txPayPair(sale.paymentMethod)
    val (statusLabel, statusColor, statusBg) = txStatusTriple(sale.paymentMethod)

    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (isEven) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Invoice
        Row(
            Modifier.weight(0.22f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) { Text(txEmoji(sale.paymentMethod), fontSize = 13.sp) }
            Text(
                sale.invoiceNumber,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
        }
        // Date
        Text(
            DateTimeUtils.formatDate(sale.soldAt),
            modifier  = Modifier.weight(0.20f),
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Method chip
        Box(Modifier.weight(0.20f)) {
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = payBg
            ) {
                Text(
                    sale.paymentMethod.name.lowercase().replaceFirstChar { it.uppercase() },
                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = payColor
                )
            }
        }
        // Amount
        Text(
            CurrencyFormatter.formatRs(sale.totalAmount),
            modifier   = Modifier.weight(0.20f),
            style      = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface,
            textAlign  = TextAlign.End
        )
        // Status
        Box(Modifier.weight(0.18f), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(100.dp), color = statusBg) {
                Text(
                    statusLabel,
                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color      = statusColor
                )
            }
        }
    }
}

// ── Card (Compact & Medium) ───────────────────────────────────────────────────
@Composable
private fun TxCard(sale: Sale, currencySymbol: String) {
    val (payColor, payBg)             = txPayPair(sale.paymentMethod)
    val (statusLabel, statusColor, statusBg) = txStatusTriple(sale.paymentMethod)

    Card(
        Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) { Text(txEmoji(sale.paymentMethod), fontSize = 18.sp) }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    sale.invoiceNumber,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    DateTimeUtils.formatDate(sale.soldAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Surface(shape = RoundedCornerShape(100.dp), color = payBg) {
                    Text(
                        sale.paymentMethod.name.lowercase().replaceFirstChar { it.uppercase() },
                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = payColor
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    CurrencyFormatter.formatRs(sale.totalAmount),
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                if (sale.discount > 0) {
                    Text(
                        "Disc: ${CurrencyFormatter.formatRs(sale.discount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(2.dp))
                Surface(shape = RoundedCornerShape(100.dp), color = statusBg) {
                    Text(
                        statusLabel,
                        Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = statusColor
                    )
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun txEmoji(m: PaymentMethod) = when (m) {
    PaymentMethod.CASH          -> "💵"
    PaymentMethod.BANK_TRANSFER -> "🏦"
    PaymentMethod.CREDIT        -> "📋"
    PaymentMethod.PARTIAL       -> "🔀"
    else                        -> "💳"
}

private fun txPayPair(m: PaymentMethod): Pair<Color, Color> = when (m) {
    PaymentMethod.CASH                               -> Success to SuccessContainer
    PaymentMethod.BANK_TRANSFER, PaymentMethod.CARD  -> Info    to InfoContainer
    PaymentMethod.PARTIAL, PaymentMethod.SPLIT       -> Warning to WarningContainer
    PaymentMethod.CREDIT                             -> Danger  to DangerContainer
}

private fun txStatusTriple(m: PaymentMethod): Triple<String, Color, Color> = when (m) {
    PaymentMethod.CREDIT  -> Triple("DUE",     Danger,  DangerContainer)
    PaymentMethod.PARTIAL -> Triple("PARTIAL", Warning, WarningContainer)
    else                  -> Triple("PAID",    Success, SuccessContainer)
}