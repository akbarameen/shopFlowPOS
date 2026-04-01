package com.matechmatrix.shopflowpos.feature.installments.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.InstallmentPlan
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.InstallmentFrequency
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.EmptyStateView
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InstallmentsScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : InstallmentsViewModel = koinViewModel()
) {
    val state         by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is InstallmentsEffect.ShowToast) snackbarState.showSnackbar(e.message)
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.onIntent(InstallmentsIntent.ShowAddDialog) },
                containerColor = Primary,
                contentColor   = Color.White,
                shape          = CircleShape
            ) { Icon(Icons.Rounded.Add, "New Plan") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding()
                .padding(horizontal = when (windowSize) {
                    AppWindowSize.EXPANDED -> 28.dp
                    AppWindowSize.MEDIUM   -> 20.dp
                    else                   -> 16.dp
                }, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
//                    Text(
//                        "Installments",
//                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary
//                    )
                    if (!state.isLoading)
                        Text("${state.activeCount} active plans", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (state.overdueCount > 0) {
                        Surface(shape = RoundedCornerShape(8.dp), color = DangerContainer) {
                            Text(
                                "⚠ ${state.overdueCount} overdue",
                                Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall, color = Danger, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    FilterChip(
                        selected = state.showCompleted,
                        onClick  = { viewModel.onIntent(InstallmentsIntent.ToggleShowCompleted(!state.showCompleted)) },
                        label    = { Text("Show Completed") },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary, selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // ── Summary row ────────────────────────────────────────────────────
            if (!state.isLoading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryBox(Modifier.weight(1f), "Outstanding", "${state.currencySymbol} ${CurrencyFormatter.formatCompact(state.totalOutstanding)}", Danger, "⏳")
                    SummaryBox(Modifier.weight(1f), "Collected",   "${state.currencySymbol} ${CurrencyFormatter.formatCompact(state.totalCollected)}", Success, "✅")
                    SummaryBox(Modifier.weight(1f), "Active Plans", "${state.activeCount}", Warning, "📋")
                }
            }

            // ── Content ────────────────────────────────────────────────────────
            when {
                state.isLoading -> LoadingView()
                state.filtered.isEmpty() -> EmptyStateView(
                    icon     = Icons.Rounded.CreditCard,
                    title    = if (state.plans.isEmpty()) "No installment plans" else "No active plans",
                    subtitle = "Tap + to create a payment plan"
                )
                else -> when (windowSize) {
                    AppWindowSize.COMPACT  -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding      = PaddingValues(bottom = 88.dp)
                    ) {
                        items(state.filtered.size, key = { state.filtered[it].id }) { i ->
                            InstallmentCard(state.filtered[i], state.currencySymbol, viewModel)
                        }
                    }
                    else -> LazyVerticalGrid(
                        columns               = GridCells.Adaptive(320.dp),
                        verticalArrangement   = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding        = PaddingValues(bottom = 88.dp)
                    ) {
                        items(state.filtered.size, key = { state.filtered[it].id }) { i ->
                            InstallmentCard(state.filtered[i], state.currencySymbol, viewModel)
                        }
                    }
                }
            }
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────
    if (state.showAddDialog) AddPlanDialog(state = state, viewModel = viewModel)
    state.paymentPlan?.let { PaymentDialog(plan = it, state = state, viewModel = viewModel) }
    state.detailPlan?.let  { PlanDetailSheet(plan = it, state = state, viewModel = viewModel) }
}

// ─────────────────────────────────────────────────────────────────────────────
// Summary box
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryBox(modifier: Modifier, label: String, value: String, color: Color, emoji: String) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = color.copy(0.08f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(emoji, fontSize = 18.sp)
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(0.7f), fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Installment Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InstallmentCard(plan: InstallmentPlan, currency: String, viewModel: InstallmentsViewModel) {
    val now      = Clock.System.now().toEpochMilliseconds()
    val isOverdue = !plan.isCompleted && plan.nextDueDate < now
    val progress = (plan.paidAmount / plan.totalAmount.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)
    val accentColor = when {
        plan.isCompleted -> Success
        isOverdue        -> Danger
        else             -> Warning
    }
    val accentBg = when {
        plan.isCompleted -> SuccessContainer
        isOverdue        -> DangerContainer
        else             -> WarningContainer
    }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(accentBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(plan.customerName.first().uppercase(), style = MaterialTheme.typography.titleSmall, color = accentColor, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(plan.customerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        Text(plan.productName, style = MaterialTheme.typography.labelSmall, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (plan.customerPhone.isNotBlank()) Text(plan.customerPhone, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(shape = RoundedCornerShape(100.dp), color = accentBg) {
                        Text(
                            if (plan.isCompleted) "Completed" else if (isOverdue) "Overdue" else "${plan.paidInstallments}/${plan.totalInstallments}",
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accentColor
                        )
                    }
                    Text(plan.planNumber, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            // Amounts
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmountCell("Total", plan.totalAmount, currency)
                AmountCell("Paid", plan.paidAmount, currency, Success)
                AmountCell("Remaining", plan.remainingAmount, currency, if (plan.isCompleted) Success else accentColor)
                AmountCell("EMI", plan.installmentAmount, currency, Primary)
            }

            // Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress  = { progress },
                    modifier  = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color     = accentColor,
                    trackColor = accentBg
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "Next: ${if (plan.isCompleted) "—" else DateTimeUtils.formatDate(plan.nextDueDate)}",
                        style = MaterialTheme.typography.labelSmall, color = if (isOverdue) Danger else TextMuted
                    )
                    Text("${(progress * 100).toInt()}% paid", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }

            // Actions
            if (!plan.isCompleted) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick        = { viewModel.onIntent(InstallmentsIntent.ShowDetail(plan)) },
                        modifier       = Modifier.weight(1f),
                        shape          = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) { Text("History", style = MaterialTheme.typography.labelMedium) }

                    Button(
                        onClick        = { viewModel.onIntent(InstallmentsIntent.ShowPaymentDialog(plan)) },
                        modifier       = Modifier.weight(1f),
                        shape          = RoundedCornerShape(10.dp),
                        colors         = ButtonDefaults.buttonColors(containerColor = accentColor),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Payment, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Pay EMI", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AmountCell(label: String, amount: Double, currency: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text("$currency ${CurrencyFormatter.formatCompact(amount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentDialog(plan: InstallmentPlan, state: InstallmentsState, viewModel: InstallmentsViewModel) {
    var showBankDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.onIntent(InstallmentsIntent.DismissPaymentDialog) },
        title            = { Text("Record Payment", fontWeight = FontWeight.Bold) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Plan summary
                Surface(shape = RoundedCornerShape(10.dp), color = WarningContainer) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(plan.customerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Warning)
                            Text(plan.productName, style = MaterialTheme.typography.labelSmall, color = Warning.copy(0.7f))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("EMI: ${state.currencySymbol} ${CurrencyFormatter.formatRs(plan.installmentAmount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Warning)
                            Text("Remaining: ${state.currencySymbol} ${CurrencyFormatter.formatRs(plan.remainingAmount)}", style = MaterialTheme.typography.labelSmall, color = Warning.copy(0.7f))
                        }
                    }
                }

                OutlinedTextField(
                    value         = state.paymentAmount,
                    onValueChange = { viewModel.onIntent(InstallmentsIntent.SetPaymentAmount(it)) },
                    label         = { Text("Amount") },
                    prefix        = { Text("${state.currencySymbol} ") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                )

                // Account type
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AccountType.CASH to "Cash", AccountType.BANK to "Bank").forEach { (type, label) ->
                        val sel = state.paymentAccountType == type
                        FilterChip(
                            selected = sel,
                            onClick  = { viewModel.onIntent(InstallmentsIntent.SetPaymentAccountType(type)) },
                            label    = { Text(label) },
                            modifier = Modifier.weight(1f),
                            colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = Warning, selectedLabelColor = Color.White)
                        )
                    }
                }

                // Bank selector
                if (state.paymentAccountType == AccountType.BANK && state.bankAccounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = showBankDropdown, onExpandedChange = { showBankDropdown = it }) {
                        OutlinedTextField(
                            value         = state.bankAccounts.find { it.id == state.paymentAccountId }?.bankName ?: "Select Bank",
                            onValueChange = {}, readOnly = true,
                            label         = { Text("Bank Account") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showBankDropdown) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = showBankDropdown, onDismissRequest = { showBankDropdown = false }) {
                            state.bankAccounts.forEach { bank ->
                                DropdownMenuItem(
                                    text    = { Column { Text(bank.bankName, fontWeight = FontWeight.SemiBold); Text(bank.accountNumber, style = MaterialTheme.typography.labelSmall, color = TextMuted) } },
                                    onClick = { viewModel.onIntent(InstallmentsIntent.SetPaymentAccountId(bank.id)); showBankDropdown = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { viewModel.onIntent(InstallmentsIntent.RecordPayment) },
                enabled  = !state.isPaymentProcessing,
                colors   = ButtonDefaults.buttonColors(containerColor = Success),
                shape    = RoundedCornerShape(10.dp)
            ) {
                if (state.isPaymentProcessing) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Record Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(InstallmentsIntent.DismissPaymentDialog) }) { Text("Cancel") } }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Plan Detail Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanDetailSheet(plan: InstallmentPlan, state: InstallmentsState, viewModel: InstallmentsViewModel) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(InstallmentsIntent.DismissDetail) },
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Payment History — ${plan.planNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(plan.customerName, style = MaterialTheme.typography.bodySmall, color = TextMuted)

            if (state.detailPayments.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("No payments recorded yet", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            } else {
                state.detailPayments.forEach { payment ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(SuccessContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Rounded.Payment, null, tint = Success, modifier = Modifier.size(16.dp))
                            }
                            Column {
                                Text(DateTimeUtils.formatDate(payment.paidAt), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text(payment.accountType.display, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                        Text("+${state.currencySymbol} ${CurrencyFormatter.formatRs(payment.amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Success)
                    }
                    HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Plan Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPlanDialog(state: InstallmentsState, viewModel: InstallmentsViewModel) {
    var showCustomerDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.onIntent(InstallmentsIntent.DismissAddDialog) },
        title            = { Text("New Installment Plan", fontWeight = FontWeight.Bold) },
        text             = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.heightIn(max = 480.dp)) {
                // Customer
                item {
                    ExposedDropdownMenuBox(expanded = showCustomerDropdown, onExpandedChange = { showCustomerDropdown = it }) {
                        OutlinedTextField(
                            value         = state.selectedCustomer?.name ?: state.formCustomerName.ifBlank { "Select or type customer" },
                            onValueChange = { viewModel.onIntent(InstallmentsIntent.FormCustomerName(it)) },
                            label         = { Text("Customer Name *") },
                            trailingIcon  = {
                                if (state.customers.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(showCustomerDropdown)
                            },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            singleLine    = true,
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                        if (state.customers.isNotEmpty()) {
                            ExposedDropdownMenu(expanded = showCustomerDropdown, onDismissRequest = { showCustomerDropdown = false }) {
                                DropdownMenuItem(
                                    text    = { Text("Walk-in / Manual", color = TextMuted) },
                                    onClick = { viewModel.onIntent(InstallmentsIntent.SelectCustomer(null)); showCustomerDropdown = false }
                                )
                                state.customers.forEach { c ->
                                    DropdownMenuItem(
                                        text    = { Column { Text(c.name, fontWeight = FontWeight.SemiBold); Text(c.phone, style = MaterialTheme.typography.labelSmall, color = TextMuted) } },
                                        onClick = { viewModel.onIntent(InstallmentsIntent.SelectCustomer(c)); showCustomerDropdown = false }
                                    )
                                }
                            }
                        }
                    }
                }
                if (state.selectedCustomer == null) {
                    item {
                        FormField("Phone", state.formCustomerPhone, KeyboardType.Phone) { viewModel.onIntent(InstallmentsIntent.FormCustomerPhone(it)) }
                    }
                }
                // Product
                item { FormField("Product / Item *", state.formProductName) { viewModel.onIntent(InstallmentsIntent.FormProductName(it)) } }
                item { FormField("IMEI (optional)", state.formImei, KeyboardType.Number) { viewModel.onIntent(InstallmentsIntent.FormImei(it)) } }

                // Financials
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormField("Total Amount *", state.formTotalAmount, KeyboardType.Decimal, Modifier.weight(1f)) { viewModel.onIntent(InstallmentsIntent.FormTotalAmount(it)) }
                        FormField("Down Payment", state.formDownPayment, KeyboardType.Decimal, Modifier.weight(1f)) { viewModel.onIntent(InstallmentsIntent.FormDownPayment(it)) }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormField("# Installments", state.formInstallments, KeyboardType.Number, Modifier.weight(1f)) { viewModel.onIntent(InstallmentsIntent.FormInstallments(it)) }
                        // Computed EMI preview
                        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = PrimaryContainer) {
                            Column(Modifier.padding(12.dp)) {
                                Text("EMI Amount", style = MaterialTheme.typography.labelSmall, color = Primary)
                                Text("${state.currencySymbol} ${CurrencyFormatter.formatRs(state.computedInstallmentAmount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Primary)
                            }
                        }
                    }
                }

                // Frequency selector
                item {
                    Text("Frequency", style = MaterialTheme.typography.labelMedium, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InstallmentFrequency.entries.forEach { freq ->
                            val sel = state.formFrequency == freq
                            FilterChip(
                                selected = sel,
                                onClick  = { viewModel.onIntent(InstallmentsIntent.FormFrequency(freq)) },
                                label    = { Text(freq.display, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
                item { FormField("Notes (optional)", state.formNotes) { viewModel.onIntent(InstallmentsIntent.FormNotes(it)) } }
                state.formError?.let { item { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) } }
            }
        },
        confirmButton = {
            Button(
                onClick  = { viewModel.onIntent(InstallmentsIntent.SavePlan) },
                enabled  = !state.isSaving,
                colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                shape    = RoundedCornerShape(10.dp)
            ) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Create Plan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(InstallmentsIntent.DismissAddDialog) }) { Text("Cancel") } }
    )
}

@Composable
private fun FormField(
    label   : String, value: String,
    keyboard: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onChange,
        label           = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine      = true,
        modifier        = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape           = RoundedCornerShape(10.dp),
        colors          = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
    )
}