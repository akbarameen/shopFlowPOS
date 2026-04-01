package com.matechmatrix.shopflowpos.feature.inventory.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.BadgeChip
import com.matechmatrix.shopflowpos.core.ui.components.CategoryChipRow
import com.matechmatrix.shopflowpos.core.ui.components.EmptyStateView
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    windowSize   : AppWindowSize,
    navigateChild: (String) -> Unit = {},
    viewModel    : InventoryViewModel = koinViewModel()
) {
    val state           by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState   = remember { SnackbarHostState() }
    val pagedItems      = state.pagedProducts.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is InventoryEffect.ShowToast -> snackbarState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.onIntent(InventoryIntent.ShowAddSheet) },
                containerColor = Primary,
                contentColor   = Color.White,
                shape          = CircleShape
            ) { Icon(Icons.Rounded.Add, "Add Product") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header row ──────────────────────────────────────────────────
            InventoryHeader(
                lowStockCount = state.lowStockCount,
                currencySymbol = state.currencySymbol
            )

            // ── Search bar ──────────────────────────────────────────────────
            com.matechmatrix.shopflowpos.core.ui.components.ShopFlowSearchBar(
                query         = state.searchQuery,
                onQueryChange = { viewModel.onIntent(InventoryIntent.Search(it)) },
                placeholder   = "Search by name, IMEI, brand…"
            )

            // ── Category chips ───────────────────────────────────────────────
            CategoryChipRow(
                categories        = ProductCategory.entries.map { it.displayName },
                selectedCategory  = state.selectedCategory?.displayName,
                onCategorySelected = { displayName ->
                    val cat = ProductCategory.entries.find { it.displayName == displayName }
                    viewModel.onIntent(InventoryIntent.FilterByCategory(
                        if (state.selectedCategory?.displayName == displayName) null else cat
                    ))
                }
            )

            // ── Product count indicator ──────────────────────────────────────
            AnimatedVisibility(pagedItems.loadState.refresh !is LoadState.Loading) {
                val itemCount = pagedItems.itemCount
                if (itemCount > 0) {
                    Text(
                        "$itemCount product${if (itemCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            // ── Content ──────────────────────────────────────────────────────
            Box(Modifier.fillMaxSize()) {
                when (pagedItems.loadState.refresh) {
                    is LoadState.Loading -> LoadingView()
                    is LoadState.Error   -> ErrorState(
                        onRetry = { pagedItems.retry() }
                    )
                    else -> {
                        if (pagedItems.itemCount == 0 && pagedItems.loadState.append.endOfPaginationReached) {
                            EmptyStateView(
                                icon     = Icons.Rounded.Inventory2,
                                title    = "No products found",
                                subtitle = if (state.searchQuery.isBlank()) "Tap + to add your first product"
                                else "No results for \"${state.searchQuery}\""
                            )
                        } else {
                            if (windowSize == AppWindowSize.COMPACT) {
                                ProductListContent(
                                    items          = pagedItems,
                                    showCostPrice  = state.showCostPrice,
                                    currencySymbol = state.currencySymbol,
                                    onEdit         = { viewModel.onIntent(InventoryIntent.ShowEditSheet(it)) },
                                    onStockUpdate  = { viewModel.onIntent(InventoryIntent.ShowStockDialog(it.id)) },
                                    onDelete       = { viewModel.onIntent(InventoryIntent.ConfirmDelete(it.id)) }
                                )
                            } else {
                                ProductGridContent(
                                    items          = pagedItems,
                                    showCostPrice  = state.showCostPrice,
                                    currencySymbol = state.currencySymbol,
                                    onEdit         = { viewModel.onIntent(InventoryIntent.ShowEditSheet(it)) },
                                    onStockUpdate  = { viewModel.onIntent(InventoryIntent.ShowStockDialog(it.id)) },
                                    onDelete       = { viewModel.onIntent(InventoryIntent.ConfirmDelete(it.id)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Bottom sheet ─────────────────────────────────────────────────────────
    if (state.showProductSheet) {
        ProductFormBottomSheet(
            state     = state,
            viewModel = viewModel
        )
    }

    // ── Stock dialog ─────────────────────────────────────────────────────────
    state.showStockDialog?.let { productId ->
        // We need the product — search in paged items snapshot
        val product = (0 until pagedItems.itemCount)
            .mapNotNull { pagedItems.peek(it) }
            .find { it.id == productId }
        product?.let {
            StockUpdateDialog(
                product   = it,
                onConfirm = { newStock -> viewModel.onIntent(InventoryIntent.UpdateStock(productId, newStock)) },
                onDismiss = { viewModel.onIntent(InventoryIntent.ShowStockDialog("")) }
            )
        }
    }

    // ── Delete confirm ────────────────────────────────────────────────────────
    state.showDeleteConfirm?.let {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(InventoryIntent.ConfirmDelete("")) },
            icon             = { Icon(Icons.Rounded.DeleteOutline, null, tint = Danger) },
            title            = { Text("Remove Product?", fontWeight = FontWeight.Bold) },
            text             = { Text("This product will be removed from your inventory.") },
            confirmButton    = {
                Button(
                    onClick = { viewModel.onIntent(InventoryIntent.DeleteProduct) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Danger)
                ) { Text("Remove") }
            },
            dismissButton    = {
                TextButton(onClick = { viewModel.onIntent(InventoryIntent.ConfirmDelete("")) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun InventoryHeader(lowStockCount: Int, currencySymbol: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
//            Text(
//                "Inventory",
//                style      = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold,
//                color      = TextPrimary
//            )
        }
        if (lowStockCount > 0) {
            BadgeChip(
                text           = "⚠ $lowStockCount low stock",
                containerColor = DangerContainer,
                contentColor   = Danger
            )
        }
    }
}

@Composable
private fun ProductListContent(
    items         : LazyPagingItems<Product>,
    showCostPrice : Boolean,
    currencySymbol: String,
    onEdit        : (Product) -> Unit,
    onStockUpdate : (Product) -> Unit,
    onDelete      : (Product) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(bottom = 88.dp)
    ) {
        items(
            count    = items.itemCount,
            key      = items.itemKey { it.id }
        ) { index ->
            val product = items[index] ?: return@items
            InventoryListCard(
                product        = product,
                showCostPrice  = showCostPrice,
                currencySymbol = currencySymbol,
                onEdit         = { onEdit(product) },
                onStockUpdate  = { onStockUpdate(product) },
                onDelete       = { onDelete(product) }
            )
        }
        if (items.loadState.append is LoadState.Loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Primary, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun ProductGridContent(
    items         : LazyPagingItems<Product>,
    showCostPrice : Boolean,
    currencySymbol: String,
    onEdit        : (Product) -> Unit,
    onStockUpdate : (Product) -> Unit,
    onDelete      : (Product) -> Unit,
) {
    LazyVerticalGrid(
        columns             = GridCells.Adaptive(minSize = 220.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding      = PaddingValues(bottom = 88.dp)
    ) {
        items(
            count = items.itemCount,
            key   = items.itemKey { it.id }
        ) { index ->
            val product = items[index] ?: return@items
            InventoryGridCard(
                product        = product,
                showCostPrice  = showCostPrice,
                currencySymbol = currencySymbol,
                onEdit         = { onEdit(product) },
                onStockUpdate  = { onStockUpdate(product) },
                onDelete       = { onDelete(product) }
            )
        }
        if (items.loadState.append is LoadState.Loading) {
            item {
                Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Primary, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
    EmptyStateView(
        icon     = Icons.Rounded.ErrorOutline,
        title    = "Something went wrong",
        subtitle = "Tap to try again",
        action   = {
            TextButton(onClick = onRetry) { Text("Retry", color = Primary) }
        }
    )
}

@Composable
private fun StockUpdateDialog(
    product  : Product,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var newStock by remember { mutableStateOf(product.stock.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    product.name, style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Current: ${product.stock} units",
                    style = MaterialTheme.typography.bodySmall, color = TextMuted
                )
                OutlinedTextField(
                    value = newStock,
                    onValueChange = { newStock = it },
                    label = { Text("New Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { newStock.toIntOrNull()?.let { onConfirm(it) } },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}