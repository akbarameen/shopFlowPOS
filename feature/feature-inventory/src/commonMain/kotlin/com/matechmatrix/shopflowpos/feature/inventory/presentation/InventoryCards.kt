package com.matechmatrix.shopflowpos.feature.inventory.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.common.util.CurrencyFormatter
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.model.enums.ProductCategory
import com.matechmatrix.shopflowpos.core.ui.components.BadgeChip
import com.matechmatrix.shopflowpos.core.ui.theme.*

// ── Category icon helper ──────────────────────────────────────────────────────

private val ProductCategory.icon: ImageVector
    get() = when (this) {
        ProductCategory.PHONE      -> Icons.Rounded.PhoneAndroid
        ProductCategory.LAPTOP     -> Icons.Rounded.Laptop
        ProductCategory.TABLET     -> Icons.Rounded.TabletAndroid
        ProductCategory.ACCESSORY  -> Icons.Rounded.Headphones
        ProductCategory.SMARTWATCH -> Icons.Rounded.Watch
        ProductCategory.GAMING     -> Icons.Rounded.SportsEsports
        ProductCategory.OTHER      -> Icons.Rounded.Inventory2
    }

// ── Stock badge helper ────────────────────────────────────────────────────────

@Composable
private fun StockBadge(product: Product) {
    when {
        product.isImeiTracked -> {
            // IMEI items: show IMEI-based tag only (stock = 1 by definition if visible)
            BadgeChip(
                text           = "In Stock",
                containerColor = SuccessContainer,
                contentColor   = Success
            )
        }
        product.isLowStock -> BadgeChip(
            text           = "Low · ${product.stock}",
            containerColor = DangerContainer,
            contentColor   = Danger
        )
        else -> BadgeChip(
            text           = "Qty: ${product.stock}",
            containerColor = SuccessContainer,
            contentColor   = Success
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// LIST CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InventoryListCard(
    product        : Product,
    showCostPrice  : Boolean,
    currencySymbol : String,
    onEdit         : () -> Unit,
    onStockUpdate  : () -> Unit,
    onDelete       : () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon box
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(PrimaryContainer, PrimaryLight))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    product.category.icon, null,
                    tint     = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(
                modifier              = Modifier.weight(1f),
                verticalArrangement   = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    product.name,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )

                // IMEI or subtitle
                if (!product.imei.isNullOrBlank()) {
                    Text(
                        "IMEI: ${product.imei}",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = Primary,
                        fontWeight = FontWeight.Bold
                    )
                } else if (product.subtitle.isNotBlank()) {
                    Text(
                        product.subtitle,
                        style   = MaterialTheme.typography.labelSmall,
                        color   = TextMuted,
                        maxLines = 1
                    )
                }

                // Spec line (RAM / Storage / Color)
                product.specLine?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }

                // Price row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "$currencySymbol ${CurrencyFormatter.formatCompact(product.sellingPrice)}",
                        style      = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color      = Primary
                    )
                    if (showCostPrice && product.costPrice > 0) {
                        Text(
                            "Cost: $currencySymbol ${CurrencyFormatter.formatCompact(product.costPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // Right column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StockBadge(product)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Stock update only for non-IMEI items
                    if (!product.isImeiTracked) {
                        IconButton(onClick = onStockUpdate, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Rounded.EditNote, null, tint = Info, modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, null, tint = Danger, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GRID CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun InventoryGridCard(
    product        : Product,
    showCostPrice  : Boolean,
    currencySymbol : String,
    onEdit         : () -> Unit,
    onStockUpdate  : () -> Unit,
    onDelete       : () -> Unit,
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Top row: icon + stock badge
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top
            ) {
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(listOf(PrimaryContainer, PrimaryLight))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(product.category.icon, null, tint = Primary, modifier = Modifier.size(22.dp))
                }
                StockBadge(product)
            }

            // Name
            Text(
                product.name,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis
            )

            // IMEI or subtitle
            if (!product.imei.isNullOrBlank()) {
                Text(
                    "IMEI: ${product.imei}",
                    style      = MaterialTheme.typography.labelSmall,
                    color      = Primary,
                    fontWeight = FontWeight.Bold
                )
            } else if (product.subtitle.isNotBlank()) {
                Text(
                    product.subtitle,
                    style    = MaterialTheme.typography.labelSmall,
                    color    = TextMuted,
                    maxLines = 1
                )
            }

            // Spec line
            product.specLine?.let {
                Text(it, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }

            // Category chip
            BadgeChip(
                text           = "${product.category.emoji} ${product.category.displayName}",
                containerColor = PrimaryContainer,
                contentColor   = Primary
            )

            Divider(color = BorderColor.copy(alpha = 0.5f), thickness = 0.5.dp)

            // Price
            Text(
                "$currencySymbol ${CurrencyFormatter.formatRs(product.sellingPrice)}",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color      = Primary
            )
            if (showCostPrice && product.costPrice > 0) {
                Text(
                    "Cost: $currencySymbol ${CurrencyFormatter.formatCompact(product.costPrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            // Action buttons
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (!product.isImeiTracked) {
                    IconButton(onClick = onStockUpdate, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.EditNote, null, tint = Info, modifier = Modifier.size(16.dp))
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.Edit, null, tint = Primary, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Rounded.DeleteOutline, null, tint = Danger, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}