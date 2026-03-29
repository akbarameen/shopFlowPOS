package com.matechmatrix.shopflowpos.feature.salesreturn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.SaleReturn
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SalesReturnScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : SalesReturnViewModel = koinViewModel()
) {
    val state            = viewModel.state.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is SalesReturnEffect.Toast) snackbarHostState.showSnackbar(e.msg)
        }
    }

    val hPad = when (windowSize) {
        AppWindowSize.EXPANDED -> 28.dp
        AppWindowSize.MEDIUM   -> 20.dp
        AppWindowSize.COMPACT  -> 16.dp
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (windowSize == AppWindowSize.COMPACT) {
                FloatingActionButton(
                    onClick        = { viewModel.onIntent(SalesReturnIntent.ShowAddDialog) },
                    containerColor = Warning,
                    contentColor   = Color.White,
                    shape          = androidx.compose.foundation.shape.CircleShape
                ) { Icon(Icons.Rounded.Add, "New Return") }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = hPad, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Top bar with Add button on Medium/Expanded ───────────────────
            if (windowSize != AppWindowSize.COMPACT) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { viewModel.onIntent(SalesReturnIntent.ShowAddDialog) },
                        colors  = ButtonDefaults.buttonColors(containerColor = Warning),
                        shape   = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Record Return", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Summary ──────────────────────────────────────────────────────
            if (!state.isLoading) {
                Card(
                    Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("${state.returns.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold, color = Warning)
                            Text("Returns", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        VerticalDivider(Modifier.height(36.dp),
                            color = MaterialTheme.colorScheme.outlineVariant)
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(CurrencyFormatter.formatCompact(state.totalReturns),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold, color = Danger)
                            Text("Refunded", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Content ──────────────────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Warning)
                }
                state.returns.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(WarningContainer),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.AssignmentReturn, null, tint = Warning,
                            modifier = Modifier.size(32.dp)) }
                        Text("No returns this month",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("Tap + to record a sales return",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> when (windowSize) {
                    AppWindowSize.COMPACT  -> ReturnCompactList(state)
                    AppWindowSize.MEDIUM   -> ReturnMediumGrid(state)
                    AppWindowSize.EXPANDED -> ReturnDesktopTable(state)
                }
            }
        }
    }

    if (state.showAddDialog) ReturnAddDialog(state, viewModel)
}

// ── COMPACT ───────────────────────────────────────────────────────────────────
@Composable
private fun ReturnCompactList(state: SalesReturnState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(bottom = 88.dp)
    ) {
        items(state.returns, key = { it.id }) { r -> ReturnCard(r, state.currencySymbol) }
    }
}

// ── MEDIUM ────────────────────────────────────────────────────────────────────
@Composable
private fun ReturnMediumGrid(state: SalesReturnState) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding        = PaddingValues(bottom = 24.dp)
    ) {
        items(state.returns, key = { it.id }) { r -> ReturnCard(r, state.currencySymbol) }
    }
}

// ── EXPANDED — desktop table ──────────────────────────────────────────────────
@Composable
private fun ReturnDesktopTable(state: SalesReturnState) {
    Card(
        Modifier.fillMaxSize(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RetTHeader("Product",       0.30f)
                RetTHeader("Reason",        0.25f)
                RetTHeader("Date",          0.20f)
                RetTHeader("Refund Amount", 0.25f, TextAlign.End)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(state.returns, key = { _, r -> r.id }) { idx, r ->
                    ReturnTableRow(r, state.currencySymbol, idx % 2 == 0)
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.RetTHeader(
    label: String, weight: Float,
    align: TextAlign = TextAlign.Start
) {
    Text(
        label.uppercase(),
        modifier      = Modifier.weight(weight),
        style         = MaterialTheme.typography.labelSmall,
        fontWeight    = FontWeight.Bold,
        color         = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.6.sp,
        textAlign     = align
    )
}

@Composable
private fun ReturnTableRow(r: SaleReturn, currencySymbol: String, isEven: Boolean) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (isEven) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(0.30f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(WarningContainer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.AssignmentReturn, null, tint = Warning, modifier = Modifier.size(16.dp)) }
            Text(r.productName, style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(r.returnReason, modifier = Modifier.weight(0.25f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(DateTimeUtils.formatDate(r.returnedAt), modifier = Modifier.weight(0.20f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(Modifier.weight(0.25f), horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("-${CurrencyFormatter.formatRs(r.refundAmount)}",
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Danger)
            Surface(shape = RoundedCornerShape(100.dp), color = WarningContainer) {
                Text("RETURNED", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Warning)
            }
        }
    }
}

// ── Card ──────────────────────────────────────────────────────────────────────
@Composable
private fun ReturnCard(r: SaleReturn, currencySymbol: String) {
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
                Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(WarningContainer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.AssignmentReturn, null, tint = Warning, modifier = Modifier.size(20.dp)) }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(r.productName, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(r.returnReason, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(DateTimeUtils.formatDate(r.returnedAt), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("-${CurrencyFormatter.formatRs(r.refundAmount)}",
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Danger)
                Surface(shape = RoundedCornerShape(100.dp), color = WarningContainer) {
                    Text("RETURNED", Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Warning)
                }
            }
        }
    }
}

// ── Add Return Dialog ─────────────────────────────────────────────────────────
@Composable
private fun ReturnAddDialog(state: SalesReturnState, viewModel: SalesReturnViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(SalesReturnIntent.DismissDialog) },
        title = { Text("Record Sales Return", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value         = state.searchInvoice,
                        onValueChange = { viewModel.onIntent(SalesReturnIntent.SearchInvoice(it)) },
                        label         = { Text("Invoice Number") },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Warning,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    FilledIconButton(
                        onClick  = { viewModel.onIntent(SalesReturnIntent.LookupSale) },
                        modifier = Modifier.size(48.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(containerColor = PrimaryContainer)
                    ) { Icon(Icons.Rounded.Search, null, tint = Primary) }
                }

                state.saleSearchError?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Danger)
                }

                state.foundSale?.let { sale ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SuccessContainer
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Found: ${sale.invoiceNumber}",
                                    style = MaterialTheme.typography.bodySmall, color = Success,
                                    fontWeight = FontWeight.Bold)
                                Text("Amount: ${CurrencyFormatter.formatRs(sale.totalAmount)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Success.copy(alpha = 0.7f))
                            }
                            Icon(Icons.Rounded.CheckCircle, null, tint = Success, modifier = Modifier.size(20.dp))
                        }
                    }

                    OutlinedTextField(
                        value         = state.formReason,
                        onValueChange = { viewModel.onIntent(SalesReturnIntent.FormReason(it)) },
                        label         = { Text("Reason for return *") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Warning,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    OutlinedTextField(
                        value         = state.formRefundAmount,
                        onValueChange = { viewModel.onIntent(SalesReturnIntent.FormRefund(it)) },
                        label         = { Text("Refund Amount") },
                        prefix        = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Warning,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
                state.formError?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Danger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { viewModel.onIntent(SalesReturnIntent.SaveReturn) },
                colors   = ButtonDefaults.buttonColors(containerColor = Warning),
                shape    = RoundedCornerShape(10.dp),
                enabled  = state.foundSale != null
            ) { Text("Record Return") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(SalesReturnIntent.DismissDialog) }) { Text("Cancel") }
        }
    )
}