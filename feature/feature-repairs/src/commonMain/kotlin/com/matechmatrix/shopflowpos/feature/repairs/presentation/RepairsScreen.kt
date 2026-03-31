package com.matechmatrix.shopflowpos.feature.repairs.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.model.RepairJob
import com.matechmatrix.shopflowpos.core.model.enums.RepairStatus
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairsScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : RepairsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is RepairsEffect.Toast) snackbarHostState.showSnackbar(e.msg)
        }
    }

    val hPad = when (windowSize) {
        AppWindowSize.EXPANDED -> 28.dp
        AppWindowSize.MEDIUM   -> 20.dp
        AppWindowSize.COMPACT  -> 16.dp
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (windowSize == AppWindowSize.COMPACT) {
                FloatingActionButton(
                    onClick        = { viewModel.onIntent(RepairsIntent.ShowAddDialog) },
                    containerColor = Primary,
                    contentColor   = Color.White,
                    shape          = CircleShape
                ) { Icon(Icons.Rounded.Build, "New Repair") }
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
            // ── Header row: stats + Add button ──────────────────────────────
            if (!state.isLoading) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    RepairStatChip(Modifier.weight(1f), "${state.receivedCount}",    "Received",     Primary)
                    RepairStatChip(Modifier.weight(1f), "${state.inRepairCount}", "In Repair", Info)
                    RepairStatChip(Modifier.weight(1f), "${state.readyCount}",    "Ready",       Success)

                    if (windowSize != AppWindowSize.COMPACT) {
                        Spacer(Modifier.width(4.dp))
                        Button(
                            onClick = { viewModel.onIntent(RepairsIntent.ShowAddDialog) },
                            colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape   = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            Icon(Icons.Rounded.Build, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("New Job", style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Status filter chips ──────────────────────────────────────────
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.statusFilter == null,
                        onClick  = { viewModel.onIntent(RepairsIntent.SetFilter(null)) },
                        label    = { Text("All") },
                        shape    = RoundedCornerShape(100.dp),
                        colors   = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor     = Color.White
                        ),
                        border   = FilterChipDefaults.filterChipBorder(
                            enabled = true, selected = state.statusFilter == null,
                            borderColor         = MaterialTheme.colorScheme.outlineVariant,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
                items(RepairStatus.entries) { status ->
                    val isSelected   = state.statusFilter == status
                    val statusColor  = repairStatusColor(status)
                    FilterChip(
                        selected = isSelected,
                        onClick  = { viewModel.onIntent(RepairsIntent.SetFilter(if (isSelected) null else status)) },
                        label    = {
                            Text(status.display, style = MaterialTheme.typography.labelSmall)
                        },
                        shape  = RoundedCornerShape(100.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = statusColor,
                            selectedLabelColor     = Color.White,
                            containerColor         = statusColor.copy(alpha = 0.10f),
                            labelColor             = statusColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled             = true,
                            selected            = isSelected,
                            borderColor         = statusColor.copy(alpha = 0.25f),
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // ── Content ──────────────────────────────────────────────────────
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Rounded.Build, null, tint = Primary, modifier = Modifier.size(32.dp)) }
                        Text("No repair jobs", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Tap + to create the first job", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> when (windowSize) {
                    AppWindowSize.COMPACT  -> RepairCompactList(state, viewModel)
                    else -> RepairDesktopTable(state, viewModel)
                }
            }
        }
    }

    if (state.showAddDialog) RepairFormDialog(state, viewModel)

    state.showCompleteDialog?.let { repair ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(RepairsIntent.DismissCompleteDialog) },
            title = { Text("Update Charge", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(repair.customerName, style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(repair.deviceModel, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Estimated: ${CurrencyFormatter.formatRs(repair.estimatedCost)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value         = state.finalChargeInput,
                        onValueChange = { viewModel.onIntent(RepairsIntent.SetFinalCharge(it)) },
                        label         = { Text("Final Bill Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(10.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Success,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    val final = state.finalChargeInput.toDoubleOrNull() ?: 0.0
                    if (final > 0) {
                        Surface(shape = RoundedCornerShape(10.dp), color = SuccessContainer) {
                            Text("Total to collect: ${CurrencyFormatter.formatRs(final)}",
                                Modifier.fillMaxWidth().padding(12.dp),
                                color = Success, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.onIntent(RepairsIntent.CompleteRepair) },
                    colors = ButtonDefaults.buttonColors(containerColor = Success),
                    shape  = RoundedCornerShape(10.dp)) { Text("Update & Mark Ready") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(RepairsIntent.DismissCompleteDialog) }) { Text("Cancel") }
            }
        )
    }
}

// ── COMPACT — card list ───────────────────────────────────────────────────────
@Composable
private fun RepairCompactList(state: RepairsState, viewModel: RepairsViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(bottom = 88.dp)
    ) {
        items(state.filtered, key = { it.id }) { repair ->
            RepairCard(repair, state.currencySymbol, viewModel)
        }
    }
}

// ── EXPANDED — desktop data table ────────────────────────────────────────────
@Composable
private fun RepairDesktopTable(state: RepairsState, viewModel: RepairsViewModel) {
    Card(
        Modifier.fillMaxSize(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RepTHeader("Job # / Customer", 0.20f)
                RepTHeader("Device / Model",    0.20f)
                RepTHeader("Problem",          0.20f)
                RepTHeader("Final Cost",       0.12f, TextAlign.End)
                RepTHeader("Status",           0.14f, TextAlign.Center)
                RepTHeader("Date",             0.14f)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                itemsIndexed(state.filtered, key = { _, r -> r.id }) { idx, repair ->
                    RepairTableRow(repair, idx % 2 == 0, viewModel)
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
private fun RowScope.RepTHeader(
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
private fun RepairTableRow(repair: RepairJob, isEven: Boolean, viewModel: RepairsViewModel) {
    val statusColor = repairStatusColor(repair.status)
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
        Column(Modifier.weight(0.20f)) {
            Text(repair.jobNumber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Primary)
            Text(repair.customerName, style = MaterialTheme.typography.bodySmall)
        }
        Column(Modifier.weight(0.20f)) {
            Text(repair.deviceBrand, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(repair.deviceModel, style = MaterialTheme.typography.bodySmall)
        }
        Text(repair.problemDescription, modifier = Modifier.weight(0.20f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(CurrencyFormatter.formatRs(repair.finalCost.takeIf { it > 0 } ?: repair.estimatedCost),
            modifier = Modifier.weight(0.12f),
            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End)
        Box(Modifier.weight(0.14f), contentAlignment = Alignment.Center) {
            Surface(shape = RoundedCornerShape(100.dp), color = statusColor.copy(alpha = 0.12f)) {
                Text(
                    repair.status.display,
                    Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
        Text(DateTimeUtils.formatDate(repair.createdAt), modifier = Modifier.weight(0.14f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun RepairStatChip(modifier: Modifier, value: String, label: String, color: Color) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.10f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ── Repair card (Compact) ─────────────────────────────────────────────────────
@Composable
private fun RepairCard(repair: RepairJob, currencySymbol: String, viewModel: RepairsViewModel) {
    val statusColor = repairStatusColor(repair.status)
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
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)) {
                    Box(
                        Modifier.size(42.dp).clip(CircleShape).background(statusColor.copy(0.12f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.Build, null, tint = statusColor, modifier = Modifier.size(20.dp)) }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(repair.customerName, style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${repair.deviceBrand} ${repair.deviceModel}", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusColor.copy(alpha = 0.12f)) {
                    Text(
                        repair.status.display,
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor, fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(repair.problemDescription, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
                overflow = TextOverflow.Ellipsis)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Est: ${CurrencyFormatter.formatRs(repair.estimatedCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (repair.finalCost > 0) {
                        Text("Bill: ${CurrencyFormatter.formatRs(repair.finalCost)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Success, fontWeight = FontWeight.Bold)
                    }
                }
                Text(DateTimeUtils.formatDate(repair.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (repair.status != RepairStatus.DELIVERED && repair.status != RepairStatus.CANCELLED) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val nextStatus = when(repair.status) {
                        RepairStatus.RECEIVED -> RepairStatus.DIAGNOSING
                        RepairStatus.DIAGNOSING -> RepairStatus.IN_REPAIR
                        RepairStatus.IN_REPAIR -> RepairStatus.READY
                        else -> null
                    }
                    
                    if (nextStatus != null && nextStatus != RepairStatus.READY) {
                        OutlinedButton(
                            onClick = { viewModel.onIntent(RepairsIntent.UpdateStatus(repair.id, nextStatus)) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(10.dp)
                        ) { Text("To ${nextStatus.display}", style = MaterialTheme.typography.labelSmall) }
                    } else if (nextStatus == RepairStatus.READY) {
                        Button(
                            onClick  = { viewModel.onIntent(RepairsIntent.ShowCompleteDialog(repair)) },
                            modifier = Modifier.weight(1f),
                            colors   = ButtonDefaults.buttonColors(containerColor = Success),
                            shape    = RoundedCornerShape(10.dp)
                        ) { Text("Finish & Bill", style = MaterialTheme.typography.labelSmall) }
                    }
                    
                    OutlinedButton(
                        onClick  = { viewModel.onIntent(RepairsIntent.UpdateStatus(repair.id, RepairStatus.CANCELLED)) },
                        modifier = Modifier.weight(0.5f),
                        shape    = RoundedCornerShape(10.dp),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, Danger)
                    ) { Icon(Icons.Rounded.Close, null, tint = Danger, modifier = Modifier.size(16.dp)) }
                }
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun repairStatusColor(status: RepairStatus): Color = when (status) {
    RepairStatus.RECEIVED     -> Primary
    RepairStatus.DIAGNOSING   -> Warning
    RepairStatus.WAITING_PARTS -> Warning
    RepairStatus.IN_REPAIR    -> Info
    RepairStatus.READY       -> Success
    RepairStatus.DELIVERED   -> Success
    RepairStatus.CANCELLED   -> Danger
}

// ── Form dialog ───────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepairFormDialog(state: RepairsState, viewModel: RepairsViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(RepairsIntent.DismissDialog) },
        title = { Text("New Repair Job", fontWeight = FontWeight.Bold) },
        text  = {
            Column(
                modifier            = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RepField("Customer Name *", state.formCustomerName) { viewModel.onIntent(RepairsIntent.FormCustomerName(it)) }
                RepField("Phone", state.formCustomerPhone, KeyboardType.Phone) { viewModel.onIntent(RepairsIntent.FormCustomerPhone(it)) }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RepField("Brand", state.formDeviceBrand, modifier = Modifier.weight(1f)) { viewModel.onIntent(RepairsIntent.FormDeviceBrand(it)) }
                    RepField("Model *", state.formDeviceModel, modifier = Modifier.weight(1f)) { viewModel.onIntent(RepairsIntent.FormDeviceModel(it)) }
                }
                
                RepField("Problem Description *", state.formProblemDescription) { viewModel.onIntent(RepairsIntent.FormProblemDescription(it)) }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RepField("Est. Cost", state.formEstimatedCost, KeyboardType.Number, Modifier.weight(1f)) { viewModel.onIntent(RepairsIntent.FormEstimatedCost(it)) }
                    RepField("Advance", state.formAdvancePaid, KeyboardType.Number, Modifier.weight(1f)) { viewModel.onIntent(RepairsIntent.FormAdvancePaid(it)) }
                }
                
                RepField("IMEI / Serial", state.formImei) { viewModel.onIntent(RepairsIntent.FormImei(it)) }
                RepField("Notes", state.formNotes) { viewModel.onIntent(RepairsIntent.FormNotes(it)) }
                
                state.formError?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Danger)
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.onIntent(RepairsIntent.SaveRepair) },
                colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                shape   = RoundedCornerShape(10.dp)) { Text("Create Job") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(RepairsIntent.DismissDialog) }) { Text("Cancel") }
        }
    )
}

@Composable
private fun RepField(
    label: String, value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value           = value, onValueChange = onValueChange,
        label           = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine      = true, modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape           = RoundedCornerShape(10.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}
