package com.matechmatrix.shopflowpos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.ui.theme.Danger
import com.matechmatrix.shopflowpos.core.ui.theme.DangerContainer
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import com.matechmatrix.shopflowpos.core.ui.theme.PrimaryContainer
import com.matechmatrix.shopflowpos.core.ui.theme.Success
import com.matechmatrix.shopflowpos.core.ui.theme.SuccessContainer

// Passed in from call site — avoids depending on CurrencyFormatter here
@Composable
fun ProductListItem(
    product: Product,
    formattedPrice: String,
    formattedCostPrice: String? = null,   // non-null = show cost price
    modifier: Modifier = Modifier,
    onAddToCart: ((Product) -> Unit)? = null,
    onEdit: ((Product) -> Unit)? = null,
    onClick: ((Product) -> Unit)? = null
) {
    val isLowStock = product.stock <= product.lowStockThreshold

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = onClick != null) { onClick?.invoke(product) }
            .padding(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PhoneAndroid, null, tint = Primary, modifier = Modifier.size(26.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text       = product.name,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            val sub = listOf(product.brand, product.model).filter { it.isNotBlank() }.joinToString(" · ")
            if (sub.isNotEmpty()) {
                Text(sub, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically,
                modifier              = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text       = formattedPrice,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Primary
                )
                if (formattedCostPrice != null) {
                    Text("Cost: $formattedCostPrice", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BadgeChip(
                    text           = "Qty: ${product.stock}",
                    containerColor = if (isLowStock) DangerContainer else SuccessContainer,
                    contentColor   = if (isLowStock) Danger else Success
                )
            }
        }

        if (onAddToCart != null) {
            Box(
                modifier         = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                    .background(PrimaryContainer).clickable { onAddToCart(product) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, "Add", tint = Primary, modifier = Modifier.size(18.dp))
            }
        } else if (onEdit != null) {
            Box(
                modifier         = Modifier.size(36.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onEdit(product) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Edit, "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
        }
    }
}