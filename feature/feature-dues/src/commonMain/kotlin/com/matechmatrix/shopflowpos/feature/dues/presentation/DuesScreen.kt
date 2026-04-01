package com.matechmatrix.shopflowpos.feature.dues.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.EmptyStateView
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DuesScreen(
    windowSize: AppWindowSize,
    navigateChild: (String) -> Unit = {},
    viewModel: DuesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is DuesEffect.ShowToast) snackbarState.showSnackbar(e.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(
                    horizontal = when (windowSize) {
                        AppWindowSize.EXPANDED -> 28.dp; AppWindowSize.MEDIUM -> 20.dp; else -> 16.dp
                    }, vertical = 12.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
//            // ── Header ──────────────────────────────────────────────────────────
//            Text(
//                "Dues",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
//                color = TextPrimary
//            )

            // ── Summary hero ────────────────────────────────────────────────────
            if (!state.isLoading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DuesSummaryBox(
                        modifier = Modifier.weight(1f),
                        label = "Receivable",
                        subtitle = "Customers owe me",
                        value = "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.totalReceivable)}",
                        color = Success,
                        icon = "📥",
                        onClick = { viewModel.onIntent(DuesIntent.SwitchTab(DuesTab.RECEIVABLE)) }
                    )
                    DuesSummaryBox(
                        modifier = Modifier.weight(1f),
                        label = "Payable",
                        subtitle = "I owe suppliers",
                        value = "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.totalPayable)}",
                        color = Danger,
                        icon = "📤",
                        onClick = { viewModel.onIntent(DuesIntent.SwitchTab(DuesTab.PAYABLE)) }
                    )
                }
            }

            // ── Tab row ──────────────────────────────────────────────────────────
            TabRow(
                selectedTabIndex = state.activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = state.activeTab == DuesTab.RECEIVABLE,
                    onClick = { viewModel.onIntent(DuesIntent.SwitchTab(DuesTab.RECEIVABLE)) },
                    text = {
                        Text(
                            "Receivable (${state.customersWithDues.size})",
                            fontWeight = if (state.activeTab == DuesTab.RECEIVABLE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Rounded.ArrowDownward,
                            null,
                            tint = Success,
                            modifier = Modifier.size(16.dp)
                        )
                    })
                Tab(
                    selected = state.activeTab == DuesTab.PAYABLE,
                    onClick = { viewModel.onIntent(DuesIntent.SwitchTab(DuesTab.PAYABLE)) },
                    text = {
                        Text(
                            "Payable (${state.suppliersWithDues.size})",
                            fontWeight = if (state.activeTab == DuesTab.PAYABLE) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            Icons.Rounded.ArrowUpward,
                            null,
                            tint = Danger,
                            modifier = Modifier.size(16.dp)
                        )
                    })
            }

            // ── Content ──────────────────────────────────────────────────────────
            if (state.isLoading) {
                LoadingView(); return@Column
            }

            when (state.activeTab) {
                DuesTab.RECEIVABLE -> ReceivableContent(
                    state = state,
                    viewModel = viewModel,
                    windowSize = windowSize
                )

                DuesTab.PAYABLE -> PayableContent(
                    state = state,
                    viewModel = viewModel,
                    windowSize = windowSize
                )
            }
        }
    }

    // ── Payment dialog ─────────────────────────────────────────────────────────
    state.payDialog?.let { PaymentDialog(dialog = it, state = state, viewModel = viewModel) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary Box
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DuesSummaryBox(
    modifier: Modifier,
    label: String,
    subtitle: String,
    value: String,
    color: Color,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(icon, style = MaterialTheme.typography.titleMedium)
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = color.copy(0.7f))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Receivable (customer owes me)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ReceivableContent(
    state: DuesState,
    viewModel: DuesViewModel,
    windowSize: AppWindowSize
) {
    if (state.customersWithDues.isEmpty()) {
        EmptyStateView(
            icon = Icons.Rounded.CheckCircle,
            title = "No receivables!",
            subtitle = "All customers are up to date"
        )
        return
    }

    if (windowSize == AppWindowSize.EXPANDED) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Customer list
            LazyColumn(
                Modifier.weight(0.35f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.customersWithDues.size, key = { state.customersWithDues[it].id }) { i ->
                    val c = state.customersWithDues[i]
                    CustomerDueChip(
                        c,
                        state.currencySymbol,
                        isSelected = c.id == state.selectedCustomerId
                    ) {
                        viewModel.onIntent(DuesIntent.FilterByCustomer(c.id))
                    }
                }
            }
            // Sales list
            LazyColumn(
                Modifier.weight(0.65f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (state.filteredSales.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Select a customer to view their dues", color = TextMuted) }
                    }
                }
                items(state.filteredSales.size, key = { state.filteredSales[it].id }) { i ->
                    SaleDueCard(state.filteredSales[i], state.currencySymbol) {
                        viewModel.onIntent(
                            DuesIntent.ShowCollectDialog(state.filteredSales[i])
                        )
                    }
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            // Customer filter chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedCustomerId == null,
                            onClick = { viewModel.onIntent(DuesIntent.FilterByCustomer(null)) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(state.customersWithDues.size) { i ->
                        val c = state.customersWithDues[i]
                        FilterChip(
                            selected = c.id == state.selectedCustomerId,
                            onClick = { viewModel.onIntent(DuesIntent.FilterByCustomer(c.id)) },
                            label = { Text("${c.name} (${CurrencyFormatter.formatCompact(c.outstandingBalance)})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            if (state.filteredSales.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Rounded.Receipt,
                        title = "No outstanding dues",
                        subtitle = "All invoices in this selection are fully paid"
                    )
                }
            }
            items(state.filteredSales.size, key = { state.filteredSales[it].id }) { i ->
                SaleDueCard(state.filteredSales[i], state.currencySymbol) {
                    viewModel.onIntent(
                        DuesIntent.ShowCollectDialog(state.filteredSales[i])
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerDueChip(
    customer: Customer,
    currency: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    customer.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (customer.phone.isNotBlank()) Text(
                    customer.phone,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
            Text(
                "${currency} ${CurrencyFormatter.formatCompact(customer.outstandingBalance)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Danger
            )
        }
    }
}

@Composable
private fun SaleDueCard(sale: Sale, currency: String, onCollect: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        sale.invoiceNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        sale.customerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (sale.customerPhone.isNotBlank()) Text(
                        sale.customerPhone,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        "Sold: ${DateTimeUtils.formatDate(sale.soldAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total: ${currency} ${CurrencyFormatter.formatRs(sale.totalAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        "Paid: ${currency} ${CurrencyFormatter.formatRs(sale.paidAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Success
                    )
                    Text(
                        "Due: ${currency} ${CurrencyFormatter.formatRs(sale.dueAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Danger
                    )
                }
            }
            sale.dueDate?.let {
                Text(
                    "Expected by: ${DateTimeUtils.formatDate(it)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Warning
                )
            }
            Button(
                onClick = onCollect,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Success),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Rounded.ArrowDownward, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Collect Due: ${currency} ${CurrencyFormatter.formatRs(sale.dueAmount)}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payable (I owe supplier)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PayableContent(state: DuesState, viewModel: DuesViewModel, windowSize: AppWindowSize) {
    if (state.suppliersWithDues.isEmpty()) {
        EmptyStateView(
            icon = Icons.Rounded.CheckCircle,
            title = "No payables!",
            subtitle = "You're all clear with your suppliers"
        )
        return
    }

    if (windowSize == AppWindowSize.EXPANDED) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LazyColumn(
                Modifier.weight(0.35f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(state.suppliersWithDues.size, key = { state.suppliersWithDues[it].id }) { i ->
                    val s = state.suppliersWithDues[i]
                    CustomerDueChip(
                        customer = com.matechmatrix.shopflowpos.core.model.Customer(
                            id = s.id,
                            name = s.name,
                            phone = s.phone,
                            outstandingBalance = s.outstandingBalance,
                            createdAt = 0L,
                            updatedAt = 0L
                        ),
                        currency = state.currencySymbol,
                        isSelected = s.id == state.selectedSupplierId
                    ) { viewModel.onIntent(DuesIntent.FilterBySupplier(s.id)) }
                }
            }
            LazyColumn(
                Modifier.weight(0.65f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (state.filteredPurchaseOrders.isEmpty()) {
                    item {
                        Box(
                            Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("Select a supplier", color = TextMuted) }
                    }
                }
                items(
                    state.filteredPurchaseOrders.size,
                    key = { state.filteredPurchaseOrders[it].id }) { i ->
                    PurchaseOrderDueCard(
                        state.filteredPurchaseOrders[i],
                        state.currencySymbol
                    ) { viewModel.onIntent(DuesIntent.ShowPayDialog(state.filteredPurchaseOrders[i])) }
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.selectedSupplierId == null,
                            onClick = { viewModel.onIntent(DuesIntent.FilterBySupplier(null)) },
                            label = { Text("All") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(state.suppliersWithDues.size) { i ->
                        val s = state.suppliersWithDues[i]
                        FilterChip(
                            selected = s.id == state.selectedSupplierId,
                            onClick = { viewModel.onIntent(DuesIntent.FilterBySupplier(s.id)) },
                            label = { Text("${s.name} (${CurrencyFormatter.formatCompact(s.outstandingBalance)})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Danger,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            items(
                state.filteredPurchaseOrders.size,
                key = { state.filteredPurchaseOrders[it].id }) { i ->
                PurchaseOrderDueCard(
                    state.filteredPurchaseOrders[i],
                    state.currencySymbol
                ) { viewModel.onIntent(DuesIntent.ShowPayDialog(state.filteredPurchaseOrders[i])) }
            }
        }
    }
}

@Composable
private fun PurchaseOrderDueCard(order: PurchaseOrder, currency: String, onPay: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        order.poNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE67E22)
                    )
                    Text(
                        order.supplierName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (order.supplierPhone.isNotBlank()) Text(
                        order.supplierPhone,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        "Purchased: ${DateTimeUtils.formatDate(order.purchasedAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total: ${currency} ${CurrencyFormatter.formatRs(order.totalAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        "Paid: ${currency} ${CurrencyFormatter.formatRs(order.paidAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Success
                    )
                    Text(
                        "Due: ${currency} ${CurrencyFormatter.formatRs(order.dueAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Danger
                    )
                }
            }
            Button(
                onClick = onPay,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Danger),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(Icons.Rounded.ArrowUpward, null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    "Pay Due: ${currency} ${CurrencyFormatter.formatRs(order.dueAmount)}",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment Dialog (shared for both tabs)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDialog(dialog: PayDialogState, state: DuesState, viewModel: DuesViewModel) {
    val isReceivable = dialog.type == DuesTab.RECEIVABLE
    val accentColor = if (isReceivable) Success else Danger
    val accentBg = if (isReceivable) SuccessContainer else DangerContainer
    var showBankDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.onIntent(DuesIntent.DismissPayDialog) },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (isReceivable) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                    null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    if (isReceivable) "Collect Payment" else "Pay Supplier",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Reference info
                Surface(shape = RoundedCornerShape(10.dp), color = accentBg) {
                    Column(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            dialog.partyName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        Text(
                            dialog.invoiceRef,
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor.copy(0.7f)
                        )
                        Text(
                            "Outstanding: ${state.currencySymbol} ${
                                CurrencyFormatter.formatRs(
                                    dialog.dueAmount
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                }

                // Amount
                OutlinedTextField(
                    value = dialog.amount,
                    onValueChange = { viewModel.onIntent(DuesIntent.SetAmount(it)) },
                    label = { Text("Amount") },
                    prefix = { Text("${state.currencySymbol} ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = BorderColor
                    )
                )

                // Account type toggle
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        AccountType.CASH to "Cash",
                        AccountType.BANK to "Bank"
                    ).forEach { (type, label) ->
                        val sel = dialog.accountType == type
                        FilterChip(
                            selected = sel,
                            onClick = { viewModel.onIntent(DuesIntent.SetAccountType(type)) },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Cash account info / balance
                if (dialog.accountType == AccountType.CASH) {
                    val acc = state.cashAccounts.find { it.id == dialog.accountId }
                    acc?.let {
                        val paid = dialog.amount.toDoubleOrNull() ?: 0.0
                        Text(
                            "Available: ${state.currencySymbol} ${CurrencyFormatter.formatRs(it.balance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (!isReceivable && paid > it.balance) Danger else TextMuted
                        )
                        if (state.cashAccounts.size > 1) {
                            // Simple chip row for multiple cash accounts
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(state.cashAccounts.size) { i ->
                                    val a = state.cashAccounts[i]
                                    FilterChip(
                                        selected = a.id == dialog.accountId,
                                        onClick = { viewModel.onIntent(DuesIntent.SetAccountId(a.id)) },
                                        label = { Text(a.name) })
                                }
                            }
                        }
                    }
                }

                // Bank dropdown
                if (dialog.accountType == AccountType.BANK && state.bankAccounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = showBankDropdown,
                        onExpandedChange = { showBankDropdown = it }) {
                        OutlinedTextField(
                            value = state.bankAccounts.find { it.id == dialog.accountId }?.bankName
                                ?: "Select Bank",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Bank Account") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(
                                    showBankDropdown
                                )
                            },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = showBankDropdown,
                            onDismissRequest = { showBankDropdown = false }) {
                            state.bankAccounts.forEach { bank ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                "${bank.bankName} · ${bank.accountNumber}",
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            val paid = dialog.amount.toDoubleOrNull() ?: 0.0
                                            Text(
                                                "Available: ${state.currencySymbol} ${
                                                    CurrencyFormatter.formatRs(
                                                        bank.balance
                                                    )
                                                }",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (!isReceivable && paid > bank.balance) Danger else TextMuted
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.onIntent(DuesIntent.SetAccountId(bank.id)); showBankDropdown =
                                        false
                                    }
                                )
                            }
                        }
                    }
                }

                dialog.error?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Danger,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.onIntent(DuesIntent.ConfirmPayment) },
                enabled = !state.isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (state.isProcessing) CircularProgressIndicator(
                    Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                else Text(if (isReceivable) "Collect" else "Pay Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(DuesIntent.DismissPayDialog) }) {
                Text(
                    "Cancel"
                )
            }
        }
    )
}