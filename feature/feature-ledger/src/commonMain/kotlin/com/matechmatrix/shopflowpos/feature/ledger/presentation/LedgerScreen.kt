package com.matechmatrix.shopflowpos.feature.ledger.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.LedgerEntry
import com.matechmatrix.shopflowpos.core.model.enums.TransactionType
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LedgerScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : LedgerViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) { is LedgerEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onIntent(LedgerIntent.ShowAddBankDialog) },
                containerColor = Primary, contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) { Icon(Icons.Rounded.Add, "Add Bank Account") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        if (windowSize == AppWindowSize.EXPANDED) {
            Row(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
//                    PageTitle("Cash & Bank")
                    TotalBalanceCard(state)
                    CashCard(state, viewModel)
                    BankAccountsSection(state, viewModel)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LedgerHistorySection(state)
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { PageTitle("Cash & Bank") }
                item { TotalBalanceCard(state) }
                item { CashCard(state, viewModel) }
                item { BankAccountsSection(state, viewModel) }
                item { LedgerHistorySection(state) }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // ── Cash dialog ──
    if (state.showCashDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(LedgerIntent.DismissCashDialog) },
            title = { Text("Set Cash in Hand", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = state.cashInputAmount,
                    onValueChange = { viewModel.onIntent(LedgerIntent.SetCashInput(it)) },
                    label = { Text("Cash Amount") }, prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(LedgerIntent.SaveCashBalance) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.DismissCashDialog) }) { Text("Cancel") } }
        )
    }

    if (state.showBankDialog) BankAccountDialog(state, viewModel)

    state.showDeleteBankId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(LedgerIntent.ConfirmDeleteBank("")) },
            title = { Text("Delete Bank Account?", fontWeight = FontWeight.Bold) },
            text  = { Text("This action cannot be undone. Transaction history will be kept.") },
            confirmButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.DeleteBankAccount) }) { Text("Delete", color = Danger) } },
            dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.ConfirmDeleteBank("")) }) { Text("Cancel") } }
        )
    }
}

// ─── Page title ───────────────────────────────────────────────────────────────
@Composable
private fun PageTitle(title: String) {
    Text(
        title,
        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-0.4).sp, color = TextPrimary
    )
}

// ─── Total balance gradient card ──────────────────────────────────────────────
@Composable
private fun TotalBalanceCard(state: LedgerState) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Primary, PrimaryVariant)))
            .padding(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AccountBalance, null, tint = Color.White.copy(0.7f), modifier = Modifier.size(18.dp))
                Text("Total Balance", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
            }
            Text(
                CurrencyFormatter.formatRs(state.totalBalance),
                style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White
            )
            HorizontalDivider(color = Color.White.copy(0.2f), thickness = 1.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Rounded.Payments, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(14.dp))
                    Text(
                        "Cash: ${CurrencyFormatter.formatRs(state.cashBalance)}",
                        style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Rounded.CreditCard, null, tint = Color.White.copy(0.6f), modifier = Modifier.size(14.dp))
                    Text(
                        "Bank: ${CurrencyFormatter.formatRs(state.bankAccounts.sumOf { it.balance })}",
                        style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.7f)
                    )
                }
            }
        }
    }
}

// ─── Cash card ────────────────────────────────────────────────────────────────
@Composable
private fun CashCard(state: LedgerState, viewModel: LedgerViewModel) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = SuccessContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(Success.copy(0.18f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Payments, null, tint = Success, modifier = Modifier.size(22.dp)) }
                Column {
                    Text("Cash in Hand", style = MaterialTheme.typography.bodySmall, color = Success.copy(0.7f), fontWeight = FontWeight.SemiBold)
                    Text(
                        CurrencyFormatter.formatRs(state.cashBalance),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Success
                    )
                }
            }
            IconButton(onClick = { viewModel.onIntent(LedgerIntent.ShowCashDialog) }) {
                Icon(Icons.Rounded.Edit, "Edit cash", tint = Success)
            }
        }
    }
}

// ─── Bank accounts section ────────────────────────────────────────────────────
@Composable
private fun BankAccountsSection(state: LedgerState, viewModel: LedgerViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Bank Accounts",
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary
        )
        if (state.bankAccounts.isEmpty()) {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.AccountBalance, null, tint = TextMuted, modifier = Modifier.size(28.dp))
                        Text("No bank accounts added yet", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }
        } else {
            state.bankAccounts.forEach { acct -> BankAccountCard(acct, state, viewModel) }
        }
    }
}

@Composable
private fun BankAccountCard(account: BankAccount, state: LedgerState, viewModel: LedgerViewModel) {
    var showBalanceEdit by remember { mutableStateOf(false) }
    var newBalance by remember { mutableStateOf(account.balance.toString()) }

    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(InfoContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.AccountBalance, null, tint = Info, modifier = Modifier.size(22.dp)) }
                    Column {
                        Text(account.accountTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(account.bankName, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        if (account.accountNumber.isNotBlank()) {
                            Text(account.accountNumber, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.onIntent(LedgerIntent.ShowEditBankDialog(account)) }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { viewModel.onIntent(LedgerIntent.ConfirmDeleteBank(account.id)) }, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(16.dp))
                    }
                }
            }

            HorizontalDivider(color = BorderFaint, thickness = 1.dp)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Balance", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        CurrencyFormatter.formatRs(account.balance),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Info
                    )
                }
                Surface(
                    onClick = { showBalanceEdit = !showBalanceEdit },
                    shape = RoundedCornerShape(100.dp),
                    color = if (showBalanceEdit) PrimaryContainer else InputBgLight
                ) {
                    Text(
                        if (showBalanceEdit) "Cancel" else "Edit Balance",
                        Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                        color = if (showBalanceEdit) Primary else TextSecondary
                    )
                }
            }

            if (showBalanceEdit) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newBalance, onValueChange = { newBalance = it },
                        label = { Text("New Balance") }, prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                    )
                    Button(
                        onClick = {
                            newBalance.toDoubleOrNull()?.let { viewModel.onIntent(LedgerIntent.UpdateBankBalance(account.id, it)) }
                            showBalanceEdit = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape  = RoundedCornerShape(10.dp)
                    ) { Text("Save") }
                }
            }
        }
    }
}

// ─── Ledger history section ───────────────────────────────────────────────────
@Composable
private fun LedgerHistorySection(state: LedgerState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Transaction History (30 days)",
            style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary
        )
        if (state.ledgerEntries.isEmpty()) {
            Card(
                Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                    Text("No transactions yet", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                }
            }
        } else {
            state.ledgerEntries.take(30).forEach { entry -> LedgerEntryRow(entry, state.currencySymbol) }
        }
    }
}

@Composable
private fun LedgerEntryRow(entry: LedgerEntry, currencySymbol: String) {
    val isCredit = entry.type == TransactionType.CREDIT
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
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
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
                Column {
                    Text(
                        entry.description ?: entry.type.name,
                        style = MaterialTheme.typography.bodySmall, color = TextPrimary, maxLines = 1
                    )
                    Text(DateTimeUtils.formatDate(entry.createdAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
            }
            Text(
                "${if (isCredit) "+" else "-"}${CurrencyFormatter.formatRs(entry.amount)}",
                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                color = if (isCredit) Success else Danger
            )
        }
    }
}

// ─── Bank account dialog ──────────────────────────────────────────────────────
@Composable
private fun BankAccountDialog(state: LedgerState, viewModel: LedgerViewModel) {
    val isEditing = state.editingBank != null
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(LedgerIntent.DismissBankDialog) },
        title = { Text(if (isEditing) "Edit Bank Account" else "Add Bank Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BankFormField("Account Name *",  state.bankFormName)      { viewModel.onIntent(LedgerIntent.BankFormName(it)) }
                BankFormField("Bank Name *",      state.bankFormBankName)  { viewModel.onIntent(LedgerIntent.BankFormBankName(it)) }
                BankFormField("Account Number",   state.bankFormAccNumber) { viewModel.onIntent(LedgerIntent.BankFormAccNumber(it)) }
                BankFormField("Opening Balance",  state.bankFormBalance, KeyboardType.Number) { viewModel.onIntent(LedgerIntent.BankFormBalance(it)) }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(
                        checked = state.bankFormIsDefault,
                        onCheckedChange = { viewModel.onIntent(LedgerIntent.BankFormIsDefault(it)) },
                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                    )
                    Text("Set as default account", style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                }
                state.bankFormError?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger) }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.onIntent(LedgerIntent.SaveBankAccount) },
                colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                shape   = RoundedCornerShape(10.dp)
            ) { Text(if (isEditing) "Update" else "Add Account") }
        },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(LedgerIntent.DismissBankDialog) }) { Text("Cancel") } }
    )
}

@Composable
private fun BankFormField(
    label: String, value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
    )
}
