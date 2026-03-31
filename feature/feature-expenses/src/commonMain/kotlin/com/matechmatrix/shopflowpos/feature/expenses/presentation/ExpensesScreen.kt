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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow

import com.matechmatrix.shopflowpos.core.ui.components.BadgeChip
import com.matechmatrix.shopflowpos.core.ui.components.EmptyStateView
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

// ─── Category display helpers ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
private val ExpenseCategory.emoji: String
    get() = when (this) {
        ExpenseCategory.RENT      -> "🏠"
        ExpenseCategory.SALARY    -> "👤"
        ExpenseCategory.UTILITIES -> "💡"
        ExpenseCategory.SUPPLIES  -> "📦"
        ExpenseCategory.MARKETING -> "📣"
        ExpenseCategory.REPAIR    -> "🔧"
        ExpenseCategory.TRANSPORT -> "🚗"
        ExpenseCategory.TAXES     -> "🏛️"
        ExpenseCategory.OTHER     -> "📋"
    }

private val ExpenseCategory.containerColor: Color
    @Composable get() = when (this) {
        ExpenseCategory.RENT      -> WarningContainer
        ExpenseCategory.SALARY    -> AccentContainer
        ExpenseCategory.UTILITIES -> PrimaryContainer
        ExpenseCategory.SUPPLIES  -> SuccessContainer
        ExpenseCategory.MARKETING -> PrimaryContainer
        ExpenseCategory.REPAIR    -> DangerContainer
        ExpenseCategory.TRANSPORT -> AccentContainer
        ExpenseCategory.TAXES     -> WarningContainer
        ExpenseCategory.OTHER     -> MaterialTheme.colorScheme.surfaceVariant
    }

private val ExpenseCategory.contentColor: Color
    @Composable get() = when (this) {
        ExpenseCategory.RENT      -> Warning
        ExpenseCategory.SALARY    -> Accent
        ExpenseCategory.UTILITIES -> Primary
        ExpenseCategory.SUPPLIES  -> Success
        ExpenseCategory.MARKETING -> Primary
        ExpenseCategory.REPAIR    -> Danger
        ExpenseCategory.TRANSPORT -> Accent
        ExpenseCategory.TAXES     -> Warning
        ExpenseCategory.OTHER     -> MaterialTheme.colorScheme.onSurfaceVariant
    }

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ExpensesScreen(
    windowSize   : AppWindowSize,
    navigateChild: (String) -> Unit = {},
    viewModel    : ExpensesViewModel = koinViewModel()
) {
    val state         by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is ExpensesEffect.ShowToast -> snackbarState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.onIntent(ExpensesIntent.ShowAddSheet) },
                containerColor = Primary,
                contentColor   = Color.White,
                shape          = CircleShape
            ) { Icon(Icons.Rounded.Add, "Add Expense") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        val hPad = when (windowSize) {
            AppWindowSize.EXPANDED -> 28.dp
            AppWindowSize.MEDIUM   -> 20.dp
            AppWindowSize.COMPACT  -> 16.dp
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = hPad, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        state.dateFilter.label,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
//                    Text(
//                        state.dateFilter.label,
//                        style = MaterialTheme.typography.bodySmall,
//                        color = TextMuted
//                    )
                }
                // Total amount card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = DangerContainer
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total: ",
                            style = MaterialTheme.typography.labelSmall, color = Danger)
                        Text(
                            CurrencyFormatter.formatRs(state.totalAmount),
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color      = Danger
                        )
                    }
                }
            }

            // ── Date filter tabs ─────────────────────────────────────────────
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DateFilter.entries.forEach { filter ->
                    val isSelected = state.dateFilter == filter
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Primary else Color.Transparent)
                            .clickable { viewModel.onIntent(ExpensesIntent.SetDateFilter(filter)) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            filter.label,
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color      = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Category breakdown bar ───────────────────────────────────────
            AnimatedVisibility(state.categoryTotals.isNotEmpty()) {
                CategoryBreakdownRow(state.categoryTotals, state.currencySymbol)
            }

            // ── Category filter chips ────────────────────────────────────────
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding        = PaddingValues(horizontal = 2.dp)
            ) {
                // "All" chip
                item {
                    FilterChip(
                        selected = state.selectedCategory == null,
                        onClick  = { viewModel.onIntent(ExpensesIntent.SetCategoryFilter(null)) },
                        label    = { Text("All", style = MaterialTheme.typography.labelSmall) },
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
                items(ExpenseCategory.entries) { cat ->
                    val isSelected = state.selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick  = {
                            viewModel.onIntent(
                                ExpensesIntent.SetCategoryFilter(
                                    if (isSelected) null else cat
                                )
                            )
                        },
                        label = {
                            Text(
                                "${cat.emoji} ${cat.displayName}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor     = Color.White
                        )
                    )
                }
            }

            // ── Content ──────────────────────────────────────────────────────
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> LoadingView()
                    state.error != null -> EmptyStateView(
                        icon     = Icons.Rounded.ErrorOutline,
                        title    = "Something went wrong",
                        subtitle = state.error ?: "",
                        action   = {
                            TextButton(onClick = { viewModel.onIntent(ExpensesIntent.Load) }) {
                                Text("Retry", color = Primary)
                            }
                        }
                    )
                    state.filtered.isEmpty() -> EmptyStateView(
                        icon     = Icons.Rounded.Receipt,
                        title    = "No expenses",
                        subtitle = if (state.selectedCategory == null)
                            "Tap + to record your first expense"
                        else "No ${state.selectedCategory?.displayName} expenses in this period"
                    )
                    else -> ExpenseList(
                        expenses       = state.filtered,
                        currencySymbol = state.currencySymbol,
                        windowSize     = windowSize,
                        onEdit         = { viewModel.onIntent(ExpensesIntent.ShowEditSheet(it)) },
                        onDelete       = { viewModel.onIntent(ExpensesIntent.ConfirmDelete(it.id)) }
                    )
                }
            }
        }
    }

    // ── Form sheet ───────────────────────────────────────────────────────────
    if (state.showFormSheet) {
        ExpenseFormSheet(state, viewModel)
    }

    // ── Delete confirm ───────────────────────────────────────────────────────
    state.showDeleteId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(ExpensesIntent.ConfirmDelete("")) },
            icon             = { Icon(Icons.Rounded.DeleteOutline, null, tint = Danger) },
            title            = { Text("Delete Expense?", fontWeight = FontWeight.Bold) },
            text             = { Text("The deducted amount will be reversed from the account balance.") },
            confirmButton    = {
                Button(
                    onClick = { viewModel.onIntent(ExpensesIntent.DeleteExpense) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Delete") }
            },
            dismissButton    = {
                TextButton(onClick = { viewModel.onIntent(ExpensesIntent.ConfirmDelete("")) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─── Category breakdown row ───────────────────────────────────────────────────

@Composable
private fun CategoryBreakdownRow(
    totals         : List<CategoryTotal>,
    currencySymbol : String,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(horizontal = 2.dp)
    ) {
        items(totals) { item ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = item.category.containerColor
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(item.category.emoji, fontSize = 14.sp)
                    Column {
                        Text(
                            item.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = item.category.contentColor
                        )
                        Text(
                            CurrencyFormatter.formatCompact(item.total),
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color      = item.category.contentColor
                        )
                    }
                }
            }
        }
    }
}

// ─── Expense list ─────────────────────────────────────────────────────────────

@Composable
private fun ExpenseList(
    expenses       : List<Expense>,
    currencySymbol : String,
    windowSize     : AppWindowSize,
    onEdit         : (Expense) -> Unit,
    onDelete       : (Expense) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(bottom = 88.dp)
    ) {
        items(expenses, key = { it.id }) { expense ->
            ExpenseCard(expense, currencySymbol, onEdit, onDelete)
        }
    }
}

// ─── Expense card ─────────────────────────────────────────────────────────────

@Composable
private fun ExpenseCard(
    expense        : Expense,
    currencySymbol : String,
    onEdit         : (Expense) -> Unit,
    onDelete       : (Expense) -> Unit,
) {
    Card(
        Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category icon box
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(12.dp))
                    .background(expense.category.containerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.category.emoji, fontSize = 20.sp)
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    expense.title,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    expense.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                // Account type badge
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        if (expense.accountType == AccountType.CASH) Icons.Rounded.Payments
                        else Icons.Rounded.AccountBalance,
                        null, tint = TextMuted, modifier = Modifier.size(12.dp)
                    )
                    Text(
                        expense.accountType.display,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                // Receipt ref
                expense.receiptRef?.let {
                    Text("Ref: $it", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                // Date
                Text(
                    formatEpoch(expense.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "$currencySymbol ${CurrencyFormatter.formatRs(expense.amount)}",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color      = Danger
                )
                Row {
                    IconButton(onClick = { onEdit(expense) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onDelete(expense) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, null, tint = Danger, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ─── Date formatter ───────────────────────────────────────────────────────────

@Composable
private fun formatEpoch(epochMs: Long): String {
    val tz   = TimeZone.currentSystemDefault()
    val dt   = Instant.fromEpochMilliseconds(epochMs).toLocalDateTime(tz)
    val month = dt.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    return "$month ${dt.dayOfMonth}, ${dt.year}"
}

// ─────────────────────────────────────────────────────────────────────────────
// EXPENSE FORM BOTTOM SHEET
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseFormSheet(
    state    : ExpensesState,
    viewModel: ExpensesViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing  = state.editingExpense != null

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(ExpensesIntent.DismissSheet) },
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle       = {
            Box(
                Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier.size(width = 36.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(0.12f))
                )
            }
        }
    ) {
        LazyColumn(
            contentPadding      = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isEditing) "Edit Expense" else "Record Expense",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = TextPrimary
                        )
                        Text(
                            if (isEditing) "Update expense details" else "Fill in the expense information",
                            style = MaterialTheme.typography.bodySmall, color = TextMuted
                        )
                    }
                    IconButton(
                        onClick  = { viewModel.onIntent(ExpensesIntent.DismissSheet) },
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) { Icon(Icons.Rounded.Close, "Close", modifier = Modifier.size(18.dp)) }
                }
            }

            // ── Category selector ────────────────────────────────────────────
            item {
                ExpFormSection("Category") {
                    ExpenseCategoryGrid(
                        selected = state.formCategory,
                        onSelect = { viewModel.onIntent(ExpensesIntent.FormCategory(it)) }
                    )
                }
            }

            // ── Details ──────────────────────────────────────────────────────
            item {
                ExpFormSection("Expense Details") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExpTextField(
                            label       = "Title *",
                            value       = state.formTitle,
                            placeholder = "e.g. Monthly Shop Rent",
                            leadingIcon = Icons.Rounded.Title
                        ) { viewModel.onIntent(ExpensesIntent.FormTitle(it)) }

                        ExpTextField(
                            label        = "Amount *",
                            value        = state.formAmount,
                            placeholder  = "0",
                            leadingText  = "Rs.",
                            keyboardType = KeyboardType.Decimal
                        ) { viewModel.onIntent(ExpensesIntent.FormAmount(it)) }

                        ExpTextField(
                            label       = "Receipt / Voucher Ref (optional)",
                            value       = state.formReceiptRef,
                            placeholder = "e.g. RCPT-001"
                        ) { viewModel.onIntent(ExpensesIntent.FormReceiptRef(it)) }
                    }
                }
            }

            // ── Payment account ──────────────────────────────────────────────
            item {
                ExpFormSection("Paid From") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // CASH / BANK toggle
                        Row(
                            Modifier.fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            listOf(AccountType.CASH, AccountType.BANK).forEach { type ->
                                val isSelected = state.formAccountType == type
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) Primary else Color.Transparent)
                                        .clickable {
                                            viewModel.onIntent(ExpensesIntent.FormAccountType(type))
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment     = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            if (type == AccountType.CASH) Icons.Rounded.Payments
                                            else Icons.Rounded.AccountBalance,
                                            null,
                                            modifier = Modifier.size(16.dp),
                                            tint = if (isSelected) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            type.display,
                                            style      = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color      = if (isSelected) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Bank account dropdown (only when BANK is selected)
                        if (state.formAccountType == AccountType.BANK) {
                            if (state.bankAccounts.isEmpty()) {
                                Row(
                                    Modifier.fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(WarningContainer)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Warning, null,
                                        tint = Warning, modifier = Modifier.size(16.dp))
                                    Text(
                                        "No bank accounts configured. Add one in Settings.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Warning
                                    )
                                }
                            } else {
                                var expanded by remember { mutableStateOf(false) }
                                val selected = state.bankAccounts.find { it.id == state.formAccountId }
                                ExposedDropdownMenuBox(
                                    expanded        = expanded,
                                    onExpandedChange = { expanded = it }
                                ) {
                                    OutlinedTextField(
                                        value         = selected?.let { "${it.bankName} – ${it.accountTitle}" } ?: "Select bank account",
                                        onValueChange = {},
                                        readOnly      = true,
                                        label         = { Text("Bank Account", style = MaterialTheme.typography.labelSmall) },
                                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                                        modifier      = Modifier.fillMaxWidth().menuAnchor(),
                                        shape         = RoundedCornerShape(12.dp),
                                        colors        = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor   = Primary,
                                            unfocusedBorderColor = BorderColor,
                                            focusedLabelColor    = Primary
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded        = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        state.bankAccounts.forEach { bank ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(bank.bankName, fontWeight = FontWeight.SemiBold)
                                                        Text(bank.accountTitle,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = TextMuted)
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.onIntent(ExpensesIntent.FormAccountId(bank.id))
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Notes ────────────────────────────────────────────────────────
            item {
                ExpFormSection("Notes (optional)") {
                    ExpTextField(
                        label       = "",
                        value       = state.formNotes,
                        placeholder = "Additional details…",
                        singleLine  = false,
                        minLines    = 3
                    ) { viewModel.onIntent(ExpensesIntent.FormNotes(it)) }
                }
            }

            // ── Error ────────────────────────────────────────────────────────
            state.formError?.let { error ->
                item {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(DangerContainer).padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, null,
                            tint = Danger, modifier = Modifier.size(16.dp))
                        Text(error, style = MaterialTheme.typography.bodySmall, color = Danger)
                    }
                }
            }

            // ── Submit ───────────────────────────────────────────────────────
            item {
                Button(
                    onClick  = { viewModel.onIntent(ExpensesIntent.SaveExpense) },
                    enabled  = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (isEditing) Icons.Rounded.Check else Icons.Rounded.Add,
                                null, modifier = Modifier.size(18.dp)
                            )
                            Text(
                                if (isEditing) "Update Expense" else "Record Expense",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Category grid (pill style, 3-col) ───────────────────────────────────────

@Composable
private fun ExpenseCategoryGrid(
    selected : ExpenseCategory,
    onSelect : (ExpenseCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExpenseCategory.entries.chunked(3).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { cat ->
                    val isSelected   = selected == cat
                    val bgColor      = if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant
                    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .clickable { onSelect(cat) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(cat.emoji, fontSize = 18.sp)
                            Text(
                                cat.displayName,
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color      = contentColor,
                                maxLines   = 1
                            )
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

// ─── Shared primitives ────────────────────────────────────────────────────────

@Composable
private fun ExpFormSection(
    title   : String,
    content : @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (title.isNotBlank()) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier.size(width = 3.dp, height = 14.dp)
                        .clip(RoundedCornerShape(2.dp)).background(Primary)
                )
                Text(title, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }
        content()
    }
}

@Composable
private fun ExpTextField(
    label        : String,
    value        : String,
    placeholder  : String         = "",
    modifier     : Modifier       = Modifier.fillMaxWidth(),
    leadingIcon  : ImageVector?   = null,
    leadingText  : String?        = null,
    keyboardType : KeyboardType   = KeyboardType.Text,
    singleLine   : Boolean        = true,
    minLines     : Int            = 1,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        label         = if (label.isNotBlank()) ({ Text(label, style = MaterialTheme.typography.labelSmall) }) else null,
        placeholder   = { Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodySmall) },
        leadingIcon   = when {
            leadingIcon != null -> ({ Icon(leadingIcon, null, tint = TextMuted, modifier = Modifier.size(18.dp)) })
            leadingText != null -> ({ Text(leadingText, style = MaterialTheme.typography.bodySmall, color = TextMuted, fontWeight = FontWeight.SemiBold) })
            else                -> null
        },
        singleLine    = singleLine,
        minLines      = minLines,
        shape         = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Primary,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor    = Primary,
            cursorColor          = Primary
        )
    )
}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ExpensesScreen(
//    windowSize    : AppWindowSize,
//    navigateChild : (String) -> Unit = {},
//    viewModel     : ExpensesViewModel = koinViewModel()
//) {
//    val state by viewModel.state.collectAsStateWithLifecycle()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    LaunchedEffect(Unit) {
//        viewModel.effect.collect { e -> if (e is ExpensesEffect.Toast) snackbarHostState.showSnackbar(e.msg) }
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        floatingActionButton = {
//            FloatingActionButton(
//                onClick = { viewModel.onIntent(ExpensesIntent.ShowAddDialog) },
//                containerColor = Danger, contentColor = Color.White,
//                shape = androidx.compose.foundation.shape.CircleShape
//            ) { Icon(Icons.Rounded.Add, "Add Expense") }
//        },
//        containerColor = MaterialTheme.colorScheme.background
//    ) {
//        Column(
//            Modifier.fillMaxSize().padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            // ── Header row ──
////            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
////                Text(
////                    "Expenses",
////                    fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
////                    letterSpacing = (-0.4).sp, color = MaterialTheme.colorScheme.onBackground
////                )
////            }
//
//            // ── Date filter chips ──
//            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                items(ExpDateFilter.entries) { f ->
//                    Surface(
//                        onClick = { viewModel.onIntent(ExpensesIntent.SetFilter(f)) },
//                        shape = RoundedCornerShape(100.dp),
//                        color = if (state.dateFilter == f) Danger else MaterialTheme.colorScheme.surface,
//                        border = if (state.dateFilter == f) null else BorderStroke(1.5.dp, BorderColor)
//                    ) {
//                        Text(
//                            f.name.lowercase().replaceFirstChar { it.uppercase() },
//                            Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
//                            style = MaterialTheme.typography.labelMedium,
//                            fontWeight = FontWeight.SemiBold,
//                            color = if (state.dateFilter == f) Color.White else TextSecondary
//                        )
//                    }
//                }
//            }
//
//            // ── Total expenses banner ──
//            if (!state.isLoading) {
//                Row(
//                    Modifier.fillMaxWidth()
//                        .clip(RoundedCornerShape(16.dp))
//                        .background(DangerContainer)
//                        .padding(16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment     = Alignment.CenterVertically
//                ) {
//                    Column {
//                        Text("Total Expenses", style = MaterialTheme.typography.bodySmall, color = Danger.copy(0.7f), fontWeight = FontWeight.SemiBold)
//                        Text(
//                            CurrencyFormatter.formatRs(state.totalExpenses),
//                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Danger
//                        )
//                    }
//                    Surface(shape = RoundedCornerShape(100.dp), color = Danger.copy(0.15f)) {
//                        Text(
//                            "${state.expenses.size} entries",
//                            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
//                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Danger
//                        )
//                    }
//                }
//
//                if (state.categoryTotals.isNotEmpty()) {
//                    CategoryBreakdown(state.categoryTotals, state.totalExpenses)
//                }
//            }
//
//            when {
//                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator(color = Danger)
//                }
//                state.expenses.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        Box(
//                            Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(DangerContainer),
//                            contentAlignment = Alignment.Center
//                        ) { Icon(Icons.Rounded.Money, null, tint = Danger, modifier = Modifier.size(32.dp)) }
//                        Text("No expenses recorded", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
//                        Text("Tap + to log an expense", style = MaterialTheme.typography.bodySmall, color = TextMuted)
//                    }
//                }
//                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    items(state.expenses, key = { it.id }) { expense ->
//                        ExpenseCard(expense, state.currencySymbol) {
//                            viewModel.onIntent(ExpensesIntent.DeleteExpense(expense.id))
//                        }
//                    }
//                    item { Spacer(Modifier.height(80.dp)) }
//                }
//            }
//        }
//    }
//
//    // ── Add expense dialog ──
//    if (state.showAddDialog) {
//        AlertDialog(
//            onDismissRequest = { viewModel.onIntent(ExpensesIntent.DismissDialog) },
//            title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
//            text = {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    var catExpanded by remember { mutableStateOf(false) }
//                    ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
//                        OutlinedTextField(
//                            value = state.formCategory.name.lowercase().replaceFirstChar { it.uppercase() },
//                            onValueChange = {}, readOnly = true, label = { Text("Category") },
//                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
//                            modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
//                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
//                        )
//                        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
//                            ExpenseCategory.entries.forEach { cat ->
//                                DropdownMenuItem(
//                                    text    = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
//                                    onClick = { viewModel.onIntent(ExpensesIntent.FormCategory(cat)); catExpanded = false }
//                                )
//                            }
//                        }
//                    }
//                    OutlinedTextField(
//                        value = state.formAmount,
//                        onValueChange = { viewModel.onIntent(ExpensesIntent.FormAmount(it)) },
//                        label = { Text("Amount") }, prefix = { Text("Rs. ") },
//                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                        singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
//                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
//                    )
//                    OutlinedTextField(
//                        value = state.formDescription,
//                        onValueChange = { viewModel.onIntent(ExpensesIntent.FormDescription(it)) },
//                        label = { Text("Description *") }, singleLine = true,
//                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
//                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
//                    )
//
//                    // Payment Method (Cash or Bank)
//                    Text("Pay From", style = MaterialTheme.typography.labelMedium, color = TextMuted)
//                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                        AccountType.entries.forEach { type ->
//                            FilterChip(
//                                selected = state.formAccountType == type,
//                                onClick = { viewModel.onIntent(ExpensesIntent.FormAccountType(type)) },
//                                label = { Text(type.name) },
//                                modifier = Modifier.weight(1f),
//                                shape = RoundedCornerShape(8.dp)
//                            )
//                        }
//                    }
//
//                    if (state.formAccountType == AccountType.BANK) {
//                        var bankExpanded by remember { mutableStateOf(false) }
//                        ExposedDropdownMenuBox(expanded = bankExpanded, onExpandedChange = { bankExpanded = it }) {
//                            OutlinedTextField(
//                                value = state.bankAccounts.find { it.id == state.formBankAccountId }?.accountTitle ?: "Select Bank",
//                                onValueChange = {}, readOnly = true, label = { Text("Bank Account") },
//                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bankExpanded) },
//                                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
//                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
//                            )
//                            ExposedDropdownMenu(expanded = bankExpanded, onDismissRequest = { bankExpanded = false }) {
//                                state.bankAccounts.forEach { acc ->
//                                    DropdownMenuItem(
//                                        text    = { Text("${acc.accountTitle} (${acc.bankName})") },
//                                        onClick = { viewModel.onIntent(ExpensesIntent.FormBankAccountId(acc.id)); bankExpanded = false }
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    state.formError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
//                }
//            },
//            confirmButton = {
//                Button(
//                    onClick = { viewModel.onIntent(ExpensesIntent.SaveExpense) },
//                    colors = ButtonDefaults.buttonColors(containerColor = Danger),
//                    shape = RoundedCornerShape(10.dp)
//                ) { Text("Add Expense") }
//            },
//            dismissButton = { TextButton(onClick = { viewModel.onIntent(ExpensesIntent.DismissDialog) }) { Text("Cancel") } }
//        )
//    }
//}
//
//@Composable
//private fun CategoryBreakdown(totals: Map<ExpenseCategory, Double>, total: Double) {
//    Card(
//        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
//        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//    ) {
//        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
//            Text("By Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextMuted)
//            totals.entries.sortedByDescending { it.value }.take(5).forEach { (cat, amt) ->
//                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                        Text(
//                            cat.name.lowercase().replaceFirstChar { it.uppercase() },
//                            style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium
//                        )
//                        Text(CurrencyFormatter.formatRs(amt), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Danger)
//                    }
//                    if (total > 0) {
//                        LinearProgressIndicator(
//                            progress = { (amt / total).toFloat().coerceIn(0f, 1f) },
//                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
//                            color = Danger, trackColor = DangerContainer
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun ExpenseCard(expense: Expense, currencySymbol: String, onDelete: () -> Unit) {
//    Card(
//        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
//        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
//    ) {
//        Row(
//            Modifier.fillMaxWidth().padding(12.dp, 10.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment     = Alignment.CenterVertically
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                Box(
//                    Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(DangerContainer),
//                    contentAlignment = Alignment.Center
//                ) { Icon(Icons.Rounded.MoneyOff, null, tint = Danger, modifier = Modifier.size(18.dp)) }
//                Column {
//                    Text(
//                        expense.title,
//                        style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary
//                    )
//                    Text(
//                        "${expense.category.name.lowercase().replaceFirstChar { it.uppercase() }} • ${expense.accountType} • ${DateTimeUtils.formatDate(expense.createdAt)}",
//                        style = MaterialTheme.typography.labelSmall, color = TextMuted
//                    )
//                }
//            }
//            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                Text(
//                    CurrencyFormatter.formatRs(expense.amount),
//                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Danger
//                )
//                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
//                    Icon(Icons.Rounded.Delete, null, tint = TextMuted, modifier = Modifier.size(15.dp))
//                }
//            }
//        }
//    }
//}
