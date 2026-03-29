package com.matechmatrix.shopflowpos.feature.inventory.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import com.matechmatrix.shopflowpos.core.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// Entry point
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormBottomSheet(
    state    : InventoryState,
    viewModel: InventoryViewModel
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEditing  = state.editingProduct != null

    ModalBottomSheet(
        onDismissRequest   = { viewModel.onIntent(InventoryIntent.DismissSheet) },
        sheetState         = sheetState,
        containerColor     = MaterialTheme.colorScheme.surface,
        dragHandle         = { SheetDragHandle() },
        shape              = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        LazyColumn(
            contentPadding      = PaddingValues(
                start  = 20.dp,
                end    = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Sheet Header ────────────────────────────────────────────────
            item {
                SheetHeader(
                    isEditing = isEditing,
                    onDismiss = { viewModel.onIntent(InventoryIntent.DismissSheet) }
                )
            }

            // ── Category Selector (full-width pill grid) ────────────────────
            item {
                FormSection(title = "Category") {
                    CategoryGrid(
                        selected = state.formCategory,
                        onSelect = { viewModel.onIntent(InventoryIntent.FormCategory(it)) }
                    )
                }
            }

            // ── Product Identity ────────────────────────────────────────────
            item {
                FormSection(title = "Product Details") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                        // Product name — always shown
                        SheetTextField(
                            label       = "Product Name *",
                            value       = state.formName,
                            placeholder = "e.g. Samsung Galaxy S24 Ultra",
                            leadingIcon = Icons.AutoMirrored.Rounded.Label,
                            onValueChange = { viewModel.onIntent(InventoryIntent.FormName(it)) }
                        )

                        // Brand + Model — shown for categories that have them
                        if (state.formCategory.showBrand) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                SheetTextField(
                                    label       = "Brand",
                                    value       = state.formBrand,
                                    placeholder = "Samsung",
                                    modifier    = Modifier.weight(1f),
                                    onValueChange = { viewModel.onIntent(InventoryIntent.FormBrand(it)) }
                                )
                                if (state.formCategory.showModel) {
                                    SheetTextField(
                                        label       = "Model",
                                        value       = state.formModel,
                                        placeholder = "S24 Ultra",
                                        modifier    = Modifier.weight(1f),
                                        onValueChange = { viewModel.onIntent(InventoryIntent.FormModel(it)) }
                                    )
                                }
                            }
                        }

                        // IMEI — only for IMEI-tracked categories
                        if (state.formCategory.hasImei) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                SheetTextField(
                                    label       = "IMEI Number *",
                                    value       = state.formImei,
                                    placeholder = "15-digit IMEI",
                                    leadingIcon = Icons.Rounded.Nfc,
                                    keyboardType = KeyboardType.Number,
                                    onValueChange = { if (it.length <= 15) viewModel.onIntent(InventoryIntent.FormImei(it)) }
                                )
                                // PTA Status (only for Phone / Tablet)
                                if (state.formCategory == ProductCategory.PHONE ||
                                    state.formCategory == ProductCategory.TABLET) {
                                    PtaStatusSelector(
                                        selected = state.formPtaStatus,
                                        onSelect = { viewModel.onIntent(InventoryIntent.FormPtaStatus(it)) }
                                    )
                                }
                            }
                        }

                        // Condition
                        ConditionSelector(
                            selected = state.formCondition,
                            onSelect = { viewModel.onIntent(InventoryIntent.FormCondition(it)) }
                        )

                        // Barcode
                        SheetTextField(
                            label       = "Barcode (optional)",
                            value       = state.formBarcode,
                            placeholder = "Scan or type barcode",
                            leadingIcon = Icons.Rounded.QrCode,
                            onValueChange = { viewModel.onIntent(InventoryIntent.FormBarcode(it)) }
                        )
                    }
                }
            }

            // ── Specs — for phones, laptops, tablets ────────────────────────
            if (state.formCategory.hasSpecs || state.formCategory.hasImei) {
                item {
                    FormSection(title = "Specifications") {
                        SpecsForm(state = state, viewModel = viewModel)
                    }
                }
            }

            // ── Pricing ──────────────────────────────────────────────────────
            item {
                FormSection(title = "Pricing") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SheetTextField(
                            label        = "Cost Price",
                            value        = state.formCostPrice,
                            placeholder  = "0",
                            leadingText  = "Rs.",
                            keyboardType = KeyboardType.Decimal,
                            modifier     = Modifier.weight(1f),
                            onValueChange = { viewModel.onIntent(InventoryIntent.FormCostPrice(it)) }
                        )
                        SheetTextField(
                            label        = "Sale Price *",
                            value        = state.formSalePrice,
                            placeholder  = "0",
                            leadingText  = "Rs.",
                            keyboardType = KeyboardType.Decimal,
                            modifier     = Modifier.weight(1f),
                            onValueChange = { viewModel.onIntent(InventoryIntent.FormSalePrice(it)) }
                        )
                    }
                }
            }

            // ── Stock — only shown for non-IMEI categories ───────────────────
            if (!state.formCategory.hasImei) {
                item {
                    FormSection(title = "Stock") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SheetTextField(
                                label        = "Quantity",
                                value        = state.formStock,
                                placeholder  = "0",
                                leadingIcon  = Icons.Rounded.Inventory2,
                                keyboardType = KeyboardType.Number,
                                modifier     = Modifier.weight(1f),
                                onValueChange = { viewModel.onIntent(InventoryIntent.FormStock(it)) }
                            )
                            SheetTextField(
                                label        = "Low Stock Alert",
                                value        = state.formLowStockAlert,
                                placeholder  = "3",
                                leadingIcon  = Icons.Rounded.NotificationsActive,
                                keyboardType = KeyboardType.Number,
                                modifier     = Modifier.weight(1f),
                                onValueChange = { viewModel.onIntent(InventoryIntent.FormLowStockAlert(it)) }
                            )
                        }
                    }
                }
            }

            // ── Notes ────────────────────────────────────────────────────────
            item {
                FormSection(title = "Notes (optional)") {
                    SheetTextField(
                        label       = "",
                        value       = state.formDescription,
                        placeholder = "Additional notes about this product…",
                        singleLine  = false,
                        minLines    = 3,
                        onValueChange = { viewModel.onIntent(InventoryIntent.FormDescription(it)) }
                    )
                }
            }

            // ── Error ────────────────────────────────────────────────────────
            state.formError?.let { error ->
                item {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DangerContainer)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = Danger, modifier = Modifier.size(16.dp))
                        Text(error, style = MaterialTheme.typography.bodySmall, color = Danger)
                    }
                }
            }

            // ── Submit button ────────────────────────────────────────────────
            item {
                Button(
                    onClick  = { viewModel.onIntent(InventoryIntent.SaveProduct) },
                    enabled  = !state.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (isEditing) Icons.Rounded.Check else Icons.Rounded.Add,
                                null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                if (isEditing) "Update Product" else "Add Product",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Specs form — shared for Phone / Laptop / Tablet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SpecsForm(state: InventoryState, viewModel: InventoryViewModel) {
    val cat = state.formCategory
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Color — universal spec
        SheetTextField(
            label       = "Color",
            value       = state.formColor,
            placeholder = "e.g. Phantom Black",
            leadingIcon = Icons.Rounded.Palette,
            onValueChange = { viewModel.onIntent(InventoryIntent.FormColor(it)) }
        )

        // RAM + Storage row — for PHONE, LAPTOP, TABLET
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetTextField(
                label        = "RAM (GB)",
                value        = state.formRamGb,
                placeholder  = "8",
                keyboardType = KeyboardType.Number,
                modifier     = Modifier.weight(1f),
                onValueChange = { viewModel.onIntent(InventoryIntent.FormRamGb(it)) }
            )
            SheetTextField(
                label        = "Storage (GB)",
                value        = state.formStorageGb,
                placeholder  = "128",
                keyboardType = KeyboardType.Number,
                modifier     = Modifier.weight(1f),
                onValueChange = { viewModel.onIntent(InventoryIntent.FormStorageGb(it)) }
            )
        }

        // ROM (some devices differ — Phones/Tablets mostly)
        if (cat == ProductCategory.PHONE || cat == ProductCategory.TABLET) {
            SheetTextField(
                label        = "Internal ROM (GB)",
                value        = state.formRomGb,
                placeholder  = "128",
                keyboardType = KeyboardType.Number,
                onValueChange = { viewModel.onIntent(InventoryIntent.FormRomGb(it)) }
            )
        }

        // Processor — Laptop + Phone
        if (cat == ProductCategory.LAPTOP || cat == ProductCategory.PHONE || cat == ProductCategory.TABLET) {
            SheetTextField(
                label       = "Processor / Chip",
                value       = state.formProcessor,
                placeholder = if (cat == ProductCategory.LAPTOP) "Intel Core i7-1360P"
                else "Snapdragon 8 Gen 3",
                leadingIcon = Icons.Rounded.Memory,
                onValueChange = { viewModel.onIntent(InventoryIntent.FormProcessor(it)) }
            )
        }

        // Screen size — all specs categories
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetTextField(
                label        = "Screen Size (inch)",
                value        = state.formScreenSize,
                placeholder  = if (cat == ProductCategory.LAPTOP) "15.6" else "6.8",
                keyboardType = KeyboardType.Decimal,
                modifier     = Modifier.weight(1f),
                onValueChange = { viewModel.onIntent(InventoryIntent.FormScreenSize(it)) }
            )
            // Battery only for phones/tablets
            if (cat != ProductCategory.LAPTOP) {
                SheetTextField(
                    label        = "Battery (mAh)",
                    value        = state.formBatteryMah,
                    placeholder  = "5000",
                    keyboardType = KeyboardType.Number,
                    modifier     = Modifier.weight(1f),
                    onValueChange = { viewModel.onIntent(InventoryIntent.FormBatteryMah(it)) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Category Grid Selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CategoryGrid(
    selected : ProductCategory,
    onSelect : (ProductCategory) -> Unit
) {
    val categories = ProductCategory.entries
    // 2 columns
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(3).forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { cat ->
                    CategoryPill(
                        category = cat,
                        selected = selected == cat,
                        modifier = Modifier.weight(1f),
                        onClick  = { onSelect(cat) }
                    )
                }
                // Fill remaining slots in last row
                repeat(3 - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CategoryPill(
    category : ProductCategory,
    selected : Boolean,
    modifier : Modifier = Modifier,
    onClick  : () -> Unit
) {
    val bgColor     = if (selected) Primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor  = if (selected) Primary.copy(alpha = 0f) else BorderColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(category.emoji, fontSize = 20.sp)
            Text(
                category.displayName,
                style      = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color      = contentColor,
                maxLines   = 1
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PTA Status selector
// ─────────────────────────────────────────────────────────────────────────────

private val PTA_OPTIONS = listOf(
    "NA"       to "Not Applicable",
    "APPROVED" to "PTA Approved",
    "NON_PTA"  to "Non-PTA",
    "JV"       to "JV (Grey)"
)

@Composable
private fun PtaStatusSelector(selected: String, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "PTA Status",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PTA_OPTIONS.forEach { (code, label) ->
                val isSelected = selected == code
                FilterChip(
                    selected = isSelected,
                    onClick  = { onSelect(code) },
                    label    = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor     = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Condition selector
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ConditionSelector(selected: ProductCondition, onSelect: (ProductCondition) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Condition",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProductCondition.entries.forEach { cond ->
                val isSelected = selected == cond
                val (emoji, label) = when (cond) {
                    ProductCondition.NEW        -> "✨" to "New"
                    ProductCondition.USED       -> "⚡️" to "Used"
                    ProductCondition.LIKE_NEW   -> "💎" to "Like New"
                    ProductCondition.GOOD       -> "👍" to "Good"
                    ProductCondition.FAIR       -> "📦" to "Fair"
                    ProductCondition.REFURBISHED -> "🔧" to "Refurb"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else BorderColor,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelect(cond) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(emoji, fontSize = 16.sp)
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared form primitives
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SheetDragHandle() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        )
    }
}

@Composable
private fun SheetHeader(isEditing: Boolean, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                if (isEditing) "Edit Product" else "New Product",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Text(
                if (isEditing) "Update product details" else "Fill in the product information",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
        IconButton(
            onClick  = onDismiss,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(Icons.Rounded.Close, "Close", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun FormSection(
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
                    Modifier
                        .size(width = 3.dp, height = 14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Primary)
                )
                Text(
                    title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
            }
        }
        content()
    }
}

@Composable
private fun SheetTextField(
    label        : String,
    value        : String,
    placeholder  : String    = "",
    modifier     : Modifier  = Modifier.fillMaxWidth(),
    leadingIcon  : ImageVector? = null,
    leadingText  : String?   = null,
    keyboardType : KeyboardType = KeyboardType.Text,
    singleLine   : Boolean   = true,
    minLines     : Int       = 1,
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