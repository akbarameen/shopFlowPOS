package com.matechmatrix.shopflowpos.feature.pos.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.BankAccount
import com.matechmatrix.shopflowpos.core.model.CartItem
import com.matechmatrix.shopflowpos.core.model.Customer
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.PaymentMethod
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : PosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCartSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PosEffect.ShowToast -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Box(Modifier.fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = Primary)
                return@Box
            }
            if (windowSize == AppWindowSize.COMPACT) {
                PosPhoneLayout(state = state, viewModel = viewModel, onShowCart = { showCartSheet = true })
            } else {
                PosWideLayout(state = state, viewModel = viewModel)
            }
        }
    }

    if (showCartSheet && windowSize == AppWindowSize.COMPACT) {
        CartBottomSheet(
            state = state,
            viewModel = viewModel,
            onDismiss = { showCartSheet = false },
            onCheckout = {
                showCartSheet = false
                viewModel.onIntent(PosIntent.OpenCheckout)
            }
        )
    }

    if (state.showCheckoutSheet) {
        CheckoutBottomSheet(state = state, viewModel = viewModel)
    }

    if (state.showSuccessDialog && state.lastSale != null) {
        SaleSuccessDialog(state = state, onDismiss = { viewModel.onIntent(PosIntent.DismissSuccess) })
    }

    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = { /* viewModel.onIntent(PosIntent.ClearError) */ },
            title = { Text("Error", fontWeight = FontWeight.Bold) },
            text  = { Text(err) },
            confirmButton = { TextButton(onClick = { /* viewModel.onIntent(PosIntent.ClearError) */ }) { Text("OK") } }
        )
    }
}

// ─── Phone layout ─────────────────────────────────────────────────────────────
@Composable
private fun PosPhoneLayout(state: PosState, viewModel: PosViewModel, onShowCart: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProductSearchBar(state, viewModel)
            CategoryFilterRow(state, viewModel)
            ProductGrid(state, viewModel, compact = true)
        }

        // Floating action button for cart on mobile
        if (state.cart.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Surface(
                    onClick = onShowCart,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Primary,
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${state.itemCount}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text("Review Order", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(
                            CurrencyFormatter.formatRs(state.cartTotal),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ─── Wide layout ──────────────────────────────────────────────────────────────
@Composable
private fun PosWideLayout(state: PosState, viewModel: PosViewModel) {
    Row(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(0.6f).fillMaxHeight().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductSearchBar(state, viewModel)
            CategoryFilterRow(state, viewModel)
            ProductGrid(state, viewModel, compact = false)
        }
        VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = BorderColor)
        CartPanel(state = state, viewModel = viewModel, modifier = Modifier.weight(0.4f))
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────
@Composable
private fun ProductSearchBar(state: PosState, viewModel: PosViewModel) {
    TextField(
        value = state.searchQuery,
        onValueChange = { viewModel.onIntent(PosIntent.Search(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search or scan barcode...", color = TextMuted) },
        leadingIcon  = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (state.searchQuery.isNotBlank()) {
                IconButton(onClick = { viewModel.onIntent(PosIntent.Search("")) }) {
                    Icon(Icons.Rounded.Clear, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor   = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            focusedIndicatorColor   = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
    )
}

// ─── Category filter ──────────────────────────────────────────────────────────
@Composable
private fun CategoryFilterRow(state: PosState, viewModel: PosViewModel) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 2.dp)) {
        item {
            FilterChip(
                selected = state.selectedCategory == null,
                onClick = { viewModel.onIntent(PosIntent.FilterCategory(null)) },
                label = { Text("All") }
            )
        }
        items(ProductCategory.entries) { cat ->
            val selected = state.selectedCategory == cat
            FilterChip(
                selected = selected,
                onClick = { viewModel.onIntent(PosIntent.FilterCategory(if (selected) null else cat)) },
                label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) }
            )
        }
    }
}

// ─── Product grid ─────────────────────────────────────────────────────────────
@Composable
private fun ProductGrid(state: PosState, viewModel: PosViewModel, compact: Boolean) {
    if (state.filteredProducts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.SearchOff, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                Text("No products found", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
            }
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = if (compact) 110.dp else 150.dp),
        verticalArrangement   = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 100.dp) // Avoid overlap with floating cart
    ) {
        items(state.filteredProducts, key = { it.id }) { product ->
            ProductCard(
                product = product,
                inCart  = state.cart.find { it.product.id == product.id }?.quantity ?: 0
            ) { viewModel.onIntent(PosIntent.AddToCart(product)) }
        }
    }
}

@Composable
private fun ProductCard(product: Product, inCart: Int, onClick: () -> Unit) {
    val isOutOfStock = product.stock == 0
    Card(
        modifier  = Modifier.fillMaxWidth().aspectRatio(0.85f).clickable(enabled = !isOutOfStock, onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = if (isOutOfStock) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface),
        border    = if (inCart > 0) BorderStroke(2.dp, Primary) else BorderStroke(1.dp, BorderFaint),
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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
                        style    = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color    = if (isOutOfStock) TextMuted else TextPrimary,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        CurrencyFormatter.formatRs(product.sellingPrice),
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color      = if (isOutOfStock) TextMuted else Primary
                    )
                    Text(
                        if (isOutOfStock) "Out of stock" else "${product.stock} in stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOutOfStock) Danger else TextMuted
                    )
                }
            }
            if (inCart > 0) {
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).size(24.dp),
                    shape = CircleShape, color = Accent, shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$inCart", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ─── Cart components ──────────────────────────────────────────────────────────
@Composable
private fun CartPanel(state: PosState, viewModel: PosViewModel, modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxHeight().background(MaterialTheme.colorScheme.surface).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("🛒 Shopping Cart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (state.cart.isNotEmpty()) {
                TextButton(onClick = { viewModel.onIntent(PosIntent.ClearCart) }) {
                    Text("Clear", color = Danger, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (state.cart.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.ShoppingCartCheckout, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text("Cart is empty", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.cart, key = { it.product.id }) { item ->
                    CartItemRow(item = item, viewModel = viewModel)
                }
            }

            HorizontalDivider(color = BorderFaint)

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Items", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Text("${state.itemCount}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        CurrencyFormatter.formatRs(state.cartTotal),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold, color = Primary
                    )
                }
            }

            Button(
                onClick = { viewModel.onIntent(PosIntent.OpenCheckout) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Rounded.PointOfSale, null)
                Spacer(Modifier.width(12.dp))
                Text("Checkout Now", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartBottomSheet(state: PosState, viewModel: PosViewModel, onDismiss: () -> Unit, onCheckout: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Review Order (${state.itemCount})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, null) }
            }

            LazyColumn(modifier = Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.cart, key = { it.product.id }) { item ->
                    CartItemRow(item = item, viewModel = viewModel)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total to Pay", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(CurrencyFormatter.formatRs(state.cartTotal), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Primary)
                }
            }

            Button(
                onClick = onCheckout,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Rounded.PointOfSale, null)
                Spacer(Modifier.width(12.dp))
                Text("Confirm & Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun CartItemRow(item: CartItem, viewModel: PosViewModel) {
    Row(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Inventory2, null, tint = Primary, modifier = Modifier.size(20.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                item.product.name,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            item.product.imei?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall, color = TextMuted
                )
            }
            Text(
                CurrencyFormatter.formatRs(item.product.sellingPrice),
                style = MaterialTheme.typography.labelSmall, color = TextMuted
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = { viewModel.onIntent(PosIntent.ChangeQty(item.product.id, -1)) },
                modifier = Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)
            ) { Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(14.dp)) }

            Text(
                "${item.quantity}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 24.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { viewModel.onIntent(PosIntent.ChangeQty(item.product.id, +1)) },
                modifier = Modifier.size(28.dp).clip(CircleShape).background(Primary)
            ) { Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
        }
    }
}
// ─── Checkout bottom sheet ────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutBottomSheet(state: PosState, viewModel: PosViewModel) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(PosIntent.DismissCheckout) },
        sheetState       = sheetState,
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Handle bar
//            Box(Modifier.width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(BorderColor).align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(4.dp))

            Text("Checkout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = TextPrimary)

            // Order summary card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal (${state.itemCount} items)", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(CurrencyFormatter.formatRs(state.cartTotal), style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Discount", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        OutlinedTextField(
                            value = state.discountAmount,
                            onValueChange = { viewModel.onIntent(PosIntent.SetDiscount(it)) },
                            modifier = Modifier.width(130.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            prefix = { Text("Rs. ") },
                            textStyle = MaterialTheme.typography.bodySmall,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                    }
                    HorizontalDivider(color = BorderFaint)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            CurrencyFormatter.formatRs(state.netTotal),
                            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Primary
                        )
                    }
                }
            }

            // Payment method
            Text("Payment Method", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = TextMuted)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.values().forEach { method ->
                    val isSelected = state.paymentMethod == method
                    Surface(
                        onClick = { viewModel.onIntent(PosIntent.SelectPaymentMethod(method)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(100.dp),
                        color = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else BorderStroke(1.5.dp, BorderColor)
                    ) {
                        Text(
                            method.name.lowercase().replaceFirstChar { it.uppercase() },
                            Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                    }
                }
            }

            // Cash input
            if (state.paymentMethod == PaymentMethod.CASH || state.paymentMethod == PaymentMethod.PARTIAL) {
                OutlinedTextField(
                    value = state.cashPaid,
                    onValueChange = { viewModel.onIntent(PosIntent.SetCashPaid(it)) },
                    label = { Text("Cash Amount") }, prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                )
                val paid  = state.cashPaid.toLongOrNull() ?: 0L
                val change = (paid - state.netTotal.toLong()).coerceAtLeast(0L)
                if (state.paymentMethod == PaymentMethod.CASH && paid > 0) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Change", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(
                            CurrencyFormatter.formatRs(change),
                            style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold,
                            color = if (change >= 0) Success else Danger
                        )
                    }
                }
            }

            // Bank input
            if (state.paymentMethod == PaymentMethod.BANK_TRANSFER || state.paymentMethod == PaymentMethod.PARTIAL) {
                OutlinedTextField(
                    value = state.bankPaid,
                    onValueChange = { viewModel.onIntent(PosIntent.SetBankPaid(it)) },
                    label = { Text("Bank/Transfer Amount") }, prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                )
                if (state.bankAccounts.isNotEmpty()) BankAccountSelector(state, viewModel)
            }

            // Customer selector for credit
            if (state.paymentMethod == PaymentMethod.CREDIT) {
                CustomerSelector(state, viewModel)
            }

            // Notes
            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onIntent(PosIntent.SetNotes(it)) },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(), maxLines = 2, shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
            )

            // Complete sale button
            Button(
                onClick  = { viewModel.onIntent(PosIntent.CompleteSale) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = Success),
                enabled  = !state.isProcessing,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (state.isProcessing) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Complete Sale  •  ${CurrencyFormatter.formatRs(state.netTotal)}",
                        style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankAccountSelector(state: PosState, viewModel: PosViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = state.selectedBankAccount?.accountNumber ?: "Select bank account",
            onValueChange = {}, readOnly = true,
            label = { Text("Bank Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.bankAccounts.forEach { acct ->
                DropdownMenuItem(
                    text    = { Text("${acct.accountNumber} (${acct.bankName})") },
                    onClick = { viewModel.onIntent(PosIntent.SelectBankAccount(acct)); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerSelector(state: PosState, viewModel: PosViewModel) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = state.selectedCustomer?.name ?: "Select customer (required for credit)",
            onValueChange = {}, readOnly = true,
            label = { Text("Customer") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { viewModel.onIntent(PosIntent.SelectCustomer(null)); expanded = false })
            state.customers.forEach { c ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(c.name, style = MaterialTheme.typography.bodySmall)
                            Text(c.phone ?: "", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                    },
                    onClick = { viewModel.onIntent(PosIntent.SelectCustomer(c)); expanded = false }
                )
            }
        }
    }
}

// ─── Sale success dialog ──────────────────────────────────────────────────────
@Composable
private fun SaleSuccessDialog(state: PosState, onDismiss: () -> Unit) {
    val sale = state.lastSale ?: return
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                Modifier.size(60.dp).clip(RoundedCornerShape(18.dp)).background(SuccessContainer),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.CheckCircle, null, tint = Success, modifier = Modifier.size(30.dp)) }
        },
        title = { Text("Sale Complete!", textAlign = TextAlign.Center, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(shape = RoundedCornerShape(100.dp), color = PrimaryContainer) {
                    Text(sale.invoiceNumber, Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold)
                }
                Text(
                    CurrencyFormatter.formatRs(sale.soldAt),
                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = TextPrimary
                )
                Text(sale.paymentMethod.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall, color = TextMuted)
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors  = ButtonDefaults.buttonColors(containerColor = Primary),
                shape   = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("New Sale", fontWeight = FontWeight.Bold) }
        }
    )
}