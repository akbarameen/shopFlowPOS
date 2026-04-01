package com.matechmatrix.shopflowpos.feature.purchase.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.matechmatrix.shopflowpos.core.common.platform.rememberReceiptSharer
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.common.util.DateTimeUtils
import com.matechmatrix.shopflowpos.core.common.util.PurchaseReceiptBuilder
import com.matechmatrix.shopflowpos.core.model.*
import com.matechmatrix.shopflowpos.core.model.enums.AccountType
import com.matechmatrix.shopflowpos.core.model.enums.PaymentStatus
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.components.EmptyStateView
import com.matechmatrix.shopflowpos.core.ui.components.LoadingView
import com.matechmatrix.shopflowpos.core.ui.theme.*
import com.matechmatrix.shopflowpos.feature.purchase.domain.model.PurchaseSourceType
import com.matechmatrix.shopflowpos.feature.purchase.domain.repository.PurchaseCartItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PurchaseScreen(
    windowSize    : AppWindowSize,
    navigateChild : (String) -> Unit = {},
    viewModel     : PurchaseViewModel = koinViewModel()
) {
    val state         by viewModel.state.collectAsStateWithLifecycle()
    val snackbarState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { e ->
            if (e is PurchaseEffect.ShowToast) snackbarState.showSnackbar(e.message)
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarState) },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier            = Modifier.fillMaxSize()
                .padding(horizontal = when (windowSize) { AppWindowSize.EXPANDED -> 28.dp; AppWindowSize.MEDIUM -> 20.dp; else -> 16.dp }, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
//            Text("Purchases", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextPrimary)

            // ── Tab row ──────────────────────────────────────────────────────
            TabRow(selectedTabIndex = state.activeTab.ordinal, containerColor = MaterialTheme.colorScheme.surface, contentColor = Primary, modifier = Modifier.clip(RoundedCornerShape(12.dp))) {
                listOf("New Purchase" to Icons.Rounded.AddShoppingCart, "History" to Icons.Rounded.Receipt).forEachIndexed { i, (label, icon) ->
                    Tab(
                        selected = state.activeTab.ordinal == i,
                        onClick  = { viewModel.onIntent(PurchaseIntent.SwitchTab(PurchaseTab.entries[i])) },
                        text     = { Text(label, fontWeight = if (state.activeTab.ordinal == i) FontWeight.Bold else FontWeight.Normal) },
                        icon     = { Icon(icon, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            when (state.activeTab) {
                PurchaseTab.NEW_ORDER -> NewPurchaseContent(state, viewModel, windowSize)
                PurchaseTab.HISTORY   -> HistoryContent(state, viewModel, windowSize)
            }
        }
    }

    // ── Product sheet ─────────────────────────────────────────────────────────
    if (state.showProductSheet) AddProductSheet(state, viewModel)

    // ── Payment sheet ─────────────────────────────────────────────────────────
    if (state.showPaymentSheet) PaymentSheet(state, viewModel)

    // ── Receipt dialog ─────────────────────────────────────────────────────────
    state.receipt?.let { PurchaseReceiptDialog(it, state.currencySymbol) { viewModel.onIntent(PurchaseIntent.DismissReceipt) } }

    // ── Due pay dialog ─────────────────────────────────────────────────────────
    state.showDuePayDialog?.let { DuePayDialog(it, state, viewModel) }

    // ── Error ──────────────────────────────────────────────────────────────────
    state.error?.let { err ->
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(PurchaseIntent.ClearError) },
            icon    = { Icon(Icons.Rounded.ErrorOutline, null, tint = Danger) },
            title   = { Text("Error", fontWeight = FontWeight.Bold) },
            text    = { Text(err) },
            confirmButton = {
                Button(onClick = { viewModel.onIntent(PurchaseIntent.ClearError) }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("OK") }
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// New Purchase Content
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewPurchaseContent(state: PurchaseState, viewModel: PurchaseViewModel, windowSize: AppWindowSize) {
    var showSourceDD by remember { mutableStateOf(false) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding      = PaddingValues(bottom = 100.dp)
    ) {
        // ── Step 1: Source type (Supplier / Customer) ─────────────────────────
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("📦 Step 1 — Source", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted)

                    // Supplier / Customer toggle
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(PurchaseSourceType.SUPPLIER to "Supplier", PurchaseSourceType.CUSTOMER to "Customer").forEach { (type, label) ->
                            val sel = state.sourceType == type
                            FilterChip(
                                selected = sel,
                                onClick  = { viewModel.onIntent(PurchaseIntent.SetSourceType(type)) },
                                label    = { Text(label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.weight(1f),
                                colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White)
                            )
                        }
                    }

                    // Search / type name
                    val listToSearch = if (state.sourceType == PurchaseSourceType.SUPPLIER) state.filteredSuppliers else state.filteredCustomers
                    ExposedDropdownMenuBox(expanded = showSourceDD && listToSearch.isNotEmpty(), onExpandedChange = { showSourceDD = it }) {
                        OutlinedTextField(
                            value         = state.sourceQuery,
                            onValueChange = { viewModel.onIntent(PurchaseIntent.SetSourceQuery(it)); showSourceDD = true },
                            label         = { Text(if (state.sourceType == PurchaseSourceType.SUPPLIER) "Supplier *" else "Customer *") },
                            placeholder   = { Text("Search or type new name", color = TextMuted) },
                            leadingIcon   = {
                                Icon(if (state.sourceType == PurchaseSourceType.SUPPLIER) Icons.Rounded.StoreMallDirectory else Icons.Rounded.Person, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon  = if (state.sourceQuery.isNotBlank()) ({
                                IconButton(onClick = { viewModel.onIntent(PurchaseIntent.ClearSource) }) {
                                    Icon(Icons.Rounded.Clear, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }) else null,
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true),
                            shape         = RoundedCornerShape(12.dp),
                            colors        = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = showSourceDD && listToSearch.isNotEmpty(), onDismissRequest = { showSourceDD = false }) {
                            listToSearch.forEach { contact ->
                                val contactName = if (contact is Supplier) contact.name else (contact as Customer).name
                                val contactPhone = if (contact is Supplier) contact.phone else (contact as Customer).phone
                                val contactDue = if (contact is Supplier) contact.outstandingBalance else (contact as Customer).outstandingBalance
                                
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(contactName, fontWeight = FontWeight.SemiBold)
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                if (contactPhone.isNotBlank()) Text(contactPhone, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                                if (contactDue > 0) Text("Due: ${CurrencyFormatter.formatCompact(contactDue)}", style = MaterialTheme.typography.labelSmall, color = Danger, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (contact is Supplier) viewModel.onIntent(PurchaseIntent.SelectSupplier(contact))
                                        else viewModel.onIntent(PurchaseIntent.SelectCustomer(contact as Customer))
                                        showSourceDD = false
                                    }
                                )
                            }
                        }
                    }

                    // New source extra fields
                    if (state.sourceQuery.isNotBlank() && state.selectedSource == null) {
                        Text("New ${if (state.sourceType == PurchaseSourceType.SUPPLIER) "supplier" else "customer"} — will be added to your list", style = MaterialTheme.typography.labelSmall, color = Info)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PurchaseField("Phone", state.newSourcePhone, Modifier.weight(1f), KeyboardType.Phone) { viewModel.onIntent(PurchaseIntent.SetNewSourcePhone(it)) }
                            PurchaseField("City", state.newSourceCity, Modifier.weight(1f)) { viewModel.onIntent(PurchaseIntent.SetNewSourceCity(it)) }
                        }
                        PurchaseField("Address", state.newSourceAddress) { viewModel.onIntent(PurchaseIntent.SetNewSourceAddress(it)) }
                    }

                    // Confirmed source chip
                    if (state.selectedSource != null) {
                        Surface(shape = RoundedCornerShape(100.dp), color = PrimaryContainer) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = Primary, modifier = Modifier.size(14.dp))
                                Text(state.selectedSource.name, style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                                if (state.selectedSource.phone.isNotBlank()) Text("· ${state.selectedSource.phone}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                        }
                    }

                    // Optional: Supplier invoice reference
                    if (state.sourceQuery.isNotBlank()) {
                        PurchaseField("Supplier Invoice / Bill Ref (optional)", state.supplierInvoiceRef) {
                            viewModel.onIntent(PurchaseIntent.SetSupplierInvoiceRef(it))
                        }
                    }
                }
            }
        }

        // ── Step 2: Products ──────────────────────────────────────────────────
        item {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("🛒 Step 2 — Products (${state.itemCount})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted)
                        TextButton(onClick = { viewModel.onIntent(PurchaseIntent.ShowProductSheet) }) {
                            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Product", fontWeight = FontWeight.Bold, color = Primary)
                        }
                    }

                    if (state.cartIsEmpty) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Rounded.AddShoppingCart, null, tint = TextFaint, modifier = Modifier.size(36.dp))
                                Text("Tap 'Add Product' to start", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                        }
                    } else {
                        state.cart.forEach { item ->
                            CartRow(item, state.currencySymbol, viewModel)
                        }
                    }
                }
            }
        }

        // ── Step 3: Discount + Notes ──────────────────────────────────────────
        if (!state.cartIsEmpty) {
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("🏷 Step 3 — Discount & Notes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PurchaseField("Discount", state.discountAmount, Modifier.weight(1f), KeyboardType.Decimal, prefix = "Rs. ") {
                                viewModel.onIntent(PurchaseIntent.SetDiscount(it))
                            }
                            PurchaseField("Notes", state.notes, Modifier.weight(1f)) {
                                viewModel.onIntent(PurchaseIntent.SetNotes(it))
                            }
                        }
                    }
                }
            }

            // ── Order summary + Proceed button ────────────────────────────────
            item {
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💰 Step 4 — Payment", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted)
                        SummaryRow("${state.itemCount} items", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.subtotal)}")
                        if (state.discount > 0) SummaryRow("Discount", "-${state.currencySymbol} ${CurrencyFormatter.formatRs(state.discount)}", Danger)
                        HorizontalDivider(color = BorderFaint)
                        SummaryRow("Grand Total", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}", Primary, bold = true)
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick  = { viewModel.onIntent(PurchaseIntent.ShowPaymentSheet) },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Primary),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Icon(Icons.Rounded.Payment, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Proceed to Payment · ${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartRow(item: PurchaseCartItem, currency: String, viewModel: PurchaseViewModel) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(0.4f)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.productName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (!item.imei.isNullOrBlank()) Text(item.imei, style = MaterialTheme.typography.labelSmall, color = Primary)
            if (item.isNewProduct) Text("New → will be added to inventory", style = MaterialTheme.typography.labelSmall, color = Info)
            Text("${item.quantity} × $currency ${CurrencyFormatter.formatRs(item.unitCost)}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
        Text("$currency ${CurrencyFormatter.formatRs(item.totalCost)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Primary)
        // Qty +/-
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { viewModel.onIntent(PurchaseIntent.ChangeCartQty(item.productId, -1)) }, modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface)) {
                Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(10.dp))
            }
            Text("${item.quantity}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 18.dp), textAlign = TextAlign.Center)
            IconButton(onClick = { viewModel.onIntent(PurchaseIntent.ChangeCartQty(item.productId, +1)) }, modifier = Modifier.size(24.dp).clip(CircleShape).background(Primary)) {
                Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(10.dp))
            }
            IconButton(onClick = { viewModel.onIntent(PurchaseIntent.RemoveFromCart(item.productId)) }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Rounded.DeleteOutline, null, tint = Danger, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onBackground, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold, color = color)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Product Sheet (mirrors inventory form)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProductSheet(state: PurchaseState, viewModel: PurchaseViewModel) {
    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(PurchaseIntent.DismissProductSheet) },
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        LazyColumn(
            Modifier.fillMaxWidth().navigationBarsPadding(),
            contentPadding      = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Add Product to Purchase", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = { viewModel.onIntent(PurchaseIntent.DismissProductSheet) }) { Icon(Icons.Rounded.Close, null) }
                }
            }

            // Search existing inventory
            item {
                TextField(
                    value = state.sheetProductSearch, onValueChange = { viewModel.onIntent(PurchaseIntent.SetSheetSearch(it)) },
                    modifier = Modifier.fillMaxWidth(), placeholder = { Text("Search existing product by name, IMEI, barcode…", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                    singleLine = true, shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent)
                )
            }

            // Search results
            if (state.sheetSearchResults.isNotEmpty()) {
                item { Text("Existing Products", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted) }
                items(state.sheetSearchResults.size, key = { state.sheetSearchResults[it].id }) { i ->
                    val prod = state.sheetSearchResults[i]
                    val isSelected = state.sheetSelectedProduct?.id == prod.id
                    Surface(
                        onClick = { viewModel.onIntent(PurchaseIntent.SelectSheetProduct(prod)) },
                        shape   = RoundedCornerShape(10.dp),
                        color   = if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(prod.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (!prod.imei.isNullOrBlank()) Text("IMEI: ${prod.imei}", style = MaterialTheme.typography.labelSmall, color = Primary)
                                Text("${prod.category.displayName} · Stock: ${prod.stock}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Cost: ${CurrencyFormatter.formatRs(prod.costPrice)}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                if (isSelected) Icon(Icons.Rounded.CheckCircle, null, tint = Primary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // New product option
            if (state.sheetProductSearch.isNotBlank()) {
                item {
                    Surface(
                        onClick  = { viewModel.onIntent(PurchaseIntent.SelectSheetProduct(null)) },
                        shape    = RoundedCornerShape(10.dp),
                        color    = if (state.sheetSelectedProduct == null) InfoContainer else MaterialTheme.colorScheme.surfaceVariant.copy(0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AddBox, null, tint = Info, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Add as new product", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Info)
                                Text("\"${state.sheetProductSearch}\" will be added to inventory", style = MaterialTheme.typography.labelSmall, color = Info.copy(0.7f))
                            }
                        }
                    }
                }
            }

            item {
                HorizontalDivider(color = BorderFaint)
            }
            item { Text("Product Details", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted) }

            // Product name
            item {
                PurchaseField("Product Name *", state.sheetProductName) { viewModel.onIntent(PurchaseIntent.SetSheetProductName(it)) }
            }

            // Category selector
            item {
                Text("Category", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProductCategory.entries.forEach { cat ->
                        item(key = cat.name) {
                            val sel = state.sheetProductCategory == cat
                            FilterChip(
                                selected = sel,
                                onClick  = { viewModel.onIntent(PurchaseIntent.SetSheetProductCategory(cat)) },
                                label    = { Text("${cat.emoji} ${cat.displayName}", style = MaterialTheme.typography.labelSmall) },
                                colors   = FilterChipDefaults.filterChipColors(selectedContainerColor = Primary, selectedLabelColor = Color.White)
                            )
                        }
                    }
                }
            }

            // IMEI (if phone/tablet)
            if (state.sheetProductCategory.hasImei) {
                item {
                    PurchaseField("IMEI *", state.sheetProductImei, keyboard = KeyboardType.Number) {
                        if (it.length <= 15) viewModel.onIntent(PurchaseIntent.SetSheetProductImei(it))
                    }
                }
            }

            // Brand
            item { PurchaseField("Brand (optional)", state.sheetProductBrand) { viewModel.onIntent(PurchaseIntent.SetSheetProductBrand(it)) } }

            // Qty + Cost
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PurchaseField("Quantity *", state.sheetProductQty, Modifier.weight(1f), KeyboardType.Number) { viewModel.onIntent(PurchaseIntent.SetSheetProductQty(it)) }
                    PurchaseField("Cost Price *", state.sheetProductCost, Modifier.weight(1f), KeyboardType.Decimal, prefix = "Rs. ") { viewModel.onIntent(PurchaseIntent.SetSheetProductCost(it)) }
                }
            }

            // Line total preview
            val lineTotal = (state.sheetProductQty.toIntOrNull() ?: 0) * (state.sheetProductCost.toDoubleOrNull() ?: 0.0)
            if (lineTotal > 0) {
                item {
                    Surface(shape = RoundedCornerShape(10.dp), color = PrimaryContainer) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Line Total", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Primary)
                            Text("Rs. ${CurrencyFormatter.formatRs(lineTotal)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = Primary)
                        }
                    }
                }
            }

            state.sheetFormError?.let { item { Text(it, style = MaterialTheme.typography.labelSmall, color = Danger, fontWeight = FontWeight.SemiBold) } }

            item {
                Button(
                    onClick   = { viewModel.onIntent(PurchaseIntent.AddSheetItemToCart) },
                    modifier  = Modifier.fillMaxWidth().height(52.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Primary),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    Icon(Icons.Rounded.AddShoppingCart, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add to Purchase Cart", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Payment Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentSheet(state: PurchaseState, viewModel: PurchaseViewModel) {
    var showBankDD by remember { mutableStateOf(false) }
    var showCashDD by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onIntent(PurchaseIntent.DismissPaymentSheet) },
        sheetState       = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor   = MaterialTheme.colorScheme.surface,
        shape            = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        LazyColumn(
            Modifier.fillMaxWidth().navigationBarsPadding(),
            contentPadding      = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Text("Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold) }

            // Summary
            item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SummaryRow("To: ${state.sourceQuery}", "")
                        SummaryRow("${state.itemCount} items", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.subtotal)}")
                        if (state.discount > 0) SummaryRow("Discount", "-${state.currencySymbol} ${CurrencyFormatter.formatRs(state.discount)}", Danger)
                        HorizontalDivider(color = BorderFaint)
                        SummaryRow("Grand Total", "${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}", Primary, true)
                    }
                }
            }

            // Cash
            item {
                Text("Cash Payment", style = MaterialTheme.typography.labelMedium, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                PurchaseField("Cash Amount", state.cashAmount, keyboard = KeyboardType.Decimal, prefix = "Rs. ") { viewModel.onIntent(PurchaseIntent.SetCashAmount(it)) }
                state.selectedCashAccount?.let { acc ->
                    val cashPaid = state.cashPaid
                    val insuf = cashPaid > acc.balance
                    Text(
                        (if (insuf) "⚠ Insufficient! " else "") + "Available: ${state.currencySymbol} ${CurrencyFormatter.formatRs(acc.balance)}",
                        style = MaterialTheme.typography.labelSmall, color = if (insuf) Danger else TextMuted,
                        fontWeight = if (insuf) FontWeight.Bold else FontWeight.Normal
                    )
                }
                // Multiple cash accounts
                if (state.cashAccounts.size > 1) {
                    Spacer(Modifier.height(6.dp))
                    ExposedDropdownMenuBox(expanded = showCashDD, onExpandedChange = { showCashDD = it }) {
                        OutlinedTextField(
                            value = state.selectedCashAccount?.name ?: "Cash", onValueChange = {}, readOnly = true,
                            label = { Text("Cash Account") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showCashDD) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = showCashDD, onDismissRequest = { showCashDD = false }) {
                            state.cashAccounts.forEach { acc ->
                                DropdownMenuItem(text = { Text(acc.name) }, onClick = { viewModel.onIntent(PurchaseIntent.SetCashAccountId(acc.id)); showCashDD = false })
                            }
                        }
                    }
                }
            }

            // Bank
            if (state.bankAccounts.isNotEmpty()) {
                item {
                    Text("Bank Payment (optional)", style = MaterialTheme.typography.labelMedium, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    PurchaseField("Bank Amount", state.bankAmount, keyboard = KeyboardType.Decimal, prefix = "Rs. ") { viewModel.onIntent(PurchaseIntent.SetBankAmount(it)) }
                    Spacer(Modifier.height(6.dp))
                    ExposedDropdownMenuBox(expanded = showBankDD, onExpandedChange = { showBankDD = it }) {
                        OutlinedTextField(
                            value = state.selectedBankAccount?.let { "${it.bankName} · ${it.accountNumber}" } ?: "Select Bank",
                            onValueChange = {}, readOnly = true, label = { Text("Bank Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showBankDD) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = showBankDD, onDismissRequest = { showBankDD = false }) {
                            state.bankAccounts.forEach { bank ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("${bank.bankName} · ${bank.accountNumber}", fontWeight = FontWeight.SemiBold)
                                            val insuf = state.bankPaid > bank.balance
                                            Text("Available: ${state.currencySymbol} ${CurrencyFormatter.formatRs(bank.balance)}", style = MaterialTheme.typography.labelSmall, color = if (insuf) Danger else TextMuted)
                                        }
                                    },
                                    onClick = { viewModel.onIntent(PurchaseIntent.SetBankAccountId(bank.id)); showBankDD = false }
                                )
                            }
                        }
                    }
                }
            }

            // Due info
            item {
                val due = state.dueAfterPay
                if (due > 0) {
                    Surface(shape = RoundedCornerShape(10.dp), color = WarningContainer) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("⏳ Due Payable to ${state.sourceQuery}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Warning)
                                Text("Will appear in Dues → Payable", style = MaterialTheme.typography.labelSmall, color = Warning.copy(0.7f))
                            }
                            Text("${state.currencySymbol} ${CurrencyFormatter.formatRs(due)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = Warning)
                        }
                    }
                }
            }

            // Confirm button
            item {
                val cashOk = state.cashPaid == 0.0 || (state.selectedCashAccount?.balance ?: 0.0) >= state.cashPaid
                val bankOk = state.bankPaid == 0.0 || (state.selectedBankAccount?.balance ?: 0.0) >= state.bankPaid
                val canProceed = !state.isProcessing && cashOk && bankOk

                Button(
                    onClick  = { viewModel.onIntent(PurchaseIntent.ConfirmPurchase) },
                    enabled  = canProceed,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = if (canProceed) Success else MaterialTheme.colorScheme.surfaceVariant),
                    elevation = ButtonDefaults.buttonElevation(4.dp)
                ) {
                    if (state.isProcessing) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    else { Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("Confirm Purchase · ${state.currencySymbol} ${CurrencyFormatter.formatRs(state.netTotal)}", fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// History
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HistoryContent(state: PurchaseState, viewModel: PurchaseViewModel, windowSize: AppWindowSize) {
    if (state.purchaseOrders.isEmpty()) {
        EmptyStateView(icon = Icons.Rounded.Receipt, title = "No purchases yet", subtitle = "Create your first purchase order")
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
        items(state.purchaseOrders.size, key = { state.purchaseOrders[it].id }) { i ->
            val order = state.purchaseOrders[i]
            val statusColor = when (order.paymentStatus) { PaymentStatus.PAID -> Success; PaymentStatus.PARTIAL -> Warning; else -> Danger }
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(order.poNumber, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFE67E22))
                            Text(order.supplierName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
                            if (order.supplierPhone.isNotBlank()) Text(order.supplierPhone, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(DateTimeUtils.formatDate(order.purchasedAt), style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(shape = RoundedCornerShape(100.dp), color = statusColor.copy(0.12f)) {
                                Text(order.paymentStatus.display, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${state.currencySymbol} ${CurrencyFormatter.formatRs(order.totalAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold)
                            if (order.dueAmount > 0) Text("Due: ${state.currencySymbol} ${CurrencyFormatter.formatRs(order.dueAmount)}", style = MaterialTheme.typography.labelSmall, color = Danger, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (order.dueAmount > 0) {
                        Button(
                            onClick = { viewModel.onIntent(PurchaseIntent.ShowDuePayDialog(order)) },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Danger), contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.Payment, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Pay Due: ${state.currencySymbol} ${CurrencyFormatter.formatRs(order.dueAmount)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Due Payment Dialog
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuePayDialog(order: PurchaseOrder, state: PurchaseState, viewModel: PurchaseViewModel) {
    var showBankDD by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { viewModel.onIntent(PurchaseIntent.DismissDuePayDialog) },
        title = { Text("Pay Supplier Due", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(10.dp), color = DangerContainer) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column { Text(order.supplierName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Danger); Text(order.poNumber, style = MaterialTheme.typography.labelSmall, color = Danger.copy(0.7f)) }
                        Text("${state.currencySymbol} ${CurrencyFormatter.formatRs(order.dueAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.ExtraBold, color = Danger)
                    }
                }
                PurchaseField("Amount", state.duePayAmount, keyboard = KeyboardType.Decimal, prefix = "Rs. ") { viewModel.onIntent(PurchaseIntent.SetDuePayAmount(it)) }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(AccountType.CASH to "Cash", AccountType.BANK to "Bank").forEach { (type, label) ->
                        val sel = state.duePayAccountType == type
                        FilterChip(selected = sel, onClick = { viewModel.onIntent(PurchaseIntent.SetDuePayAccountType(type)) }, label = { Text(label) }, modifier = Modifier.weight(1f), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Danger, selectedLabelColor = Color.White))
                    }
                }
                if (state.duePayAccountType == AccountType.CASH) {
                    val acc = state.cashAccounts.find { it.id == state.duePayAccountId }
                    acc?.let { a ->
                        val paid = state.duePayAmount.toDoubleOrNull() ?: 0.0
                        Text("Available: ${state.currencySymbol} ${CurrencyFormatter.formatRs(a.balance)}", style = MaterialTheme.typography.labelSmall, color = if (paid > a.balance) Danger else TextMuted)
                    }
                } else if (state.bankAccounts.isNotEmpty()) {
                    ExposedDropdownMenuBox(expanded = showBankDD, onExpandedChange = { showBankDD = it }) {
                        OutlinedTextField(
                            value = state.bankAccounts.find { it.id == state.duePayAccountId }?.bankName ?: "Select Bank",
                            onValueChange = {}, readOnly = true, label = { Text("Bank Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(showBankDD) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true), shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Danger, unfocusedBorderColor = BorderColor)
                        )
                        ExposedDropdownMenu(expanded = showBankDD, onDismissRequest = { showBankDD = false }) {
                            state.bankAccounts.forEach { bank ->
                                DropdownMenuItem(text = { Column { Text("${bank.bankName} · ${bank.accountNumber}", fontWeight = FontWeight.SemiBold); Text("Available: ${state.currencySymbol} ${CurrencyFormatter.formatRs(bank.balance)}", style = MaterialTheme.typography.labelSmall, color = TextMuted) } }, onClick = { viewModel.onIntent(PurchaseIntent.SetDuePayAccountId(bank.id)); showBankDD = false })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { viewModel.onIntent(PurchaseIntent.ExecuteDuePayment) }, enabled = !state.isDuePaying, colors = ButtonDefaults.buttonColors(containerColor = Danger), shape = RoundedCornerShape(10.dp)) {
                if (state.isDuePaying) CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp) else Text("Pay Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = { viewModel.onIntent(PurchaseIntent.DismissDuePayDialog) }) { Text("Cancel") } }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Purchase Receipt Dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PurchaseReceiptDialog(receipt: PurchaseReceiptData, currency: String, onDone: () -> Unit) {
    val sharer      = rememberReceiptSharer()
    val receiptText = remember(receipt) { PurchaseReceiptBuilder.buildText(receipt) }
    val receiptHtml = remember(receipt) { PurchaseReceiptBuilder.buildHtml(receipt) }
    val fileName    = "purchase_${receipt.poNumber}"

    androidx.compose.ui.window.Dialog(onDismissRequest = onDone, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Card(modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.88f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxSize()) {
                // Orange header (distinct from sale's blue header)
                Box(Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFFE67E22), Color(0xFFF39C12)))).padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        Text("Purchase Complete!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Surface(shape = RoundedCornerShape(100.dp), color = Color.White.copy(0.25f)) {
                            Text(receipt.poNumber, Modifier.padding(horizontal = 16.dp, vertical = 5.dp), style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        if (receipt.hasDue) Surface(shape = RoundedCornerShape(100.dp), color = Color(0xFFE74C3C).copy(0.3f)) {
                            Text("⚠ Due: ${receipt.currencySymbol} ${CurrencyFormatter.formatRs(receipt.dueAmount)}", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                // Body
                Column(Modifier.weight(1f).verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReceiptInfoRow("Supplier", receipt.supplierName)
                    if (receipt.supplierPhone.isNotBlank()) ReceiptInfoRow("Phone", receipt.supplierPhone)
                    HorizontalDivider(color = BorderFaint)
                    Text("Items", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextMuted)
                    receipt.items.forEach { item ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(item.productName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                if (!item.imei.isNullOrBlank()) Text(item.imei!!, style = MaterialTheme.typography.labelSmall, color = Primary)
                                Text("${item.quantity} × ${receipt.currencySymbol} ${CurrencyFormatter.formatRs(item.unitCost)}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            }
                            Text("${receipt.currencySymbol} ${CurrencyFormatter.formatRs(item.totalCost)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    HorizontalDivider(color = BorderFaint)
                    if (receipt.hasDiscount) ReceiptInfoRow("Discount", "-${receipt.currencySymbol} ${CurrencyFormatter.formatRs(receipt.discountAmount)}", Danger)
                    ReceiptInfoRow("Total", "${receipt.currencySymbol} ${CurrencyFormatter.formatRs(receipt.totalAmount)}", Primary, true)
                    HorizontalDivider(color = BorderFaint)
                    if (receipt.cashPaid > 0) ReceiptInfoRow("Cash Paid", "${receipt.currencySymbol} ${CurrencyFormatter.formatRs(receipt.cashPaid)}", Success)
                    if (receipt.bankPaid > 0) ReceiptInfoRow("Bank Paid", "${receipt.currencySymbol} ${CurrencyFormatter.formatRs(receipt.bankPaid)}", Success)
                    if (receipt.hasDue) ReceiptInfoRow("Due Payable", "${receipt.currencySymbol} ${CurrencyFormatter.formatRs(receipt.dueAmount)}", Danger, true)
                }
                // Actions
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { sharer.shareViaWhatsApp(receiptText, receipt.supplierPhone.ifBlank { null }) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF25D366)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF25D366))) {
                            Icon(Icons.Rounded.Share, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("WhatsApp", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        OutlinedButton(onClick = { sharer.openHtmlReceipt(receiptHtml, fileName) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE67E22)), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE67E22))) {
                            Icon(Icons.Rounded.Download, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)); Text("Receipt", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                        Icon(Icons.Rounded.AddShoppingCart, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("New Purchase", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptInfoRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onBackground, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun PurchaseField(label: String, value: String, modifier: Modifier = Modifier.fillMaxWidth(), keyboard: KeyboardType = KeyboardType.Text, prefix: String = "", onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        prefix = if (prefix.isNotBlank()) ({ Text(prefix, style = MaterialTheme.typography.bodySmall) }) else null,
        singleLine = true, modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = BorderColor, focusedLabelColor = Primary)
    )
}