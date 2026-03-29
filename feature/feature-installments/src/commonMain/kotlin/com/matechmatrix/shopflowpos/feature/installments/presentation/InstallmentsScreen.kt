package com.matechmatrix.shopflowpos.feature.installments.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.Installment
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InstallmentsScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : InstallmentsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e -> if (e is InstallmentsEffect.Toast) snackbarHostState.showSnackbar(e.msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onIntent(InstallmentsIntent.ShowAddDialog) },
                containerColor = Primary, contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) { Icon(Icons.Rounded.Add, "New Installment") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Page header ──
//            Text(
//                "Installments",
//                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
//                letterSpacing = (-0.4).sp, color = MaterialTheme.colorScheme.onBackground
//            )

            // ── Summary stat boxes ──
            if (!state.isLoading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryStatBox(
                        modifier = Modifier.weight(1f),
                        value    = "${state.activeCount}",
                        label    = "Active Plans",
                        color    = Warning,
                        iconText = "📋"
                    )
                    SummaryStatBox(
                        modifier = Modifier.weight(1f),
                        value    = CurrencyFormatter.formatCompact(state.totalPending),
                        label    = "Total Pending",
                        color    = Danger,
                        iconText = "⏳"
                    )
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.installments.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.CreditCard, null, tint = Primary, modifier = Modifier.size(32.dp)) }
                        Text("No installment plans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Tap + to create a payment plan", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.installments, key = { it.id }) { i ->
                        InstallmentCard(i, state.currencySymbol, viewModel)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ── New installment dialog ──
    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(InstallmentsIntent.DismissDialog) },
            title = { Text("New Installment Plan", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple("Customer Name *", state.formCustomerName) { v: String -> viewModel.onIntent(InstallmentsIntent.FormCustomerName(v)) },
                        Triple("Item / Description", state.formDescription) { v: String -> viewModel.onIntent(InstallmentsIntent.FormDescription(v)) },
                        Triple("Notes", state.formNotes) { v: String -> viewModel.onIntent(InstallmentsIntent.FormNotes(v)) }
                    ).forEach { (label, value, onChange) ->
                        OutlinedTextField(
                            value = value, onValueChange = onChange, label = { Text(label) },
                            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.formTotalAmount,
                            onValueChange = { viewModel.onIntent(InstallmentsIntent.FormTotalAmount(it)) },
                            label = { Text("Total Amount") }, prefix = { Text("Rs. ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                        OutlinedTextField(
                            value = state.formInstallmentCount,
                            onValueChange = { viewModel.onIntent(InstallmentsIntent.FormInstallmentCount(it)) },
                            label = { Text("# Months") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                    }
                    state.formError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(InstallmentsIntent.SaveInstallment) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Create Plan") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onIntent(InstallmentsIntent.DismissDialog) }) { Text("Cancel") } }
        )
    }

    // ── Record payment dialog ──
    state.showPaymentDialog?.let { inst ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(InstallmentsIntent.DismissPaymentDialog) },
            title = { Text("Record Payment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(WarningContainer).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(inst.customerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Warning)
                        Text("Remaining: ${CurrencyFormatter.formatRs(inst.remainingAmount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Warning)
                    }
                    OutlinedTextField(
                        value = state.paymentAmount,
                        onValueChange = { viewModel.onIntent(InstallmentsIntent.SetPaymentAmount(it)) },
                        label = { Text("Payment Amount") }, prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Success, unfocusedBorderColor = BorderColor)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(InstallmentsIntent.RecordPayment) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Success),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Record Payment") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onIntent(InstallmentsIntent.DismissPaymentDialog) }) { Text("Cancel") } }
        )
    }
}

// ─── Summary stat box ─────────────────────────────────────────────────────────
@Composable
private fun SummaryStatBox(modifier: Modifier, value: String, label: String, color: Color, iconText: String) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(0.08f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(iconText, fontSize = 20.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(0.7f), fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Installment card ─────────────────────────────────────────────────────────
@Composable
private fun InstallmentCard(i: Installment, currencySymbol: String, viewModel: InstallmentsViewModel) {
    val isComplete = i.isCompleted
    val paidAmount = i.totalAmount - i.remainingAmount
    val progress   = if (i.totalAmount > 0) (paidAmount / i.totalAmount).toFloat().coerceIn(0f, 1f) else 0f
    val statusColor = if (isComplete) Success else Warning
    val statusBg    = if (isComplete) SuccessContainer else WarningContainer

    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // ── Header row ──
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(WarningContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            i.customerName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleSmall, color = Warning, fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(i.customerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        if (i.productName.isNotBlank()) {
                            Text(i.productName, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
                Surface(shape = RoundedCornerShape(100.dp), color = statusBg) {
                    Text(
                        if (isComplete) "Completed" else "${i.paidMonths}/${i.totalMonths} months",
                        Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor
                    )
                }
            }

            // ── Amounts row ──
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$currencySymbol${CurrencyFormatter.formatCompact(i.totalAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Paid", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text("$currencySymbol${CurrencyFormatter.formatCompact(paidAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Success)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        "$currencySymbol${CurrencyFormatter.formatCompact(i.remainingAmount)}",
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                        color = if (isComplete) Success else Danger
                    )
                }
            }

            // ── Progress bar ──
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = statusColor, trackColor = statusBg
            )

            // ── Actions ──
            if (!isComplete) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { viewModel.onIntent(InstallmentsIntent.DeleteInstallment(i.id)) },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("Delete", style = MaterialTheme.typography.labelSmall, color = Danger) }
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = { viewModel.onIntent(InstallmentsIntent.ShowPaymentDialog(i)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Warning),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.Payment, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Pay $currencySymbol${CurrencyFormatter.formatCompact(i.monthlyAmount)}",
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
