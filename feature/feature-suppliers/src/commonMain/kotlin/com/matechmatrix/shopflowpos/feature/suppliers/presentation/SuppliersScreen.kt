package com.matechmatrix.shopflowpos.feature.suppliers.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SuppliersScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : SuppliersViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val hPad = when (windowSize) {
        AppWindowSize.EXPANDED -> 28.dp
        AppWindowSize.MEDIUM   -> 20.dp
        AppWindowSize.COMPACT  -> 16.dp
    }

    Scaffold(
        floatingActionButton = {
            if (windowSize == AppWindowSize.COMPACT) {
                FloatingActionButton(
                    onClick        = { viewModel.onIntent(SuppliersIntent.ShowAddDialog) },
                    containerColor = Primary,
                    contentColor   = Color.White,
                    shape          = androidx.compose.foundation.shape.CircleShape
                ) { Icon(Icons.Rounded.Add, "Add Supplier") }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = hPad, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Search + Add button row ──────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                TextField(
                    value       = state.searchQuery,
                    onValueChange = { viewModel.onIntent(SuppliersIntent.Search(it)) },
                    modifier    = Modifier.weight(1f),
                    placeholder = { Text("Search suppliers…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)) },
                    singleLine  = true,
                    shape       = RoundedCornerShape(12.dp),
                    colors      = TextFieldDefaults.colors(
                        focusedContainerColor   = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                // On Medium/Expanded show inline Add button instead of FAB
                if (windowSize != AppWindowSize.COMPACT) {
                    Button(
                        onClick = { viewModel.onIntent(SuppliersIntent.ShowAddDialog) },
                        colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape   = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Add Supplier", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ── Main content ─────────────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(AccentContainer),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.LocalShipping, null, tint = Accent, modifier = Modifier.size(32.dp)) }
                        Text("No suppliers yet",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface)
                        Text("Tap + to add your first supplier",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> when (windowSize) {
                    AppWindowSize.COMPACT  -> SupplierCompactList(state, viewModel)
                    AppWindowSize.MEDIUM   -> SupplierMediumGrid(state, viewModel)
                    AppWindowSize.EXPANDED -> SupplierDesktopTable(state, viewModel)
                }
            }
        }
    }

    if (state.showFormDialog) SupplierFormDialog(state, viewModel)

    state.showDeleteId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SuppliersIntent.ConfirmDelete("")) },
            icon = {
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(DangerContainer),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(26.dp))
                }
            },
            title         = { Text("Delete Supplier?", fontWeight = FontWeight.Bold) },
            text          = { Text("This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(SuppliersIntent.DeleteSupplier) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(SuppliersIntent.ConfirmDelete("")) }) { Text("Cancel") }
            }
        )
    }
}

// ── COMPACT ───────────────────────────────────────────────────────────────────
@Composable
private fun SupplierCompactList(state: SuppliersState, viewModel: SuppliersViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 88.dp)
    ) {
        items(state.filtered, key = { it.id }) { s ->
            SupplierCard(s, state.currencySymbol, viewModel)
        }
    }
}

// ── MEDIUM ────────────────────────────────────────────────────────────────────
@Composable
private fun SupplierMediumGrid(state: SuppliersState, viewModel: SuppliersViewModel) {
    LazyVerticalGrid(
        columns               = GridCells.Fixed(2),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding        = PaddingValues(bottom = 24.dp)
    ) {
        items(state.filtered, key = { it.id }) { s ->
            SupplierCard(s, state.currencySymbol, viewModel)
        }
    }
}

// ── EXPANDED — desktop table ──────────────────────────────────────────────────
@Composable
private fun SupplierDesktopTable(state: SuppliersState, viewModel: SuppliersViewModel) {
    Card(
        Modifier.fillMaxSize(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SupTHeader("Supplier",  0.28f)
                SupTHeader("Phone",     0.20f)
                SupTHeader("Email",     0.24f)
                SupTHeader("Balance",   0.16f, TextAlign.End)
                SupTHeader("Actions",   0.12f, TextAlign.Center)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(state.filtered, key = { _, s -> s.id }) { idx, s ->
                    SupplierTableRow(s, state.currencySymbol, idx % 2 == 0, viewModel)
                    HorizontalDivider(
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.5.dp,
                        modifier  = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.SupTHeader(
    label: String, weight: Float,
    align: TextAlign = TextAlign.Start
) {
    Text(
        label.uppercase(),
        modifier      = Modifier.weight(weight),
        style         = MaterialTheme.typography.labelSmall,
        fontWeight    = FontWeight.Bold,
        color         = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.6.sp,
        textAlign     = align
    )
}

@Composable
private fun SupplierTableRow(
    s: Supplier, currencySymbol: String,
    isEven: Boolean, viewModel: SuppliersViewModel
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(
                if (isEven) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name
        Row(
            Modifier.weight(0.28f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(34.dp).clip(RoundedCornerShape(10.dp)).background(AccentContainer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.LocalShipping, null, tint = Accent, modifier = Modifier.size(16.dp)) }
            Text(
                s.name,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onSurface,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }
        // Phone
        Text(
            s.phone ?: "—",
            modifier  = Modifier.weight(0.20f),
            style     = MaterialTheme.typography.bodySmall,
            color     = if (s.phone.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
            else MaterialTheme.colorScheme.onSurface,
            maxLines  = 1
        )
        // Email
        Text(
            s.email ?: "—",
            modifier  = Modifier.weight(0.24f),
            style     = MaterialTheme.typography.bodySmall,
            color     = if (s.email.isNullOrBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f)
            else MaterialTheme.colorScheme.onSurface,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis
        )
        // Balance
        Box(Modifier.weight(0.16f), contentAlignment = Alignment.CenterEnd) {
            if (s.balance > 0) {
                Surface(shape = RoundedCornerShape(100.dp), color = WarningContainer) {
                    Text(
                        CurrencyFormatter.formatRs(s.balance),
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Warning
                    )
                }
            } else {
                Text("—", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f),
                    textAlign = TextAlign.End)
            }
        }
        // Actions
        Row(
            Modifier.weight(0.12f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = { viewModel.onIntent(SuppliersIntent.ShowEditDialog(s)) },
                modifier = Modifier.size(32.dp)
            ) { Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp)) }
            IconButton(
                onClick  = { viewModel.onIntent(SuppliersIntent.ConfirmDelete(s.id)) },
                modifier = Modifier.size(32.dp)
            ) { Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(16.dp)) }
        }
    }
}

// ── Card (Compact & Medium) ───────────────────────────────────────────────────
@Composable
private fun SupplierCard(s: Supplier, currencySymbol: String, viewModel: SuppliersViewModel) {
    Card(
        Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(AccentContainer),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.LocalShipping, null, tint = Accent, modifier = Modifier.size(22.dp)) }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(s.name,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis)
                        if (!s.phone.isNullOrBlank())
                            Text(s.phone, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!s.email.isNullOrBlank())
                            Text(s.email, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row {
                    IconButton(onClick = { viewModel.onIntent(SuppliersIntent.ShowEditDialog(s)) },
                        modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { viewModel.onIntent(SuppliersIntent.ConfirmDelete(s.id)) },
                        modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(16.dp))
                    }
                }
            }
            if (s.balance > 0) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.AccountBalance, null,
                            tint = Warning, modifier = Modifier.size(14.dp))
                        Text("Outstanding",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = RoundedCornerShape(100.dp), color = WarningContainer) {
                        Text(
                            CurrencyFormatter.formatRs(s.balance),
                            Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color      = Warning
                        )
                    }
                }
            }
        }
    }
}

// ── Form dialog ───────────────────────────────────────────────────────────────
@Composable
private fun SupplierFormDialog(state: SuppliersState, viewModel: SuppliersViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(SuppliersIntent.DismissDialog) },
        title = {
            Text(
                if (state.editingSupplier != null) "Edit Supplier" else "Add Supplier",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(
                    Triple("Name *",  state.formName)    { v: String -> viewModel.onIntent(SuppliersIntent.FormName(v)) },
                    Triple("Phone",   state.formPhone)   { v: String -> viewModel.onIntent(SuppliersIntent.FormPhone(v)) },
                    Triple("Email",   state.formEmail)   { v: String -> viewModel.onIntent(SuppliersIntent.FormEmail(v)) },
                    Triple("Address", state.formAddress) { v: String -> viewModel.onIntent(SuppliersIntent.FormAddress(v)) },
                    Triple("Notes",   state.formNotes)   { v: String -> viewModel.onIntent(SuppliersIntent.FormNotes(v)) }
                ).forEach { (label, value, onChange) ->
                    OutlinedTextField(
                        value         = value, onValueChange = onChange,
                        label         = { Text(label) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
                OutlinedTextField(
                    value         = state.formBalance,
                    onValueChange = { viewModel.onIntent(SuppliersIntent.FormBalance(it)) },
                    label         = { Text("Outstanding Balance") },
                    prefix        = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
                state.formError?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Danger)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.onIntent(SuppliersIntent.SaveSupplier) },
                colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                shape   = RoundedCornerShape(10.dp)
            ) { Text(if (state.editingSupplier != null) "Update" else "Add") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(SuppliersIntent.DismissDialog) }) { Text("Cancel") }
        }
    )
}