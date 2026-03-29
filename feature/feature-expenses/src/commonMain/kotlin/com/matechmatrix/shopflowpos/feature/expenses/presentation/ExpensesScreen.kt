package com.matechmatrix.shopflowpos.feature.expenses.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.Expense
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.ExpenseCategory
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : ExpensesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e -> if (e is ExpensesEffect.Toast) snackbarHostState.showSnackbar(e.msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onIntent(ExpensesIntent.ShowAddDialog) },
                containerColor = Danger, contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) { Icon(Icons.Rounded.Add, "Add Expense") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header row ──
//            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    "Expenses",
//                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
//                    letterSpacing = (-0.4).sp, color = MaterialTheme.colorScheme.onBackground
//                )
//            }

            // ── Date filter chips ──
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExpDateFilter.entries) { f ->
                    Surface(
                        onClick = { viewModel.onIntent(ExpensesIntent.SetFilter(f)) },
                        shape = RoundedCornerShape(100.dp),
                        color = if (state.dateFilter == f) Danger else MaterialTheme.colorScheme.surface,
                        border = if (state.dateFilter == f) null else BorderStroke(1.5.dp, BorderColor)
                    ) {
                        Text(
                            f.name.lowercase().replaceFirstChar { it.uppercase() },
                            Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (state.dateFilter == f) Color.White else TextSecondary
                        )
                    }
                }
            }

            // ── Total expenses banner ──
            if (!state.isLoading) {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DangerContainer)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Expenses", style = MaterialTheme.typography.bodySmall, color = Danger.copy(0.7f), fontWeight = FontWeight.SemiBold)
                        Text(
                            CurrencyFormatter.formatRs(state.totalExpenses),
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Danger
                        )
                    }
                    Surface(shape = RoundedCornerShape(100.dp), color = Danger.copy(0.15f)) {
                        Text(
                            "${state.expenses.size} entries",
                            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Danger
                        )
                    }
                }

                if (state.categoryTotals.isNotEmpty()) {
                    CategoryBreakdown(state.categoryTotals, state.totalExpenses)
                }
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Danger)
                }
                state.expenses.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(DangerContainer),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.Money, null, tint = Danger, modifier = Modifier.size(32.dp)) }
                        Text("No expenses recorded", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Tap + to log an expense", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.expenses, key = { it.id }) { expense ->
                        ExpenseCard(expense, state.currencySymbol) {
                            viewModel.onIntent(ExpensesIntent.DeleteExpense(expense.id))
                        }
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ── Add expense dialog ──
    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(ExpensesIntent.DismissDialog) },
            title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var catExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                        OutlinedTextField(
                            value = state.formCategory.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {}, readOnly = true, label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            ExpenseCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text    = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = { viewModel.onIntent(ExpensesIntent.FormCategory(cat)); catExpanded = false }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = state.formAmount,
                        onValueChange = { viewModel.onIntent(ExpensesIntent.FormAmount(it)) },
                        label = { Text("Amount") }, prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
                    )
                    OutlinedTextField(
                        value = state.formDescription,
                        onValueChange = { viewModel.onIntent(ExpensesIntent.FormDescription(it)) },
                        label = { Text("Description *") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
                    )

                    // Payment Method (Cash or Bank)
                    Text("Pay From", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccountType.entries.forEach { type ->
                            FilterChip(
                                selected = state.formAccountType == type,
                                onClick = { viewModel.onIntent(ExpensesIntent.FormAccountType(type)) },
                                label = { Text(type.name) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    if (state.formAccountType == AccountType.BANK) {
                        var bankExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = bankExpanded, onExpandedChange = { bankExpanded = it }) {
                            OutlinedTextField(
                                value = state.bankAccounts.find { it.id == state.formBankAccountId }?.accountTitle ?: "Select Bank",
                                onValueChange = {}, readOnly = true, label = { Text("Bank Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bankExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
                            )
                            ExposedDropdownMenu(expanded = bankExpanded, onDismissRequest = { bankExpanded = false }) {
                                state.bankAccounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text    = { Text("${acc.accountTitle} (${acc.bankName})") },
                                        onClick = { viewModel.onIntent(ExpensesIntent.FormBankAccountId(acc.id)); bankExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    state.formError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(ExpensesIntent.SaveExpense) },
                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Add Expense") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onIntent(ExpensesIntent.DismissDialog) }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CategoryBreakdown(totals: Map<ExpenseCategory, Double>, total: Double) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("By Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextMuted)
            totals.entries.sortedByDescending { it.value }.take(5).forEach { (cat, amt) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            cat.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium
                        )
                        Text(CurrencyFormatter.formatRs(amt), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Danger)
                    }
                    if (total > 0) {
                        LinearProgressIndicator(
                            progress = { (amt / total).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = Danger, trackColor = DangerContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(expense: Expense, currencySymbol: String, onDelete: () -> Unit) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(DangerContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.MoneyOff, null, tint = Danger, modifier = Modifier.size(18.dp)) }
                Column {
                    Text(
                        expense.title,
                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary
                    )
                    Text(
                        "${expense.category.name.lowercase().replaceFirstChar { it.uppercase() }} • ${expense.accountType} • ${DateTimeUtils.formatDate(expense.createdAt)}",
                        style = MaterialTheme.typography.labelSmall, color = TextMuted
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    CurrencyFormatter.formatRs(expense.amount),
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Danger
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.Delete, null, tint = TextMuted, modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}
