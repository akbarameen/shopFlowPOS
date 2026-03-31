package com.matechmatrix.shopflowpos.feature.ledger.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CashAccount
import com.matechmatrix.shopflowpos.core.model.LedgerEntry
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.LedgerEntryType
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LedgerScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : LedgerViewModel = koinViewModel()
) {
    val state         by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is LedgerEffect.ShowToast) snackbarState.showSnackbar(e.message)
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Transfer FAB
                SmallFloatingActionButton(
                    onClick        = { viewModel.onIntent(LedgerIntent.ShowTransferDialog) },
                    containerColor = Info,
                    contentColor   = Color.White,
                    shape          = CircleShape
                ) { Icon(Icons.Rounded.SwapHoriz, "Transfer") }

                // Add bank account FAB
                FloatingActionButton(
                    onClick        = { viewModel.onIntent(LedgerIntent.ShowAddBankDialog) },
                    containerColor = Primary,
                    contentColor   = Color.White,
                    shape          = CircleShape
                ) { Icon(Icons.Rounded.Add, "Add Bank") }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { LoadingView() }
            return@Scaffold
        }

        if (windowSize == AppWindowSize.EXPANDED) {
            Row(Modifier.fillMaxSize().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
                    item { TotalBalanceCard(state) }
                    item { CashAccountsSection(state, viewModel) }
                    item { BankAccountsSection(state, viewModel) }
                }
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                    item { Text("Transaction History (30 days)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary) }
                    if (state.ledgerEntries.isEmpty()) {
                        item {
                            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No transactions yet", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }
                        }
                    }
                    items(state.ledgerEntries.size, key = { state.ledgerEntries[it].id }) { i ->
                        LedgerEntryRow(state.ledgerEntries[i], state.currencySymbol)
                    }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Accounts & Ledger", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        OutlinedButton(
                            onClick = { viewModel.onIntent(LedgerIntent.ShowAddCashDialog) },
                            shape   = RoundedCornerShape(10.dp),
                            border  = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Rounded.Add, null, tint = Primary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Cash A/C", style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { TotalBalanceCard(state) }
                item { CashAccountsSection(state, viewModel) }
                item { BankAccountsSection(state, viewModel) }
                item {
                    Text("Transaction History (30 days)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                if (state.ledgerEntries.isEmpty()) {
                    item {
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No transactions yet", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    }
                }
                items(state.ledgerEntries.take(50).size, key = { state.ledgerEntries[it].id }) { i ->
                    LedgerEntryRow(state.ledgerEntries[i], state.currencySymbol)
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────
    if (state.showCashDialog)    CashAdjustDialog(state = state, viewModel = viewModel)
    if (state.showAddCashDialog) AddCashAccountDialog(state = state, viewModel = viewModel)
    if (state.showBankDialog)    BankAccountDialog(state = state, viewModel = viewModel)
    if (state.showTransferDialog) TransferDialog(state = state, viewModel = viewModel)

    state.showDeleteBankId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(LedgerIntent.ConfirmDeleteBank("")) },
            icon    = { Icon(Icons.Rounded.DeleteForever, null, tint = Danger) },
            title   = { Text("Remove Bank Account?", fontWeight = FontWeight.Bold) },
            text    = { Text("Transaction history is preserved. The account will be deactivated.") },
            confirmButton = {
                Button(onClick = { viewModel.onIntent(LedgerIntent.DeleteBankAccount) }, colors = ButtonDefaults.buttonColors(containerColor = Danger), shape = RoundedCornerShape(10.dp)) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.ConfirmDeleteBank("")) }) { Text("Cancel") } }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Total balance gradient card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TotalBalanceCard(state: LedgerState) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Primary, PrimaryVariant))).padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AccountBalance, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(16.dp))
                Text("Total Liquid Balance", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
            }
            Text(CurrencyFormatter.formatRs(state.totalBalance), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
            HorizontalDivider(color = Color.White.copy(0.2f))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Rounded.Payments, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(14.dp))
                    Text("Cash: ${CurrencyFormatter.formatRs(state.totalCash)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Rounded.CreditCard, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(14.dp))
                    Text("Bank: ${CurrencyFormatter.formatRs(state.totalBank)}", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cash Accounts Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CashAccountsSection(state: LedgerState, viewModel: LedgerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Cash Accounts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        if (state.cashAccounts.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Payments, null, tint = TextMuted, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("No cash accounts", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        } else {
            state.cashAccounts.forEach { acc -> CashAccountCard(acc, state, viewModel) }
        }
    }
}

@Composable
private fun CashAccountCard(account: CashAccount, state: LedgerState, viewModel: LedgerViewModel) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = SuccessContainer), elevation = CardDefaults.cardElevation(0.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(Success.copy(0.18f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Payments, null, tint = Success, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(account.name, style = MaterialTheme.typography.bodySmall, color = Success.copy(0.7f), fontWeight = FontWeight.SemiBold)
                    Text(CurrencyFormatter.formatRs(account.balance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Success)
                }
            }
            Row {
                IconButton(onClick = { viewModel.onIntent(LedgerIntent.ShowCashDialog(account)) }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Rounded.Edit, null, tint = Success, modifier = Modifier.size(16.dp))
                }
                if (account.id != "default_cash") {
                    IconButton(onClick = { viewModel.onIntent(LedgerIntent.ConfirmDeleteBank(account.id)) }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Bank Accounts Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BankAccountsSection(state: LedgerState, viewModel: LedgerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Bank Accounts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
        if (state.bankAccounts.isEmpty()) {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.AccountBalance, null, tint = TextMuted, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.height(6.dp))
                        Text("No bank accounts yet. Tap + to add one.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        } else {
            state.bankAccounts.forEach { acc -> BankAccountCard(acc, state, viewModel) }
        }
    }
}

@Composable
private fun BankAccountCard(account: BankAccount, state: LedgerState, viewModel: LedgerViewModel) {
    val isAdjusting = state.adjustingBankId == account.id
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(InfoContainer), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.AccountBalance, null, tint = Info, modifier = Modifier.size(20.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(account.accountTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(account.bankName, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        if (account.accountNumber.isNotBlank()) Text(account.accountNumber, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        if (!account.iban.isNullOrBlank()) Text("IBAN: ${account.iban}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.onIntent(LedgerIntent.ShowEditBankDialog(account)) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = { viewModel.onIntent(LedgerIntent.ConfirmDeleteBank(account.id)) }, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, null, tint = Danger, modifier = Modifier.size(14.dp))
                    }
                }
            }

            HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)

            // Balance row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Balance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(CurrencyFormatter.formatRs(account.balance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Info)
                }
                Surface(
                    onClick = {
                        if (isAdjusting) viewModel.onIntent(LedgerIntent.CancelAdjustBankBalance)
                        else viewModel.onIntent(LedgerIntent.StartAdjustBankBalance(account.id, account.balance))
                    },
                    shape = RoundedCornerShape(100.dp),
                    color = if (isAdjusting) DangerContainer else InputBgLight
                ) {
                    Text(
                        if (isAdjusting) "Cancel" else "Adjust",
                        Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                        color = if (isAdjusting) Danger else TextSecondary
                    )
                }
            }

            // Inline balance editor
            AnimatedVisibility(isAdjusting) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value         = state.bankBalanceInput,
                        onValueChange = { viewModel.onIntent(LedgerIntent.SetBankBalanceInput(it)) },
                        label         = { Text("New Balance") },
                        prefix        = { Text("Rs. ") },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                    )
                    Button(onClick = { viewModel.onIntent(LedgerIntent.SaveBankBalance) }, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp)) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ledger Entry Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LedgerEntryRow(entry: LedgerEntry, currencySymbol: String) {
    val isCredit = entry.entryType == LedgerEntryType.CREDIT
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                // Use weight(1f) here so this section consumes remaining space
                // without pushing the Column on the right out of view.
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(11.dp))
                        .background(if (isCredit) SuccessContainer else DangerContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isCredit) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                        null,
                        tint = if (isCredit) Success else Danger,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f), // Allow column to shrink/clip text
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis // Ellipsis will now work correctly
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            entry.referenceType.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text("·", style = MaterialTheme.typography.labelSmall, color = TextFaint)
                        Text(
                            DateTimeUtils.formatDate(entry.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // Fixed-width/Intrinsic-width column for the amounts
            Column(
                modifier = Modifier.padding(start = 8.dp), // Add spacing between description and amount
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    "${if (isCredit) "+" else "-"} ${CurrencyFormatter.formatRs(entry.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isCredit) Success else Danger,
                    softWrap = false // Keep amount on one line
                )
                Text(
                    "Bal: ${CurrencyFormatter.formatCompact(entry.balanceAfter)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    softWrap = false
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Dialogs
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CashAdjustDialog(state: LedgerState, viewModel: LedgerViewModel) {
    val acc = state.adjustingCashAccount ?: return
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(LedgerIntent.DismissCashDialog) },
        title = { Text("Adjust: ${acc.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Current: ${state.currencySymbol} ${CurrencyFormatter.formatRs(acc.balance)}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                OutlinedTextField(
                    value = state.cashInputAmount, onValueChange = { viewModel.onIntent(LedgerIntent.SetCashInput(it)) },
                    label = { Text("New Balance") }, prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                )
            }
        },
        confirmButton = { Button(onClick = { viewModel.onIntent(LedgerIntent.SaveCashBalance) }, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp)) { Text("Save") } },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.DismissCashDialog) }) { Text("Cancel") } }
    )
}

@Composable
private fun AddCashAccountDialog(state: LedgerState, viewModel: LedgerViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(LedgerIntent.DismissAddCashDialog) },
        title = { Text("Add Cash Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LedgerFormField("Account Name *", state.addCashName) { viewModel.onIntent(LedgerIntent.SetAddCashName(it)) }
                LedgerFormField("Opening Balance", state.addCashBalance, KeyboardType.Decimal) { viewModel.onIntent(LedgerIntent.SetAddCashBalance(it)) }
                state.addCashError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
            }
        },
        confirmButton = { Button(onClick = { viewModel.onIntent(LedgerIntent.SaveAddCashAccount) }, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp)) { Text("Add") } },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.DismissAddCashDialog) }) { Text("Cancel") } }
    )
}

@Composable
private fun BankAccountDialog(state: LedgerState, viewModel: LedgerViewModel) {
    val isEditing = state.editingBank != null
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(LedgerIntent.DismissBankDialog) },
        title = { Text(if (isEditing) "Edit Bank Account" else "Add Bank Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LedgerFormField("Account Title *",   state.bankFormTitle)   { viewModel.onIntent(LedgerIntent.BankFormTitle(it)) }
                LedgerFormField("Bank Name *",        state.bankFormBankName) { viewModel.onIntent(LedgerIntent.BankFormBankName(it)) }
                LedgerFormField("Account Number",     state.bankFormAccNum)  { viewModel.onIntent(LedgerIntent.BankFormAccNum(it)) }
                LedgerFormField("IBAN (optional)",    state.bankFormIban)    { viewModel.onIntent(LedgerIntent.BankFormIban(it)) }
                if (!isEditing) LedgerFormField("Opening Balance", state.bankFormBalance, KeyboardType.Decimal) { viewModel.onIntent(LedgerIntent.BankFormBalance(it)) }
                state.bankFormError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.onIntent(LedgerIntent.SaveBankAccount) }, enabled = !state.isSaving, colors = ButtonDefaults.buttonColors(containerColor = Primary), shape = RoundedCornerShape(10.dp)) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(if (isEditing) "Update" else "Add Account")
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.DismissBankDialog) }) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransferDialog(state: LedgerState, viewModel: LedgerViewModel) {
    var showFromDropdown by remember { mutableStateOf(false) }
    var showToDropdown   by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.onIntent(LedgerIntent.DismissTransferDialog) },
        title = { Text("Transfer Between Accounts", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // From account
                Text("From", style = MaterialTheme.typography.labelMedium, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AccountType.CASH to "Cash", AccountType.BANK to "Bank").forEach { (type, label) ->
                        val sel = state.transferFromType == type
                        FilterChip(selected = sel, onClick = { viewModel.onIntent(LedgerIntent.SetTransferFromType(type)) }, label = { Text(label) }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White))
                    }
                }
                val fromAccounts = if (state.transferFromType == AccountType.CASH) state.cashAccounts else state.bankAccounts
                if (fromAccounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = showFromDropdown, onExpandedChange = { showFromDropdown = it }) {
                        OutlinedTextField(
                            value         = fromAccounts.find {
                                when(it) {
                                    is CashAccount -> it.id == state.transferFromId
                                    is BankAccount -> it.id == state.transferFromId
                                    else -> false
                                }
                            }?.let { if (it is CashAccount) it.name else (it as BankAccount).bankName } ?: "Select Account",
                            onValueChange = {}, readOnly = true,
                            label         = { Text("Refund From") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showFromDropdown) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = showFromDropdown, onDismissRequest = { showFromDropdown = false }) {
                            fromAccounts.forEach { acc ->
                                val (id, label) = if (acc is CashAccount) acc.id to acc.name else (acc as BankAccount).run { id to "$bankName · $accountNumber" }
                                DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.onIntent(LedgerIntent.SetTransferFromId(id)); showFromDropdown = false })
                            }
                        }
                    }
                }

                // To account
                Text("To", style = MaterialTheme.typography.labelMedium, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AccountType.CASH to "Cash", AccountType.BANK to "Bank").forEach { (type, label) ->
                        val sel = state.transferToType == type
                        FilterChip(selected = sel, onClick = { viewModel.onIntent(LedgerIntent.SetTransferToType(type)) }, label = { Text(label) }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Info, selectedLabelColor = Color.White))
                    }
                }
                val toAccounts = if (state.transferToType == AccountType.CASH) state.cashAccounts else state.bankAccounts

                if (toAccounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = showToDropdown, onExpandedChange = { showToDropdown = it }) {
                        OutlinedTextField(
                            value         = toAccounts.find {
                                when(it) {
                                    is CashAccount -> it.id == state.transferFromId
                                    is BankAccount -> it.id == state.transferFromId
                                    else -> false
                                }
                            }?.let { if (it is CashAccount) it.name else (it as BankAccount).bankName } ?: "Select Account",
                            onValueChange = {}, readOnly = true,
                            label         = { Text("Refund From") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showFromDropdown) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Warning, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = showToDropdown, onDismissRequest = { showToDropdown = false }) {
                            toAccounts.forEach { acc ->
                                val (id, label) = if (acc is CashAccount) acc.id to acc.name else (acc as BankAccount).run { id to "$bankName · $accountNumber" }
                                DropdownMenuItem(text = { Text(label) }, onClick = { viewModel.onIntent(LedgerIntent.SetTransferToId(id)); showToDropdown = false })
                            }
                        }
                    }
                }

                LedgerFormField("Amount", state.transferAmount, KeyboardType.Decimal) { viewModel.onIntent(LedgerIntent.SetTransferAmount(it)) }
                LedgerFormField("Notes (optional)", state.transferNotes) { viewModel.onIntent(LedgerIntent.SetTransferNotes(it)) }
                state.transferError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.onIntent(LedgerIntent.ExecuteTransfer) }, enabled = !state.isTransferring, colors = ButtonDefaults.buttonColors(containerColor = Info), shape = RoundedCornerShape(10.dp)) {
                if (state.isTransferring) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else { Icon(Icons.Rounded.SwapHoriz, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Transfer", fontWeight = FontWeight.Bold) }
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.DismissTransferDialog) }) { Text("Cancel") } }
    )
}

@Composable
private fun LedgerFormField(label: String, value: String, keyboard: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
    )
}