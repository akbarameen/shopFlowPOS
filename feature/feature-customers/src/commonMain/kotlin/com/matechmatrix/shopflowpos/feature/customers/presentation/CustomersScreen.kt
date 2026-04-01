// ════════════════════════════════════════════════════════════════════════════
// feature/customers/presentation/CustomersScreen.kt
// ════════════════════════════════════════════════════════════════════════════
package com.matechmatrix.shopflowpos.feature.customers.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CashAccount
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.EmptyStateView
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : CustomersViewModel = koinViewModel()
) {
    val state          by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState  = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is CustomersEffect.ShowToast) snackbarState.showSnackbar(e.message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.onIntent(CustomersIntent.ShowAddDialog) },
                containerColor = Primary,
                contentColor   = Color.White,
                shape          = CircleShape
            ) { Icon(Icons.Rounded.PersonAdd, "Add Customer") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
//                    Text(
//                        "Customers",
//                        style      = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold,
//                        color      = TextPrimary
//                    )
                    if (state.customers.isNotEmpty()) {
                        Text(
                            "${state.customers.size} customers",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                // Total outstanding badge
                AnimatedVisibility(state.totalOutstanding > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerContainer
                    ) {
                        Column(
                            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                "Total Due",
                                style = MaterialTheme.typography.labelSmall,
                                color = Danger
                            )
                            Text(
                                CurrencyFormatter.formatRs(state.totalOutstanding),
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color      = Danger
                            )
                        }
                    }
                }
            }

            // ── Search ───────────────────────────────────────────────────────
            TextField(
                value         = state.searchQuery,
                onValueChange = { viewModel.onIntent(CustomersIntent.Search(it)) },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = { Text("Search by name, phone, CNIC…", color = TextMuted) },
                leadingIcon   = {
                    Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                },
                trailingIcon  = if (state.searchQuery.isNotBlank()) ({
                    IconButton(onClick = { viewModel.onIntent(CustomersIntent.Search("")) }) {
                        Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }) else null,
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            // ── Content ───────────────────────────────────────────────────────
            when {
                state.isLoading -> LoadingView()

                state.filtered.isEmpty() -> EmptyStateView(
                    icon     = Icons.Rounded.Group,
                    title    = "No customers found",
                    subtitle = if (state.searchQuery.isBlank()) "Tap + to add your first customer"
                    else "No results for \"${state.searchQuery}\""
                )

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(bottom = 88.dp)
                ) {
                    items(state.filtered, key = { it.id }) { customer ->
                        CustomerCard(
                            customer       = customer,
                            currencySymbol = state.currencySymbol,
                            onEdit         = { viewModel.onIntent(CustomersIntent.ShowEditDialog(customer)) },
                            onDelete       = { viewModel.onIntent(CustomersIntent.ConfirmDelete(customer.id)) },
                            onCollectDue   = { viewModel.onIntent(CustomersIntent.ShowDueDialog(customer)) }
                        )
                    }
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (state.showFormDialog) {
        CustomerFormDialog(state = state, viewModel = viewModel)
    }

    state.showDueDialog?.let { customer ->
        DueCollectionDialog(
            customer       = customer,
            state          = state,
            currencySymbol = state.currencySymbol,
            viewModel      = viewModel
        )
    }

    state.showDeleteId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(CustomersIntent.ConfirmDelete("")) },
            icon     = { Icon(Icons.Rounded.PersonRemove, null, tint = Danger) },
            title    = { Text("Remove Customer?", fontWeight = FontWeight.Bold) },
            text     = { Text("The customer will be deactivated. Their sale history is preserved.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(CustomersIntent.DeleteCustomer) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(CustomersIntent.ConfirmDelete("")) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Customer Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CustomerCard(
    customer       : Customer,
    currencySymbol : String,
    onEdit         : () -> Unit,
    onDelete       : () -> Unit,
    onCollectDue   : () -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: avatar + name/phone + actions
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar
                    Box(
                        modifier         = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            customer.name.first().uppercase(),
                            style      = MaterialTheme.typography.titleMedium,
                            color      = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            customer.name,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onBackground
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(
                                customer.phone,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        if (!customer.city.isNullOrBlank()) {
                            Text(
                                customer.city,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.PersonRemove, null, tint = Danger, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Stats row
            if (customer.totalTransactions > 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LabelValue("Purchases", customer.totalTransactions.toString())
                    LabelValue("Total Spent", "$currencySymbol ${CurrencyFormatter.formatCompact(customer.totalPurchases)}")
                    if (customer.creditLimit > 0) {
                        LabelValue("Credit Limit", "$currencySymbol ${CurrencyFormatter.formatCompact(customer.creditLimit)}")
                    }
                }
            }

            // Due row
            if (customer.outstandingBalance > 0) {
                HorizontalDivider(color = BorderFaint, thickness = 0.5.dp)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Warning, null, tint = Danger, modifier = Modifier.size(14.dp))
                        Text(
                            "Due: $currencySymbol ${CurrencyFormatter.formatRs(customer.outstandingBalance)}",
                            style      = MaterialTheme.typography.labelSmall,
                            color      = Danger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        onClick = onCollectDue,
                        shape   = RoundedCornerShape(100.dp),
                        color   = SuccessContainer
                    ) {
                        Text(
                            "Collect",
                            modifier   = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Success
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Due Collection Dialog (with account selector)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueCollectionDialog(
    customer       : Customer,
    state          : CustomersState,
    currencySymbol : String,
    viewModel      : CustomersViewModel
) {
    var showAccountDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { viewModel.onIntent(CustomersIntent.DismissDueDialog) },
        title            = { Text("Collect Payment", fontWeight = FontWeight.Bold) },
        text             = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Customer + due info
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DangerContainer)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        customer.name,
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = Danger
                    )
                    Text(
                        "Due: $currencySymbol ${CurrencyFormatter.formatRs(customer.outstandingBalance)}",
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = Danger
                    )
                }

                // Amount
                OutlinedTextField(
                    value         = state.dueCollectAmount,
                    onValueChange = { viewModel.onIntent(CustomersIntent.SetDueAmount(it)) },
                    label         = { Text("Amount") },
                    prefix        = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Primary,
                        unfocusedBorderColor = BorderColor
                    )
                )

                // Account type selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    listOf(AccountType.CASH to "Cash", AccountType.BANK to "Bank").forEach { (type, label) ->
                        val selected = state.dueAccountType == type
                        FilterChip(
                            selected  = selected,
                            onClick   = { viewModel.onIntent(CustomersIntent.SetDueAccountType(type)) },
                            label     = { Text(label, style = MaterialTheme.typography.labelSmall) },
                            modifier  = Modifier.weight(1f),
                            colors    = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor     = Color.White
                            )
                        )
                    }
                }

                // Account dropdown (show only when BANK selected and bank accounts exist)
                if (state.dueAccountType == AccountType.BANK && state.bankAccounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded        = showAccountDropdown,
                        onExpandedChange = { showAccountDropdown = it }
                    ) {
                        OutlinedTextField(
                            value         = state.bankAccounts.find { it.id == state.dueAccountId }?.bankName ?: "Select Bank",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Bank Account") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showAccountDropdown) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Primary,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded        = showAccountDropdown,
                            onDismissRequest = { showAccountDropdown = false }
                        ) {
                            state.bankAccounts.forEach { bank ->
                                DropdownMenuItem(
                                    text    = {
                                        Column {
                                            Text(bank.bankName, fontWeight = FontWeight.SemiBold)
                                            Text(bank.accountTitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                        }
                                    },
                                    onClick = {
                                        viewModel.onIntent(CustomersIntent.SetDueAccountId(bank.id))
                                        showAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }
                } else if (state.dueAccountType == AccountType.CASH && state.cashAccounts.size > 1) {
                    // Multiple cash accounts — show selector
                    ExposedDropdownMenuBox(
                        expanded         = showAccountDropdown,
                        onExpandedChange = { showAccountDropdown = it }
                    ) {
                        OutlinedTextField(
                            value         = state.cashAccounts.find { it.id == state.dueAccountId }?.name ?: "Cash",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Cash Account") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(showAccountDropdown) },
                            modifier      = Modifier.fillMaxWidth().menuAnchor(),
                            shape         = RoundedCornerShape(10.dp),
                            colors        = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor   = Primary,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                        ExposedDropdownMenu(
                            expanded         = showAccountDropdown,
                            onDismissRequest = { showAccountDropdown = false }
                        ) {
                            state.cashAccounts.forEach { cash ->
                                DropdownMenuItem(
                                    text    = { Text(cash.name) },
                                    onClick = {
                                        viewModel.onIntent(CustomersIntent.SetDueAccountId(cash.id))
                                        showAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { viewModel.onIntent(CustomersIntent.CollectDue) },
                enabled  = !state.isSaving && state.dueCollectAmount.isNotBlank(),
                colors   = ButtonDefaults.buttonColors(containerColor = Success),
                shape    = RoundedCornerShape(10.dp)
            ) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Collect Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(CustomersIntent.DismissDueDialog) }) {
                Text("Cancel")
            }
        }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Customer Form Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CustomerFormDialog(state: CustomersState, viewModel: CustomersViewModel) {
    val isEditing = state.editingCustomer != null

    AlertDialog(
        onDismissRequest = { viewModel.onIntent(CustomersIntent.DismissDialog) },
        title            = {
            Text(
                if (isEditing) "Edit Customer" else "Add Customer",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Required
                CustomerTextField("Full Name *", state.formName) {
                    viewModel.onIntent(CustomersIntent.FormName(it))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomerTextField("Phone", state.formPhone, Modifier.weight(1f), keyboardType = KeyboardType.Phone) {
                        viewModel.onIntent(CustomersIntent.FormPhone(it))
                    }
                    CustomerTextField("WhatsApp", state.formWhatsapp, Modifier.weight(1f), keyboardType = KeyboardType.Phone) {
                        viewModel.onIntent(CustomersIntent.FormWhatsapp(it))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomerTextField("CNIC", state.formCnic, Modifier.weight(1f), keyboardType = KeyboardType.Number) {
                        if (it.length <= 13) viewModel.onIntent(CustomersIntent.FormCnic(it))
                    }
                    CustomerTextField("Email", state.formEmail, Modifier.weight(1f), keyboardType = KeyboardType.Email) {
                        viewModel.onIntent(CustomersIntent.FormEmail(it))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CustomerTextField("City", state.formCity, Modifier.weight(1f)) {
                        viewModel.onIntent(CustomersIntent.FormCity(it))
                    }
                    CustomerTextField(
                        "Credit Limit", state.formCreditLimit,
                        Modifier.weight(1f),
                        keyboardType = KeyboardType.Decimal
                    ) { viewModel.onIntent(CustomersIntent.FormCreditLimit(it)) }
                }
                CustomerTextField("Address", state.formAddress) {
                    viewModel.onIntent(CustomersIntent.FormAddress(it))
                }
                CustomerTextField("Notes", state.formNotes) {
                    viewModel.onIntent(CustomersIntent.FormNotes(it))
                }

                state.formError?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Danger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = { viewModel.onIntent(CustomersIntent.SaveCustomer) },
                enabled  = !state.isSaving,
                colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                shape    = RoundedCornerShape(10.dp)
            ) {
                if (state.isSaving) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(if (isEditing) "Update" else "Add Customer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(CustomersIntent.DismissDialog) }) { Text("Cancel") }
        }
    )
}

@Composable
private fun CustomerTextField(
    label        : String,
    value        : String,
    modifier     : Modifier = Modifier.fillMaxWidth(),
    keyboardType : KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value          = value,
        onValueChange  = onValueChange,
        label          = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine     = true,
        modifier       = modifier,
        shape          = RoundedCornerShape(10.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors         = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Primary,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor    = Primary
        )
    )
}