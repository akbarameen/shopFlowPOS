package com.matechmatrix.shopflowpos.feature.customers.presentation

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
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CustomersScreen(
    windowSize: AppWindowSize,
    navigateChild: (String) -> Unit = {},
    viewModel: CustomersViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is CustomersEffect.Toast) snackbarHostState.showSnackbar(
                e.msg
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onIntent(CustomersIntent.ShowAddDialog) },
                containerColor = Primary,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape
            ) { Icon(Icons.Rounded.PersonAdd, "Add Customer") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ──
//            Text(
//                "Customers",
//                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold,
//                letterSpacing = (-0.4).sp, color = MaterialTheme.colorScheme.onBackground
//            )

            // ── HTML-style search ──
            TextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onIntent(CustomersIntent.Search(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search customers...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = InputBgLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )

            // ── Total due banner ──
            if (!state.isLoading && state.totalDue > 0) {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DangerContainer)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            null,
                            tint = Danger,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Total Outstanding Due",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Danger
                        )
                    }
                    Text(
                        CurrencyFormatter.formatRs(state.totalDue),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Danger
                    )
                }
            }

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }

                state.filtered.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Group,
                                null,
                                tint = Primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Text(
                            "No customers found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "Tap + to add your first customer",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.filtered, key = { it.id }) { c ->
                        CustomerCard(c, state.currencySymbol, viewModel)
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (state.showFormDialog) CustomerFormDialog(state, viewModel)

    state.showDueDialog?.let { c ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(CustomersIntent.DismissDueDialog) },
            title = { Text("Collect Due", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(DangerContainer).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            c.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Danger
                        )
                        Text(
                            "Due: ${CurrencyFormatter.formatRs(c.dueBalance)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Danger
                        )
                    }
                    OutlinedTextField(
                        value = state.dueCollectAmount,
                        onValueChange = { viewModel.onIntent(CustomersIntent.SetDueAmount(it)) },
                        label = { Text("Amount Collected") }, prefix = { Text("Rs. ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(CustomersIntent.CollectDue) },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Collect Payment") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(CustomersIntent.DismissDueDialog) }) {
                    Text(
                        "Cancel"
                    )
                }
            }
        )
    }

    state.showDeleteId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(CustomersIntent.ConfirmDelete("")) },
            title = { Text("Delete Customer?", fontWeight = FontWeight.Bold) },
            text = { Text("Their transaction history will be preserved.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(CustomersIntent.DeleteCustomer) }) {
                    Text(
                        "Delete",
                        color = Danger
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onIntent(
                        CustomersIntent.ConfirmDelete(
                            ""
                        )
                    )
                }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CustomerCard(c: Customer, currencySymbol: String, viewModel: CustomersViewModel) {
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar circle with initial
                    Box(
                        Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            c.name.first().uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Primary, fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(
                            c.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (!c.phone.isNullOrBlank()) {
                            Text(
                                c.phone,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
                Row {
                    IconButton(
                        onClick = { viewModel.onIntent(CustomersIntent.ShowEditDialog(c)) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.onIntent(CustomersIntent.ConfirmDelete(c.id)) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            null,
                            tint = Danger,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (c.dueBalance > 0) {
                HorizontalDivider(color = BorderFaint, thickness = 1.dp)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            null,
                            tint = Danger,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "Due: ${CurrencyFormatter.formatRs(c.dueBalance)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Danger,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        onClick = { viewModel.onIntent(CustomersIntent.ShowDueDialog(c)) },
                        shape = RoundedCornerShape(100.dp), color = SuccessContainer
                    ) {
                        Text(
                            "Collect",
                            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerFormDialog(state: CustomersState, viewModel: CustomersViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(CustomersIntent.DismissDialog) },
        title = {
            Text(
                if (state.editingCustomer != null) "Edit Customer" else "Add Customer",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple("Full Name *", state.formName) { v: String ->
                        viewModel.onIntent(
                            CustomersIntent.FormName(v)
                        )
                    },
                    Triple("Phone", state.formPhone) { v: String ->
                        viewModel.onIntent(
                            CustomersIntent.FormPhone(v)
                        )
                    },
                    Triple("Email", state.formEmail) { v: String ->
                        viewModel.onIntent(
                            CustomersIntent.FormEmail(v)
                        )
                    },
                    Triple("Address", state.formAddress) { v: String ->
                        viewModel.onIntent(
                            CustomersIntent.FormAddress(v)
                        )
                    },
                    Triple("Notes", state.formNotes) { v: String ->
                        viewModel.onIntent(
                            CustomersIntent.FormNotes(v)
                        )
                    }
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value = value,
                        onValueChange = onChange,
                        label = { Text(label) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                }
                state.formError?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Danger
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.onIntent(CustomersIntent.SaveCustomer) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) { Text(if (state.editingCustomer != null) "Update" else "Add Customer") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(CustomersIntent.DismissDialog) }) {
                Text(
                    "Cancel"
                )
            }
        }
    )
}