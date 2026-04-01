package com.matechmatrix.shopflowpos.feature.salesreturn.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AssignmentReturn
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
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CashAccount
import com.matechmatrix.shopflowpos.core.model.SaleReturn
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.RefundMethod
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.EmptyStateView
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SalesReturnScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : SalesReturnViewModel = koinViewModel()
) {
    val state         by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is SalesReturnEffect.ShowToast) snackbarState.showSnackbar(e.message)
        }
    }

    val hPad = when (windowSize) { AppWindowSize.EXPANDED -> 28.dp; AppWindowSize.MEDIUM -> 20.dp; else -> 16.dp }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            if (windowSize == AppWindowSize.COMPACT) {
                FloatingActionButton(
                    onClick        = { viewModel.onIntent(SalesReturnIntent.ShowAddDialog) },
                    containerColor = Warning,
                    contentColor   = Color.White,
                    shape          = androidx.compose.foundation.shape.CircleShape
                ) { Icon(Icons.AutoMirrored.Rounded.AssignmentReturn, "New Return") }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(horizontal = hPad, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
//                Column {
//                    Text("Sales Returns", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
//                    Text("This month", style = MaterialTheme.typography.bodySmall, color = TextMuted)
//                }
                if (windowSize != AppWindowSize.COMPACT) {
                    Button(
                        onClick = { viewModel.onIntent(SalesReturnIntent.ShowAddDialog) },
                        colors  = ButtonDefaults.buttonColors(containerColor = Warning),
                        shape   = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.AssignmentReturn, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Record Return", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Summary ────────────────────────────────────────────────────────
            if (!state.isLoading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryBox(Modifier.weight(1f), "Returns", "${state.returns.size}", Warning, "📦")
                    SummaryBox(Modifier.weight(1f), "Total Refunded", "${state.currencySymbol} ${CurrencyFormatter.formatCompact(state.totalReturns)}", Danger, "💸")
                }
            }

            // ── Content ────────────────────────────────────────────────────────
            when {
                state.isLoading -> LoadingView()
                state.returns.isEmpty() -> EmptyStateView(
                    icon     = Icons.AutoMirrored.Rounded.AssignmentReturn,
                    title    = "No returns this month",
                    subtitle = "Tap + to record a sales return"
                )
                else -> when (windowSize) {
                    AppWindowSize.COMPACT  -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 88.dp)) {
                        items(state.returns.size, key = { state.returns[it].id }) { i ->
                            ReturnCard(state.returns[i], state.currencySymbol)
                        }
                    }
                    AppWindowSize.MEDIUM   -> LazyVerticalGrid(GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                        items(state.returns.size, key = { state.returns[it].id }) { i ->
                            ReturnCard(state.returns[i], state.currencySymbol)
                        }
                    }
                    AppWindowSize.EXPANDED -> ReturnDesktopTable(state)
                }
            }
        }
    }

    if (state.showAddDialog) ReturnAddDialog(state = state, viewModel = viewModel)
}

@Composable
private fun SummaryBox(modifier: Modifier, label: String, value: String, color: Color, emoji: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = color.copy(0.08f)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 18.sp)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(0.7f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReturnCard(r: SaleReturn, currency: String) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(WarningContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.AssignmentReturn, null, tint = Warning, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(r.returnNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(r.customerName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                Text(r.returnReason, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(DateTimeUtils.formatDate(r.returnedAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (r.deductionAmount > 0) {
                    Text("-$currency ${CurrencyFormatter.formatRs(r.grossRefundAmount)}", style = MaterialTheme.typography.labelSmall, color = TextMuted, textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                }
                Text("-$currency ${CurrencyFormatter.formatRs(r.netRefundAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Danger)
                Surface(shape = RoundedCornerShape(100.dp), color = WarningContainer) {
                    Text(r.refundMethod.display, Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Warning)
                }
            }
        }
    }
}

@Composable
private fun ReturnDesktopTable(state: SalesReturnState) {
    Card(Modifier.fillMaxSize(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column {
            Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 20.dp, vertical = 12.dp)) {
                listOf("Return #" to 0.15f, "Customer" to 0.20f, "Reason" to 0.25f, "Date" to 0.15f, "Gross" to 0.12f, "Net Refund" to 0.13f).forEach { (label, weight) ->
                    Text(label.uppercase(), Modifier.weight(weight), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 0.6.sp)
                }
            }
            HorizontalDivider(color = BorderFaint)
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(state.returns, key = { _, r -> r.id }) { idx, r ->
                    Row(
                        Modifier.fillMaxWidth().background(if (idx % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(0.35f)).padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.returnNumber, Modifier.weight(0.15f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Warning)
                        Text(r.customerName, Modifier.weight(0.20f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(r.returnReason, Modifier.weight(0.25f), style = MaterialTheme.typography.bodySmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(DateTimeUtils.formatDate(r.returnedAt), Modifier.weight(0.15f), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("${state.currencySymbol} ${CurrencyFormatter.formatCompact(r.grossRefundAmount)}", Modifier.weight(0.12f), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text("-${state.currencySymbol} ${CurrencyFormatter.formatCompact(r.netRefundAmount)}", Modifier.weight(0.13f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Danger)
                    }
                    HorizontalDivider(color = BorderFaint.copy(0.4f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 20.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Return Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReturnAddDialog(state: SalesReturnState, viewModel: SalesReturnViewModel) {
    var showAccountDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(SalesReturnIntent.DismissDialog) },
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        LazyColumn(
            Modifier.fillMaxWidth().navigationBarsPadding(),
            contentPadding      = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Record Sales Return", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }

            // ── Invoice lookup ──────────────────────────────────────────────────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value         = state.searchInvoice,
                        onValueChange = { viewModel.onIntent(SalesReturnIntent.SearchInvoice(it)) },
                        label         = { Text("Invoice Number (e.g. INV-00001)") },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                    )
                    FilledIconButton(
                        onClick  = { viewModel.onIntent(SalesReturnIntent.LookupSale) },
                        modifier = Modifier.size(52.dp),
                        colors   = IconButtonDefaults.filledIconButtonColors(containerColor = Warning)
                    ) {
                        if (state.isSearching) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                        else Icon(Icons.Rounded.Search, null, tint = Color.White)
                    }
                }
                state.saleSearchError?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Danger)
                }
            }

            // ── Found sale info ─────────────────────────────────────────────────
            state.foundSale?.let { sale ->
                item {
                    Surface(shape = RoundedCornerShape(10.dp), color = SuccessContainer) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("✅ ${sale.invoiceNumber}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Success)
                                Text(DateTimeUtils.formatDate(sale.soldAt), style = MaterialTheme.typography.labelSmall, color = Success.copy(0.7f))
                            }
                            Text("${sale.customerName}${if (sale.customerPhone.isNotBlank()) " · ${sale.customerPhone}" else ""}", style = MaterialTheme.typography.labelSmall, color = Success.copy(0.7f))
                            Text("Total: ${state.currencySymbol} ${CurrencyFormatter.formatRs(sale.totalAmount)}  |  Status: ${sale.status.display}", style = MaterialTheme.typography.labelSmall, color = Success.copy(0.7f))
                        }
                    }
                }

                // ── Item selection table ──────────────────────────────────────────
                item { Text("Select Items to Return", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted) }

                state.foundItems.forEachIndexed { _, itemState ->
                    item(key = itemState.saleItem.id) {
                        Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(itemState.saleItem.productName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                                        if (!itemState.saleItem.imei.isNullOrBlank()) Text(itemState.saleItem.imei!!, style = MaterialTheme.typography.labelSmall, color = Primary)
                                        Text("Sold: ${itemState.saleItem.quantity} × ${state.currencySymbol} ${CurrencyFormatter.formatRs(itemState.saleItem.unitPrice)}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                    // Qty stepper
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IconButton(
                                            onClick  = { viewModel.onIntent(SalesReturnIntent.SetItemQty(itemState.saleItem.id, itemState.returnedQty - 1)) },
                                            modifier = Modifier.size(28.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.surface)
                                        ) { Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(12.dp)) }
                                        Text("${itemState.returnedQty}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 24.dp), textAlign = TextAlign.Center)
                                        IconButton(
                                            onClick  = { viewModel.onIntent(SalesReturnIntent.SetItemQty(itemState.saleItem.id, itemState.returnedQty + 1)) },
                                            modifier = Modifier.size(28.dp).clip(androidx.compose.foundation.shape.CircleShape).background(if (itemState.returnedQty < itemState.saleItem.quantity) Warning else BorderColor)
                                        ) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                                    }
                                }
                                // Restock toggle
                                AnimatedVisibility(itemState.returnedQty > 0) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Add back to inventory", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        Switch(
                                            checked         = itemState.restockItem,
                                            onCheckedChange = { viewModel.onIntent(SalesReturnIntent.SetItemRestock(itemState.saleItem.id, it)) },
                                            modifier        = Modifier.height(24.dp),
                                            colors          = SwitchDefaults.colors(checkedThumbColor = Success, checkedTrackColor = SuccessContainer)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Deduction & Refund ──────────────────────────────────────────
                if (state.hasSelectedItems) {
                    item {
                        Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Gross Refund", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    Text("${state.currencySymbol} ${CurrencyFormatter.formatRs(state.grossRefund)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Deduction %", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                    OutlinedTextField(
                                        value         = state.formDeductionPct,
                                        onValueChange = { viewModel.onIntent(SalesReturnIntent.FormDeductionPct(it)) },
                                        modifier      = Modifier.width(110.dp),
                                        singleLine    = true,
                                        suffix        = { Text("%") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        textStyle     = MaterialTheme.typography.bodySmall,
                                        shape         = RoundedCornerShape(8.dp),
                                        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                                    )
                                }
                                HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Net Refund", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Text("${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netRefund)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = if (state.netRefund > 0) Success else TextMuted)
                                }
                            }
                        }
                    }

                    // Reason + notes
                    item {
                        OutlinedTextField(
                            value         = state.formReason,
                            onValueChange = { viewModel.onIntent(SalesReturnIntent.FormReason(it)) },
                            label         = { Text("Return Reason *") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                        )
                    }
                    item {
                        OutlinedTextField(
                            value         = state.formNotes,
                            onValueChange = { viewModel.onIntent(SalesReturnIntent.FormNotes(it)) },
                            label         = { Text("Notes (optional)") },
                            modifier      = Modifier.fillMaxWidth(),
                            maxLines      = 2,
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                        )
                    }

                    // Refund method
                    item {
                        Text("Refund Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextMuted)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RefundMethod.entries.forEach { method ->
                                val sel = state.formRefundMethod == method
                                FilterChip(
                                    selected = sel,
                                    onClick  = { viewModel.onIntent(SalesReturnIntent.FormRefundMethod(method)) },
                                    label    = { Text(method.display, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f),
                                    colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = Warning, selectedLabelColor = Color.White)
                                )
                            }
                        }
                    }

                    // Account selector
                    if (state.formRefundMethod != RefundMethod.STORE_CREDIT) {
                        item {
                            val accounts = if (state.formAccountType == AccountType.CASH) state.cashAccounts else state.bankAccounts
                            if (accounts.isNotEmpty()) {
                                ExposedDropdownMenuBox(expanded = showAccountDropdown, onExpandedChange = { showAccountDropdown = it }) {
                                    OutlinedTextField(
                                        value         = accounts.find { 
                                            when(it) {
                                                is CashAccount -> it.id == state.formAccountId
                                                is BankAccount -> it.id == state.formAccountId
                                                else -> false
                                            }
                                        }?.let { if (it is CashAccount) it.name else (it as BankAccount).bankName } ?: "Select Account",
                                        onValueChange = {}, readOnly = true,
                                        label         = { Text("Refund From") },
                                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showAccountDropdown) },
                                        modifier      = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                        shape         = RoundedCornerShape(10.dp),
                                        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                                    )
                                    ExposedDropdownMenu(expanded = showAccountDropdown, onDismissRequest = { showAccountDropdown = false }) {
                                        accounts.forEach { acc ->
                                            val (id, label) = if (acc is CashAccount) acc.id to acc.name else (acc as BankAccount).run { id to "$bankName · $accountNumber" }
                                            DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.onIntent(SalesReturnIntent.FormAccountId(id)); showAccountDropdown = false })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                state.formError?.let {
                    item { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
                }

                // Submit
                item {
                    Button(
                        onClick   = { viewModel.onIntent(SalesReturnIntent.SaveReturn) },
                        enabled   = !state.isProcessing && state.hasSelectedItems,
                        modifier  = Modifier.fillMaxWidth().height(54.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Warning),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        if (state.isProcessing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else {
                            Icon(Icons.AutoMirrored.Rounded.AssignmentReturn, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Process Return · Refund ${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netRefund)}", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
