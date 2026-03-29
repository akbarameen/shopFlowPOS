package com.matechmatrix.shopflowpos.feature.settings.presentation

import SettingsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.*
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    windowSize : AppWindowSize,
    onSignOut  : () -> Unit = {},
    viewModel  : SettingsViewModel = koinViewModel()
) {
    val state             = viewModel.state.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SettingsEffect.ShowToast   -> snackbarHostState.showSnackbar(effect.message)
                SettingsEffect.NavigateToAuth -> onSignOut()
            }
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Scaffold
        }

        when (windowSize) {
            AppWindowSize.EXPANDED -> SettingsExpandedLayout(state, viewModel, innerPadding)
            else                   -> SettingsSingleColumnLayout(state, viewModel, innerPadding, windowSize)
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (state.showShopNameDialog)
        SimpleInputDialog("Shop Name", state.shopNameInput, "e.g. Ahmed Mobile Shop",
            { viewModel.onIntent(SettingsIntent.SetShopNameInput(it)) },
            { viewModel.onIntent(SettingsIntent.SaveShopName) },
            { viewModel.onIntent(SettingsIntent.DismissShopNameDialog) })

    if (state.showCurrencyDialog)
        SimpleInputDialog("Currency Symbol", state.currencyInput, "e.g. Rs. or $",
            { viewModel.onIntent(SettingsIntent.SetCurrencyInput(it)) },
            { viewModel.onIntent(SettingsIntent.SaveCurrency) },
            { viewModel.onIntent(SettingsIntent.DismissCurrencyDialog) })

    if (state.showLowStockDialog)
        SimpleInputDialog("Low Stock Threshold", state.lowStockInput, "e.g. 5",
            { viewModel.onIntent(SettingsIntent.SetLowStockInput(it)) },
            { viewModel.onIntent(SettingsIntent.SaveLowStock) },
            { viewModel.onIntent(SettingsIntent.DismissLowStockDialog) })

    if (state.showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(SettingsIntent.DismissSignOutConfirm) },
            icon = {
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(DangerContainer),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Logout, null, tint = Danger, modifier = Modifier.size(26.dp))
                }
            },
            title = { Text("Sign Out?", fontWeight = FontWeight.Bold) },
            text  = { Text("This will deactivate the app on this device. You'll need your license key to activate again.") },
            confirmButton = {
                Button(onClick = { viewModel.onIntent(SettingsIntent.SignOut) },
                    colors  = ButtonDefaults.buttonColors(containerColor = Danger),
                    shape   = RoundedCornerShape(10.dp)) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(SettingsIntent.DismissSignOutConfirm) }) { Text("Cancel") }
            }
        )
    }
}

// ── COMPACT & MEDIUM — single column ─────────────────────────────────────────
@Composable
private fun SettingsSingleColumnLayout(
    state: SettingsState, viewModel: SettingsViewModel,
    innerPadding: PaddingValues, windowSize: AppWindowSize
) {
    val hPad = if (windowSize == AppWindowSize.MEDIUM) 24.dp else 16.dp
    LazyColumn(
        Modifier.fillMaxSize().padding(innerPadding),
        contentPadding      = PaddingValues(horizontal = hPad, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProfileHeroCard(state) }
        item { ShopSettingsSection(state, viewModel) }
        item { DisplaySettingsSection(state, viewModel) }
        item { AboutSection() }
        item { SignOutButton(viewModel) }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── EXPANDED — 2-column layout ────────────────────────────────────────────────
@Composable
private fun SettingsExpandedLayout(
    state: SettingsState, viewModel: SettingsViewModel,
    innerPadding: PaddingValues
) {
    Row(
        Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Left column
        LazyColumn(
            modifier            = Modifier.weight(0.45f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding      = PaddingValues(bottom = 24.dp)
        ) {
            item { ProfileHeroCard(state) }
            item { AboutSection() }
            item { SignOutButton(viewModel) }
        }
        // Right column
        LazyColumn(
            modifier            = Modifier.weight(0.55f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding      = PaddingValues(bottom = 24.dp)
        ) {
            item { ShopSettingsSection(state, viewModel) }
            item { DisplaySettingsSection(state, viewModel) }
        }
    }
}

// ── Profile hero ──────────────────────────────────────────────────────────────
@Composable
private fun ProfileHeroCard(state: SettingsState) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Primary, PrimaryVariant)))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(
                Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(0.18f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.Store, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(state.shopName.ifBlank { "My Shop" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text("ShopFlow POS", style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f))
            }
        }
    }
}

// ── Shop settings ─────────────────────────────────────────────────────────────
@Composable
private fun ShopSettingsSection(state: SettingsState, viewModel: SettingsViewModel) {
    SettingsSectionCard("Shop Settings") {
        SettingsRow(Icons.Rounded.Store, "Shop Name", state.shopName.ifBlank { "Not set" })
        { viewModel.onIntent(SettingsIntent.ShowShopNameDialog) }
        SectionDivider()
        SettingsRow(Icons.Rounded.AttachMoney, "Currency Symbol", state.currencySymbol)
        { viewModel.onIntent(SettingsIntent.ShowCurrencyDialog) }
        SectionDivider()
        SettingsRow(Icons.Rounded.Inventory2, "Low Stock Threshold", "${state.lowStockThreshold} units")
        { viewModel.onIntent(SettingsIntent.ShowLowStockDialog) }
    }
}

// ── Display settings ──────────────────────────────────────────────────────────
@Composable
private fun DisplaySettingsSection(state: SettingsState, viewModel: SettingsViewModel) {
    SettingsSectionCard("Display Settings") {
        SettingsToggleRow(
            icon      = Icons.Rounded.Visibility,
            title     = "Show Analytics",
            subtitle  = "Hide revenue figures on dashboard",
            checked   = state.analyticsVisible,
            onChanged = { viewModel.onIntent(SettingsIntent.SetAnalyticsVisible(it)) }
        )
        SectionDivider()
        SettingsToggleRow(
            icon      = Icons.Rounded.LocalOffer,
            title     = "Show Cost Price",
            subtitle  = "Display cost price in inventory",
            checked   = state.showCostPrice,
            onChanged = { viewModel.onIntent(SettingsIntent.SetShowCostPrice(it)) }
        )
        SectionDivider()
        // Theme selector row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.DarkMode, null, tint = Primary, modifier = Modifier.size(18.dp)) }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("App Theme", style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Text("Visual appearance", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("System", "Light", "Dark").forEach { t ->
                    val isSelected = state.theme == t.lowercase()
                    Surface(
                        onClick = { viewModel.onIntent(SettingsIntent.SetTheme(t.lowercase())) },
                        shape   = RoundedCornerShape(100.dp),
                        color   = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
                        border  = if (isSelected) null
                        else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(t, Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ── About ─────────────────────────────────────────────────────────────────────
@Composable
private fun AboutSection() {
    SettingsSectionCard("About") {
        SettingsRow(Icons.Rounded.Info, "App Version", "ShopFlow POS v1.0.0", onClick = null)
        SectionDivider()
        SettingsRow(Icons.Rounded.Security, "License", "Active",
            iconTint = Success, onClick = null)
    }
}

// ── Sign out button ───────────────────────────────────────────────────────────
@Composable
private fun SignOutButton(viewModel: SettingsViewModel) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DangerContainer)
            .clickable { viewModel.onIntent(SettingsIntent.ShowSignOutConfirm) }
            .padding(16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Danger.copy(0.15f)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Logout, null, tint = Danger, modifier = Modifier.size(18.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text("Sign Out / Deactivate", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = Danger)
            Text("Deactivates app on this device", style = MaterialTheme.typography.labelSmall,
                color = Danger.copy(0.7f))
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = Danger, modifier = Modifier.size(18.dp))
    }
}

// ── Section card wrapper ──────────────────────────────────────────────────────
@Composable
private fun SettingsSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title.uppercase(),
            style         = MaterialTheme.typography.labelSmall,
            fontWeight    = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color         = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier      = Modifier.padding(horizontal = 4.dp)
        )
        Card(
            Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(14.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(0.dp)
        ) { Column(content = content) }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        Modifier.padding(horizontal = 16.dp),
        color     = MaterialTheme.colorScheme.outlineVariant,
        thickness = 0.5.dp
    )
}

// ── Tappable row ──────────────────────────────────────────────────────────────
@Composable
private fun SettingsRow(
    icon: ImageVector, title: String, subtitle: String,
    iconTint: Color = Primary, onClick: (() -> Unit)?
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconTint.copy(0.10f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) {
            Icon(Icons.Rounded.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp))
        }
    }
}

// ── Toggle row ────────────────────────────────────────────────────────────────
@Composable
private fun SettingsToggleRow(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onChanged: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked          = checked,
            onCheckedChange  = onChanged,
            colors           = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = Primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

// ── Simple input dialog ───────────────────────────────────────────────────────
@Composable
private fun SimpleInputDialog(
    title: String, value: String, placeholder: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit, onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                placeholder   = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(10.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}