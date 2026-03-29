package com.matechmatrix.shopflowpos.feature.inventory.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.model.enums.ProductCondition
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    windowSize: AppWindowSize,
    navigateChild: (String) -> Unit = {},
    viewModel: InventoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InventoryEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onIntent(InventoryIntent.ShowAddDialog) },
                containerColor = Primary, contentColor = Color.White, shape = CircleShape
            ) { Icon(Icons.Rounded.Add, "Add Product") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Search field ──
            TextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onIntent(InventoryIntent.Search(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search products...", color = TextMuted) },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        null,
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (state.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onIntent(InventoryIntent.Search("")) }) {
                            Icon(
                                Icons.Rounded.Clear,
                                null,
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = InputBgLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                )
            )

            // ── Pill-shaped category chips ──
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PillChip(label = "All", selected = state.selectedCategory == null) {
                        viewModel.onIntent(InventoryIntent.FilterByCategory(null))
                    }
                }
                items(ProductCategory.entries) { cat ->
                    PillChip(
                        label = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                        selected = state.selectedCategory == cat
                    ) {
                        viewModel.onIntent(InventoryIntent.FilterByCategory(if (state.selectedCategory == cat) null else cat))
                    }
                }
            }

            // ── Count row ──
            if (!state.isLoading) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${state.filteredProducts.size} products",
                        style = MaterialTheme.typography.bodySmall, color = TextMuted
                    )
                    val lowCount = state.filteredProducts.count { it.stock <= it.lowStockThreshold }
                    if (lowCount > 0) {
                        Surface(shape = RoundedCornerShape(100.dp), color = DangerContainer) {
                            Text(
                                "$lowCount low stock",
                                Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Danger,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            when {
                state.isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }

                state.filteredProducts.isEmpty() -> EmptyState(
                    icon = Icons.Rounded.Inventory2,
                    title = "No products found",
                    subtitle = "Tap + to add your first product"
                )

                windowSize == AppWindowSize.COMPACT -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(
                        8.dp
                    )
                ) {
                    items(state.filteredProducts, key = { it.id }) { product ->
                        ProductListCard(
                            product = product,
                            showCostPrice = state.showCostPrice,
                            currencySymbol = state.currencySymbol,
                            onEdit = { viewModel.onIntent(InventoryIntent.ShowEditDialog(product)) },
                            onStockUpdate = {
                                viewModel.onIntent(
                                    InventoryIntent.ShowStockDialog(
                                        product.id
                                    )
                                )
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }

                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredProducts, key = { it.id }) { product ->
                        ProductGridCard(
                            product = product,
                            showCostPrice = state.showCostPrice,
                            currencySymbol = state.currencySymbol,
                            onEdit = { viewModel.onIntent(InventoryIntent.ShowEditDialog(product)) },
                            onStockUpdate = {
                                viewModel.onIntent(
                                    InventoryIntent.ShowStockDialog(
                                        product.id
                                    )
                                )
                            }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (state.showAddDialog) ProductFormDialog(state, viewModel)

    state.showStockDialog?.let { productId ->
        state.products.find { it.id == productId }?.let { product ->
            StockUpdateDialog(
                product = product,
                onConfirm = { viewModel.onIntent(InventoryIntent.UpdateStock(productId, it)) },
                onDismiss = { viewModel.onIntent(InventoryIntent.ShowStockDialog("")) }
            )
        }
    }

    state.showDeleteConfirm?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(InventoryIntent.ConfirmDelete("")) },
            title = { Text("Delete Product?") },
            text = { Text("This product will be removed from your inventory.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(InventoryIntent.DeleteProduct) }) {
                    Text("Delete", color = Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(InventoryIntent.ConfirmDelete("")) }) {
                    Text(
                        "Cancel"
                    )
                }
            }
        )
    }
}

@Composable
private fun PillChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100.dp),
        color = if (selected) Primary else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.5.dp, BorderColor)
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else TextSecondary
        )
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String = ""
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(20.dp)).background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(32.dp)) }
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (subtitle.isNotBlank()) Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun ProductListCard(
    product: Product,
    showCostPrice: Boolean,
    currencySymbol: String,
    onEdit: () -> Unit,
    onStockUpdate: () -> Unit
) {
    val isLow = product.stock <= product.lowStockThreshold
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Inventory2,
                    null,
                    tint = Primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!product.imei.isNullOrBlank()) Text(
                    "IMEI: ${product.imei}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${
                        product.category.name.lowercase().replaceFirstChar { it.uppercase() }
                    } • ${product.brand} ${product.model}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$currencySymbol ${CurrencyFormatter.formatCompact(product.sellingPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    if (showCostPrice) Text(
                        "Cost: $currencySymbol ${
                            CurrencyFormatter.formatCompact(
                                product.costPrice
                            )
                        }", style = MaterialTheme.typography.labelSmall, color = TextMuted
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isLow) DangerContainer else SuccessContainer
                ) {
                    Text(
                        "Qty: ${product.stock}",
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLow) Danger else Success,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row {
                    IconButton(onClick = onStockUpdate, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Rounded.EditNote,
                            null,
                            tint = Info,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductGridCard(
    product: Product,
    showCostPrice: Boolean,
    currencySymbol: String,
    onEdit: () -> Unit,
    onStockUpdate: () -> Unit
) {
    val isLow = product.stock <= product.lowStockThreshold
    Card(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        .background(PrimaryContainer), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Inventory2,
                        null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = if (isLow) DangerContainer else SuccessContainer
                ) {
                    Text(
                        "${product.stock}",
                        Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isLow) Danger else Success,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!product.imei.isNullOrBlank()) Text(
                "IMEI: ${product.imei}",
                style = MaterialTheme.typography.labelSmall,
                color = Primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                "${
                    product.category.name.lowercase().replaceFirstChar { it.uppercase() }
                } • ${product.brand}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                "$currencySymbol ${CurrencyFormatter.formatRs(product.sellingPrice)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Primary
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = onStockUpdate,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) { Text("Stock", style = MaterialTheme.typography.labelSmall, color = Info) }
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) { Text("Edit", style = MaterialTheme.typography.labelSmall, color = Primary) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormDialog(state: InventoryState, viewModel: InventoryViewModel) {
    val isEditing = state.editingProduct != null
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(InventoryIntent.DismissDialog) },
        title = {
            Text(
                if (isEditing) "Edit Product" else "Add Product",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FormField(
                    "Product Name *",
                    state.formName
                ) { viewModel.onIntent(InventoryIntent.FormName(it)) }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormField("Brand", state.formBrand, Modifier.weight(1f)) {
                        viewModel.onIntent(
                            InventoryIntent.FormBrand(it)
                        )
                    }
                    FormField("Model", state.formModel, Modifier.weight(1f)) {
                        viewModel.onIntent(
                            InventoryIntent.FormModel(it)
                        )
                    }
                }

                if (state.formCategory == ProductCategory.PHONE) {
                    FormField(
                        "IMEI Number",
                        state.formImei
                    ) { viewModel.onIntent(InventoryIntent.FormImei(it)) }

                    var ptaExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = ptaExpanded,
                        onExpandedChange = { ptaExpanded = it }) {
                        OutlinedTextField(
                            value = state.formPtaStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("PTA Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(ptaExpanded) },
                            modifier = Modifier.fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = ptaExpanded,
                            onDismissRequest = { ptaExpanded = false }) {
                            listOf("NA", "APPROVED", "NON_PTA", "JV").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status) },
                                    onClick = {
                                        viewModel.onIntent(
                                            InventoryIntent.FormPtaStatus(status)
                                        ); ptaExpanded = false
                                    })
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    var catExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = catExpanded,
                        onExpandedChange = { catExpanded = it },
                        Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.formCategory.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            onValueChange = {}, readOnly = true, label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) },
                            modifier = Modifier.fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = catExpanded,
                            onDismissRequest = { catExpanded = false }) {
                            ProductCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            cat.name.lowercase()
                                                .replaceFirstChar { it.uppercase() })
                                    },
                                    onClick = {
                                        viewModel.onIntent(InventoryIntent.FormCategory(cat)); catExpanded =
                                        false
                                    })
                            }
                        }
                    }
                    var condExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = condExpanded,
                        onExpandedChange = { condExpanded = it },
                        Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = state.formCondition.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            onValueChange = {}, readOnly = true, label = { Text("Condition") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(condExpanded) },
                            modifier = Modifier.fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = condExpanded,
                            onDismissRequest = { condExpanded = false }) {
                            ProductCondition.entries.forEach { cond ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            cond.name.lowercase()
                                                .replaceFirstChar { it.uppercase() })
                                    },
                                    onClick = {
                                        viewModel.onIntent(
                                            InventoryIntent.FormCondition(
                                                cond
                                            )
                                        ); condExpanded = false
                                    })
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormField(
                        "Cost Price",
                        state.formCostPrice,
                        Modifier.weight(1f),
                        KeyboardType.Number
                    ) { viewModel.onIntent(InventoryIntent.FormCostPrice(it)) }
                    FormField(
                        "Sale Price *",
                        state.formSalePrice,
                        Modifier.weight(1f),
                        KeyboardType.Number
                    ) { viewModel.onIntent(InventoryIntent.FormSalePrice(it)) }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FormField(
                        "Stock",
                        state.formStock,
                        Modifier.weight(1f),
                        KeyboardType.Number
                    ) { viewModel.onIntent(InventoryIntent.FormStock(it)) }
                    FormField(
                        "Low Alert",
                        state.formLowStockAlert,
                        Modifier.weight(1f),
                        KeyboardType.Number
                    ) { viewModel.onIntent(InventoryIntent.FormLowStockAlert(it)) }
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
                onClick = { viewModel.onIntent(InventoryIntent.SaveProduct) },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) { Text(if (isEditing) "Update" else "Add Product") }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.onIntent(InventoryIntent.DismissDialog) }) {
                Text(
                    "Cancel"
                )
            }
        }
    )
}

@Composable
private fun FormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier, shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = BorderColor
        )
    )
}

@Composable
private fun StockUpdateDialog(product: Product, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var newStock by remember { mutableStateOf(product.stock.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    product.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    "Current: ${product.stock} units",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                OutlinedTextField(
                    value = newStock, onValueChange = { newStock = it },
                    label = { Text("New Stock Quantity") },
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
                onClick = { newStock.toIntOrNull()?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
