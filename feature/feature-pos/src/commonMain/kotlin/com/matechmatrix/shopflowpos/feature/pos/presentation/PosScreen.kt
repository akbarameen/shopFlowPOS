package com.matechmatrix.shopflowpos.feature.pos.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.paging.LoadState
import com.matechmatrix.shopflowpos.core.common.platform.rememberReceiptSharer
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.ReceiptBuilder
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
@Composable
fun PosScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : PosViewModel = koinViewModel()
) {
    val state         by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }
    val pagedProducts = state.pagedProducts.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is PosEffect.ShowToast) snackbarState.showSnackbar(e.message)
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarState) },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        if (windowSize == AppWindowSize.COMPACT) {
            PosPhoneLayout(
                state         = state,
                pagedProducts = pagedProducts,
                viewModel     = viewModel,
                modifier      = Modifier.padding()
            )
        } else {
            PosWideLayout(
                state         = state,
                pagedProducts = pagedProducts,
                viewModel     = viewModel,
                modifier      = Modifier
            )
        }
    }

    // ── Checkout sheet ────────────────────────────────────────────────────────
    if (state.showCheckoutSheet) {
        CheckoutBottomSheet(state = state, viewModel = viewModel)
    }

    // ── Receipt dialog ────────────────────────────────────────────────────────
    state.receiptData?.let { receipt ->
        ReceiptDialog(
            receipt   = receipt,
            currency  = state.currencySymbol,
            onNewSale = { viewModel.onIntent(PosIntent.DismissReceipt) }
        )
    }

    // ── Error dialog ──────────────────────────────────────────────────────────
    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(PosIntent.ClearError) },
            icon    = { Icon(Icons.Rounded.ErrorOutline, null, tint = Danger) },
            title   = { Text("Error", fontWeight = FontWeight.Bold) },
            text    = { Text(err) },
            confirmButton = {
                Button(
                    onClick = { viewModel.onIntent(PosIntent.ClearError) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("OK") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Phone Layout
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PosPhoneLayout(
    state         : PosState,
    pagedProducts : LazyPagingItems<Product>,
    viewModel     : PosViewModel,
    modifier      : Modifier = Modifier
) {
    var showCartSheet by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProductSearchBar(state, viewModel)
            CategoryFilterRow(state, viewModel)
            ProductGrid(pagedProducts = pagedProducts, viewModel = viewModel, compact = true)
        }

        // Floating cart button
        AnimatedVisibility(
            visible  = !state.cartIsEmpty,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).navigationBarsPadding(),
            enter    = fadeIn() + slideInVertically { it }
        ) {
            Surface(
                onClick          = { showCartSheet = true },
                modifier         = Modifier.fillMaxWidth().height(62.dp),
                shape            = RoundedCornerShape(18.dp),
                color            = Primary,
                shadowElevation  = 10.dp,
                tonalElevation   = 4.dp
            ) {
                Row(
                    Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier.size(34.dp).clip(CircleShape).background(Color.White.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${state.itemCount}", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Text("Review Order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(
                        "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}",
                        color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp
                    )
                }
            }
        }
    }

    if (showCartSheet) {
        CartBottomSheet(
            state     = state,
            viewModel = viewModel,
            onDismiss = { showCartSheet = false },
            onCheckout = {
                showCartSheet = false
                viewModel.onIntent(PosIntent.OpenCheckout)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Wide Layout (Tablet / Desktop)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PosWideLayout(
    state         : PosState,
    pagedProducts : LazyPagingItems<Product>,
    viewModel     : PosViewModel,
    modifier      : Modifier = Modifier
) {
    Row(modifier.fillMaxSize()) {
        Column(
            Modifier.weight(0.6f).fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductSearchBar(state, viewModel)
            CategoryFilterRow(state, viewModel)
            ProductGrid(pagedProducts = pagedProducts, viewModel = viewModel, compact = false)
        }
        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = BorderFaint)
        CartPanel(state = state, viewModel = viewModel, modifier = Modifier.weight(0.4f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Search + Filter
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductSearchBar(state: PosState, viewModel: PosViewModel) {
    TextField(
        value         = state.searchQuery,
        onValueChange = { viewModel.onIntent(PosIntent.Search(it)) },
        modifier      = Modifier.fillMaxWidth(),
        placeholder   = { Text("Search by name, IMEI, barcode…", color = TextMuted) },
        leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
        trailingIcon  = if (state.searchQuery.isNotBlank()) ({
            IconButton(onClick = { viewModel.onIntent(PosIntent.Search("")) }) {
                Icon(Icons.Rounded.Clear, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }) else null,
        singleLine    = true,
        shape         = RoundedCornerShape(14.dp),
        colors        = TextFieldDefaults.colors(
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

@Composable
private fun CategoryFilterRow(state: PosState, viewModel: PosViewModel) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding        = PaddingValues(horizontal = 2.dp)
    ) {
        item {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick  = { viewModel.onIntent(PosIntent.FilterCategory(null)) },
                label    = { Text("All") },
                colors   = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor     = Color.White
                )
            )
        }
        ProductCategory.entries.forEach { cat ->
            item(key = cat.name) {
                val sel = state.selectedCategory == cat
                FilterChip(
                    selected = sel,
                    onClick  = { viewModel.onIntent(PosIntent.FilterCategory(if (sel) null else cat)) },
                    label    = { Text("${cat.emoji} ${cat.displayName}") },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor     = Color.White
                    )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Product Grid (paged)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProductGrid(
    pagedProducts : LazyPagingItems<Product>,
    viewModel     : PosViewModel,
    compact       : Boolean
) {
    when (pagedProducts.loadState.refresh) {
        is LoadState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Primary, modifier = Modifier.size(32.dp))
        }
        is LoadState.Error   -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = Danger, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { pagedProducts.retry() }) { Text("Retry", color = Primary) }
            }
        }
        else -> {
            if (pagedProducts.itemCount == 0 && pagedProducts.loadState.append.endOfPaginationReached) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.SearchOff, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Text("No products found", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns               = GridCells.Adaptive(if (compact) 110.dp else 150.dp),
                    verticalArrangement   = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding        = PaddingValues(bottom = 100.dp)
                ) {
                    items(count = pagedProducts.itemCount, key = pagedProducts.itemKey { it.id }) { i ->
                        val product = pagedProducts[i] ?: return@items
                        PosProductCard(
                            product = product,
                            inCart  = 0, // resolved below — peek is non-null
                            onClick = { viewModel.onIntent(PosIntent.AddToCart(product)) }
                        )
                    }
                    if (pagedProducts.loadState.append is LoadState.Loading) {
                        item {
                            Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(20.dp), color = Primary, strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PosProductCard(product: Product, inCart: Int, onClick: () -> Unit) {
    val outOfStock = product.stock <= 0 && !product.isImeiTracked
    Card(
        modifier  = Modifier.fillMaxWidth().aspectRatio(0.85f).clickable(enabled = !outOfStock, onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (outOfStock) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        border    = if (inCart > 0) BorderStroke(2.dp, Primary) else BorderStroke(0.5.dp, BorderFaint),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    Modifier.size(38.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (inCart > 0) Primary else PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Inventory2, null,
                        tint     = if (inCart > 0) Color.White else Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        product.name,
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = if (outOfStock) TextMuted else TextPrimary,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (!product.imei.isNullOrBlank()) {
                        Text(
                            product.imei!!,
                            style   = MaterialTheme.typography.labelSmall,
                            color   = Primary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        CurrencyFormatter.formatRs(product.sellingPrice),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = if (outOfStock) TextMuted else Primary
                    )
                    Text(
                        if (outOfStock) "Out of stock"
                        else if (product.isImeiTracked) "1 unit"
                        else "${product.stock} in stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (outOfStock) Danger else TextMuted
                    )
                }
            }
            if (inCart > 0) {
                Surface(
                    modifier       = Modifier.align(Alignment.TopEnd).padding(8.dp).size(22.dp),
                    shape          = CircleShape,
                    color          = Accent,
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$inCart", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cart Panel (wide layout)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartPanel(state: PosState, viewModel: PosViewModel, modifier: Modifier) {
    Column(
        modifier            = modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "🛒 Cart (${state.itemCount})",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!state.cartIsEmpty) {
                TextButton(onClick = { viewModel.onIntent(PosIntent.ClearCart) }) {
                    Text("Clear", color = Danger, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (state.cartIsEmpty) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.ShoppingCartCheckout, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text("Cart is empty", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.cart.size, key = { state.cart[it].product.id }) { i ->
                    CartItemRow(state.cart[i], viewModel)
                }
            }
            HorizontalDivider(color = BorderFaint)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (state.taxRate > 0) {
                    SummaryRow("Subtotal", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.subtotal)}")
                    SummaryRow("Tax (${state.taxRate.toInt()}%)", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.taxAmount)}")
                }
                SummaryRow(
                    label  = "Total",
                    value  = "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}",
                    bold   = true
                )
            }
            Button(
                onClick   = { viewModel.onIntent(PosIntent.OpenCheckout) },
                modifier  = Modifier.fillMaxWidth().height(56.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Rounded.PointOfSale, null)
                Spacer(Modifier.width(12.dp))
                Text("Checkout", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = TextPrimary)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (bold) Primary else TextPrimary)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cart Item Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CartItemRow(item: CartItem, viewModel: PosViewModel) {
    var showDiscountField by remember(item.product.id) { mutableStateOf(false) }
    var discountInput     by remember(item.product.id) { mutableStateOf(if (item.discount > 0) item.discount.toLong().toString() else "") }

    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Inventory2, null, tint = Primary, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    item.product.name,
                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (!item.product.imei.isNullOrBlank()) {
                    Text(item.product.imei!!, style = MaterialTheme.typography.labelSmall, color = Primary)
                }
                Text(
                    "${CurrencyFormatter.formatRs(item.lineTotal)}",
                    style = MaterialTheme.typography.labelSmall, color = TextMuted
                )
            }

            // Quantity controls
            if (!item.product.isImeiTracked) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick  = { viewModel.onIntent(PosIntent.ChangeQty(item.product.id, -1)) },
                        modifier = Modifier.size(26.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
                    ) { Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(12.dp)) }
                    Text("${item.quantity}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold,
                        modifier = Modifier.widthIn(min = 20.dp), textAlign = TextAlign.Center)
                    IconButton(
                        onClick  = { viewModel.onIntent(PosIntent.ChangeQty(item.product.id, +1)) },
                        modifier = Modifier.size(26.dp).clip(CircleShape).background(Primary)
                    ) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                }
            }

            // Discount toggle + remove
            Row {
                IconButton(
                    onClick  = { showDiscountField = !showDiscountField },
                    modifier = Modifier.size(26.dp)
                ) { Icon(Icons.Rounded.Discount, null, tint = if (item.discount > 0) Accent else TextMuted, modifier = Modifier.size(14.dp)) }
                IconButton(
                    onClick  = { viewModel.onIntent(PosIntent.RemoveFromCart(item.product.id)) },
                    modifier = Modifier.size(26.dp)
                ) { Icon(Icons.Rounded.RemoveCircleOutline, null, tint = Danger, modifier = Modifier.size(14.dp)) }
            }
        }

        // Discount input row
        AnimatedVisibility(showDiscountField) {
            OutlinedTextField(
                value         = discountInput,
                onValueChange = { v ->
                    discountInput = v
                    viewModel.onIntent(PosIntent.SetItemDiscount(item.product.id, v.toDoubleOrNull() ?: 0.0))
                },
                label         = { Text("Discount", style = MaterialTheme.typography.labelSmall) },
                prefix        = { Text("Rs. ", style = MaterialTheme.typography.labelSmall) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth().height(54.dp),
                shape         = RoundedCornerShape(10.dp),
                textStyle     = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Accent,
                    unfocusedBorderColor = BorderColor
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Cart Bottom Sheet (phone)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartBottomSheet(
    state     : PosState,
    viewModel : PosViewModel,
    onDismiss : () -> Unit,
    onCheckout: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text("Order (${state.itemCount})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null) }
            }
            LazyColumn(
                modifier            = Modifier.weight(1f, fill = false).heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.cart.size, key = { state.cart[it].product.id }) { i ->
                    CartItemRow(state.cart[i], viewModel)
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(0.3f)),
                shape  = RoundedCornerShape(14.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Total", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}",
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Primary
                    )
                }
            }
            Button(
                onClick   = onCheckout,
                modifier  = Modifier.fillMaxWidth().height(56.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Rounded.PointOfSale, null)
                Spacer(Modifier.width(12.dp))
                Text("Confirm & Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Checkout Bottom Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CheckoutBottomSheet(state: PosState, viewModel: PosViewModel) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(PosIntent.DismissCheckout) },
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        LazyColumn(
            Modifier.fillMaxWidth().navigationBarsPadding(),
            contentPadding      = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Text("Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }

            // ── Order Summary ──────────────────────────────────────────────────
            item {
                Card(
                    shape  = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SummaryRow("Subtotal (${state.itemCount} items)", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.subtotal)}")
                        // Discount field
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text("Discount", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            OutlinedTextField(
                                value         = state.discountAmount,
                                onValueChange = { viewModel.onIntent(PosIntent.SetDiscount(it)) },
                                modifier      = Modifier.width(130.dp),
                                singleLine    = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                prefix        = { Text("Rs. ") },
                                textStyle     = MaterialTheme.typography.bodySmall,
                                shape         = RoundedCornerShape(8.dp),
                                colors        = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor   = Primary,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                        }
                        if (state.taxRate > 0) {
                            SummaryRow("Tax (${state.taxRate.toInt()}%)", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.taxAmount)}")
                        }
                        HorizontalDivider(color = BorderFaint)
                        SummaryRow(
                            "Total",
                            "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}",
                            bold = true
                        )
                    }
                }
            }

            // ── Customer selector ──────────────────────────────────────────────
            item {
                Text("Customer (optional)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextMuted)
                Spacer(Modifier.height(6.dp))
                CustomerDropdown(state, viewModel)
            }

            // ── Payment method ─────────────────────────────────────────────────
            item {
                Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextMuted)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentMethod.entries.forEach { method ->
                        val sel = state.paymentMethod == method
                        Surface(
                            onClick  = { viewModel.onIntent(PosIntent.SelectPaymentMethod(method)) },
                            modifier = Modifier.weight(1f),
                            shape    = RoundedCornerShape(100.dp),
                            color    = if (sel) Primary else MaterialTheme.colorScheme.surface,
                            border   = if (sel) null else BorderStroke(1.5.dp, BorderColor)
                        ) {
                            Text(
                                method.display,
                                Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                textAlign  = TextAlign.Center,
                                color      = if (sel) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // ── Cash input ─────────────────────────────────────────────────────
            if (state.paymentMethod == PaymentMethod.CASH || state.paymentMethod == PaymentMethod.SPLIT) {
                item {
                    CheckoutTextField("Cash Amount", state.cashPaid, "Rs. ") {
                        viewModel.onIntent(PosIntent.SetCashPaid(it))
                    }
                    if (state.paymentMethod == PaymentMethod.CASH && (state.cashPaid.toDoubleOrNull() ?: 0.0) > 0) {
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Change", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.cashChange)}",
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                                color = if (state.cashChange >= 0) Success else Danger
                            )
                        }
                    }
                    // Multiple cash accounts selector (if >1)
                    if (state.cashAccounts.size > 1) {
                        Spacer(Modifier.height(8.dp))
                        CashAccountDropdown(state, viewModel)
                    }
                }
            }

            // ── Bank input ─────────────────────────────────────────────────────
            if (state.paymentMethod == PaymentMethod.BANK || state.paymentMethod == PaymentMethod.SPLIT) {
                item {
                    CheckoutTextField("Bank / Transfer Amount", state.bankPaid, "Rs. ") {
                        viewModel.onIntent(PosIntent.SetBankPaid(it))
                    }
                    if (state.bankAccounts.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        BankAccountDropdown(state, viewModel)
                    }
                }
            }

            // ── Credit — show due summary ──────────────────────────────────────
            if (state.paymentMethod == PaymentMethod.CREDIT || state.dueAfterPayment > 0) {
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DangerContainer
                    ) {
                        Row(
                            Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Warning, null, tint = Danger, modifier = Modifier.size(16.dp))
                                Text("Due Balance", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Danger)
                            }
                            Text(
                                "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.dueAfterPayment)}",
                                style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = Danger
                            )
                        }
                    }
                }
            }

            // ── Notes ──────────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value         = state.notes,
                    onValueChange = { viewModel.onIntent(PosIntent.SetNotes(it)) },
                    label         = { Text("Notes (optional)") },
                    modifier      = Modifier.fillMaxWidth(),
                    maxLines      = 2,
                    shape         = RoundedCornerShape(10.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Primary,
                        unfocusedBorderColor = BorderColor
                    )
                )
            }

            // ── Complete Sale button ───────────────────────────────────────────
            item {
                Button(
                    onClick   = { viewModel.onIntent(PosIntent.CompleteSale) },
                    enabled   = !state.isProcessing,
                    modifier  = Modifier.fillMaxWidth().height(56.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Success),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Complete Sale  ·  ${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CheckoutTextField(label: String, value: String, prefix: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value           = value,
        onValueChange   = onValueChange,
        label           = { Text(label) },
        prefix          = { Text(prefix) },
        singleLine      = true,
        modifier        = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape           = RoundedCornerShape(10.dp),
        colors          = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Primary,
            unfocusedBorderColor = BorderColor
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerDropdown(state: PosState, viewModel: PosViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value         = state.selectedCustomer?.let { "${it.name}${if (it.phone.isNotBlank()) " · ${it.phone}" else ""}" } ?: "Walk-in Customer",
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Customer") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text    = { Text("None (Walk-in)", color = TextMuted) },
                onClick = { viewModel.onIntent(PosIntent.SelectCustomer(null)); expanded = false }
            )
            state.customers.forEach { c ->
                DropdownMenuItem(
                    text    = {
                        Column {
                            Text(c.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (c.phone.isNotBlank()) Text(c.phone, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                if (c.outstandingBalance > 0) Text(
                                    "Due: ${CurrencyFormatter.formatRs(c.outstandingBalance)}",
                                    style = MaterialTheme.typography.labelSmall, color = Danger, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    onClick = { viewModel.onIntent(PosIntent.SelectCustomer(c)); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashAccountDropdown(state: PosState, viewModel: PosViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value         = state.selectedCashAccount?.name ?: "Cash",
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Cash Account") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.cashAccounts.forEach { acc ->
                DropdownMenuItem(
                    text    = { Text(acc.name) },
                    onClick = { viewModel.onIntent(PosIntent.SelectCashAccount(acc.id)); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankAccountDropdown(state: PosState, viewModel: PosViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value         = state.selectedBankAccount?.let { "${it.bankName} · ${it.accountNumber}" } ?: "Select Bank",
            onValueChange = {},
            readOnly      = true,
            label         = { Text("Bank Account") },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            shape         = RoundedCornerShape(10.dp),
            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.bankAccounts.forEach { acc ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("${acc.bankName} — ${acc.accountNumber}", fontWeight = FontWeight.SemiBold)
                            Text(acc.accountTitle, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    },
                    onClick = { viewModel.onIntent(PosIntent.SelectBankAccount(acc)); expanded = false }
                )
            }
        }
    }
}


