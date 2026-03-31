package com.matechmatrix.shopflowpos.feature.suppliers.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.Supplier
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.BadgeChip
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun SuppliersScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : SuppliersViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SuppliersEffect.Toast -> snackbarState.showSnackbar(effect.msg)
            }
        }
    }

    val hPad = when (windowSize) {
        AppWindowSize.EXPANDED -> 28.dp
        AppWindowSize.MEDIUM   -> 20.dp
        AppWindowSize.COMPACT  -> 16.dp
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            if (windowSize == AppWindowSize.COMPACT) {
                FloatingActionButton(
                    onClick        = { viewModel.onIntent(SuppliersIntent.ShowAddSheet) },
                    containerColor = Primary,
                    contentColor   = Color.White,
                    shape          = CircleShape
                ) { Icon(Icons.Rounded.Add, "Add Supplier") }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
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
//                    Text(
//                        "Suppliers",
//                        style      = MaterialTheme.typography.headlineSmall,
//                        fontWeight = FontWeight.Bold,
//                        color      = TextPrimary
//                    )
                    val withDues = state.suppliers.count { it.outstandingBalance > 0 }
                    if (withDues > 0) {
                        Text(
                            "$withDues with outstanding dues",
                            style = MaterialTheme.typography.bodySmall,
                            color = Warning
                        )
                    }
                }
                if (windowSize != AppWindowSize.COMPACT) {
                    Button(
                        onClick = { viewModel.onIntent(SuppliersIntent.ShowAddSheet) },
                        colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape   = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Add Supplier",
                            style      = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // ── Search ──────────────────────────────────────────────────────
            TextField(
                value         = state.searchQuery,
                onValueChange = { viewModel.onIntent(SuppliersIntent.Search(it)) },
                modifier      = Modifier.fillMaxWidth(),
                placeholder   = {
                    Text("Search by name or phone…",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                leadingIcon   = {
                    Icon(Icons.Rounded.Search, null,
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                },
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = TextFieldDefaults.colors(
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // ── Content ──────────────────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.filtered.isEmpty() -> EmptySuppliers()
                else -> when (windowSize) {
                    AppWindowSize.COMPACT  -> SupplierList(state, viewModel)
                    AppWindowSize.MEDIUM   -> SupplierGrid(state, viewModel)
                    AppWindowSize.EXPANDED -> SupplierTable(state, viewModel)
                }
            }
        }
    }

    // ── Form bottom sheet ────────────────────────────────────────────────────
    if (state.showFormSheet) {
        SupplierFormSheet(state, viewModel)
    }

    // ── Delete confirm ───────────────────────────────────────────────────────
    state.showDeleteId?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SuppliersIntent.ConfirmDelete("")) },
            icon = {
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(DangerContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(26.dp)) }
            },
            title         = { Text("Remove Supplier?", fontWeight = FontWeight.Bold) },
            text          = { Text("This will hide the supplier from your list. Purchase history is preserved.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(SuppliersIntent.DeleteSupplier) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape   = RoundedCornerShape(10.dp)
                ) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(SuppliersIntent.ConfirmDelete("")) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ─── List (COMPACT) ───────────────────────────────────────────────────────────

@Composable
private fun SupplierList(state: SuppliersState, viewModel: SuppliersViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(bottom = 88.dp)
    ) {
        items(state.filtered, key = { it.id }) { s ->
            SupplierCard(s, state.currencySymbol, viewModel)
        }
    }
}

// ─── Grid (MEDIUM) ────────────────────────────────────────────────────────────

@Composable
private fun SupplierGrid(state: SuppliersState, viewModel: SuppliersViewModel) {
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

// ─── Table (EXPANDED) ─────────────────────────────────────────────────────────

@Composable
private fun SupplierTable(state: SuppliersState, viewModel: SuppliersViewModel) {
    Card(
        Modifier.fillMaxSize(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            // Header row
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SupTHeader("Supplier",     0.25f)
                SupTHeader("Phone",        0.18f)
                SupTHeader("City / NTN",   0.20f)
                SupTHeader("Purchased",    0.15f, TextAlign.End)
                SupTHeader("Outstanding",  0.14f, TextAlign.End)
                SupTHeader("Actions",      0.08f, TextAlign.Center)
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
    label : String, weight : Float,
    align : TextAlign = TextAlign.Start
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
    s            : Supplier,
    currencySymbol: String,
    isEven       : Boolean,
    viewModel    : SuppliersViewModel
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
        // Name + avatar
        Row(
            Modifier.weight(0.25f),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SupplierAvatar(s)
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
            s.phone.ifBlank { "—" },
            modifier = Modifier.weight(0.18f),
            style    = MaterialTheme.typography.bodySmall,
            color    = if (s.phone.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f)
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        // City / NTN
        Column(Modifier.weight(0.20f)) {
            if (s.city.isNotBlank())
                Text(s.city, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            if (s.ntn != null)
                Text("NTN: ${s.ntn}", style = MaterialTheme.typography.labelSmall,
                    color = TextMuted, maxLines = 1)
        }
        // Total purchased
        Box(Modifier.weight(0.15f), contentAlignment = Alignment.CenterEnd) {
            Text(
                CurrencyFormatter.formatRs(s.totalPurchased),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            )
        }
        // Outstanding balance
        Box(Modifier.weight(0.14f), contentAlignment = Alignment.CenterEnd) {
            if (s.outstandingBalance > 0) {
                Surface(shape = RoundedCornerShape(100.dp), color = WarningContainer) {
                    Text(
                        CurrencyFormatter.formatRs(s.outstandingBalance),
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = Warning
                    )
                }
            } else {
                Text("—", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                    textAlign = TextAlign.End)
            }
        }
        // Actions
        Row(
            Modifier.weight(0.08f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = { viewModel.onIntent(SuppliersIntent.ShowEditSheet(s)) },
                modifier = Modifier.size(30.dp)
            ) { Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp)) }
            IconButton(
                onClick  = { viewModel.onIntent(SuppliersIntent.ConfirmDelete(s.id)) },
                modifier = Modifier.size(30.dp)
            ) { Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(16.dp)) }
        }
    }
}

// ─── Card (COMPACT / MEDIUM) ─────────────────────────────────────────────────

@Composable
private fun SupplierCard(
    s             : Supplier,
    currencySymbol: String,
    viewModel     : SuppliersViewModel
) {
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
                    modifier              = Modifier.weight(1f)
                ) {
                    SupplierAvatar(s)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            s.name,
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis
                        )
                        if (s.phone.isNotBlank())
                            Text(s.phone, style = MaterialTheme.typography.labelSmall,
                                color = TextMuted)
                        if (!s.city.isNullOrBlank())
                            Text(s.city, style = MaterialTheme.typography.labelSmall,
                                color = TextMuted)
                    }
                }
                Row {
                    IconButton(
                        onClick  = { viewModel.onIntent(SuppliersIntent.ShowEditSheet(s)) },
                        modifier = Modifier.size(34.dp)
                    ) { Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp)) }
                    IconButton(
                        onClick  = { viewModel.onIntent(SuppliersIntent.ConfirmDelete(s.id)) },
                        modifier = Modifier.size(34.dp)
                    ) { Icon(Icons.Rounded.Delete, null, tint = Danger, modifier = Modifier.size(16.dp)) }
                }
            }

            // Financials row — only shown when there are any
            if (s.outstandingBalance > 0 || s.totalPurchased > 0) {
                HorizontalDivider(
                    color     = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Total purchased
                    Column {
                        Text("Total Purchased",
                            style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(
                            CurrencyFormatter.formatRs(s.totalPurchased),
                            style      = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    // Outstanding
                    if (s.outstandingBalance > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Outstanding",
                                style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Surface(shape = RoundedCornerShape(100.dp), color = WarningContainer) {
                                Text(
                                    CurrencyFormatter.formatRs(s.outstandingBalance),
                                    Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    style      = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color      = Warning
                                )
                            }
                        }
                    }
                }
            }

            // NTN badge
            s.ntn?.let {
                BadgeChip(
                    text           = "NTN: $it",
                    containerColor = AccentContainer,
                    contentColor   = Accent
                )
            }
        }
    }
}

// ─── Avatar ───────────────────────────────────────────────────────────────────

@Composable
private fun SupplierAvatar(s: Supplier) {
    Box(
        Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(AccentContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            s.name.take(1).uppercase(),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = Accent
        )
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptySuppliers() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(AccentContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocalShipping, null,
                    tint = Accent, modifier = Modifier.size(32.dp))
            }
            Text("No suppliers yet",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
            Text("Tap + to add your first supplier",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SUPPLIER FORM BOTTOM SHEET
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierFormSheet(
    state    : SuppliersState,
    viewModel: SuppliersViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing  = state.editingSupplier != null

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(SuppliersIntent.DismissSheet) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (isEditing) "Edit Supplier" else "New Supplier",
                            style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            if (isEditing) "Update supplier details" else "Fill in supplier information",
                            style = MaterialTheme.typography.bodySmall, color = TextMuted
                        )
                    }
                    IconButton(
                        onClick  = { viewModel.onIntent(SuppliersIntent.DismissSheet) },
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) { Icon(Icons.Rounded.Close, "Close", modifier = Modifier.size(18.dp)) }
                }
            }

            // ── Contact info ─────────────────────────────────────────────────
            item {
                SupFormSection("Contact Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SupTextField("Name *", state.formName, "e.g. Al-Fatah Electronics") {
                            viewModel.onIntent(SuppliersIntent.FormName(it))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SupTextField("Phone", state.formPhone, "+92 300 0000000",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Phone) {
                                viewModel.onIntent(SuppliersIntent.FormPhone(it))
                            }
                            SupTextField("WhatsApp", state.formWhatsapp, "+92 300 0000000",
                                modifier = Modifier.weight(1f),
                                keyboardType = KeyboardType.Phone) {
                                viewModel.onIntent(SuppliersIntent.FormWhatsapp(it))
                            }
                        }
                        SupTextField("Email", state.formEmail, "supplier@example.com",
                            keyboardType = KeyboardType.Email) {
                            viewModel.onIntent(SuppliersIntent.FormEmail(it))
                        }
                    }
                }
            }

            // ── Location & identity ──────────────────────────────────────────
            item {
                SupFormSection("Location & Business") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SupTextField("Address", state.formAddress, "Shop / street address") {
                            viewModel.onIntent(SuppliersIntent.FormAddress(it))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SupTextField("City", state.formCity, "Karachi",
                                modifier = Modifier.weight(1f)) {
                                viewModel.onIntent(SuppliersIntent.FormCity(it))
                            }
                            SupTextField("NTN (optional)", state.formNtn, "1234567-8",
                                modifier = Modifier.weight(1f)) {
                                viewModel.onIntent(SuppliersIntent.FormNtn(it))
                            }
                        }
                    }
                }
            }

            // ── Financial ────────────────────────────────────────────────────
            item {
                SupFormSection("Opening Balance") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SupTextField(
                            label        = "Amount owed to supplier at setup",
                            value        = state.formOpeningBalance,
                            placeholder  = "0",
                            leadingText  = "Rs.",
                            keyboardType = KeyboardType.Decimal,
                            enabled      = !isEditing  // can't change opening balance on edit
                        ) { viewModel.onIntent(SuppliersIntent.FormOpeningBalance(it)) }
                        if (isEditing) {
                            Text(
                                "Opening balance cannot be changed after creation. Adjust via purchase payments.",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            // ── Notes ────────────────────────────────────────────────────────
            item {
                SupFormSection("Notes (optional)") {
                    SupTextField(
                        label      = "",
                        value      = state.formNotes,
                        placeholder = "Any additional notes…",
                        singleLine  = false,
                        minLines    = 3
                    ) { viewModel.onIntent(SuppliersIntent.FormNotes(it)) }
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
                    onClick  = { viewModel.onIntent(SuppliersIntent.SaveSupplier) },
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
                                if (isEditing) "Update Supplier" else "Add Supplier",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Shared form primitives ───────────────────────────────────────────────────

@Composable
private fun SupFormSection(
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
private fun SupTextField(
    label        : String,
    value        : String,
    placeholder  : String      = "",
    modifier     : Modifier    = Modifier.fillMaxWidth(),
    keyboardType : KeyboardType = KeyboardType.Text,
    leadingText  : String?     = null,
    singleLine   : Boolean     = true,
    minLines     : Int         = 1,
    enabled      : Boolean     = true,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        enabled       = enabled,
        label         = if (label.isNotBlank()) ({ Text(label, style = MaterialTheme.typography.labelSmall) }) else null,
        placeholder   = { Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodySmall) },
        leadingIcon   = if (leadingText != null) ({
            Text(leadingText, style = MaterialTheme.typography.bodySmall,
                color = TextMuted, fontWeight = FontWeight.SemiBold)
        }) else null,
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