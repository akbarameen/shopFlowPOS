package com.matechmatrix.shopflowpos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

@Composable
fun ProductGridCard(
    product: Product,
    formattedPrice: String,
    formattedCostPrice: String? = null,
    modifier: Modifier = Modifier,
    onAddToCart: ((Product) -> Unit)? = null,
    onEdit: ((Product) -> Unit)? = null,
    onClick: ((Product) -> Unit)? = null
) {
    val isLowStock = product.stock <= product.lowStockThreshold

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                onClick?.invoke(product) ?: onAddToCart?.invoke(product)
            }
    ) {
        Box(
            modifier         = Modifier.fillMaxWidth().height(100.dp)
                .background(Brush.linearGradient(listOf(PrimaryContainer, PrimaryContainer))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.PhoneAndroid, null,
                tint = Primary.copy(alpha = 0.7f), modifier = Modifier.size(44.dp))
            BadgeChip(
                text           = product.stock.toString(),
                containerColor = if (isLowStock) DangerContainer else SuccessContainer,
                contentColor   = if (isLowStock) Danger else Success,
                modifier       = Modifier.align(Alignment.TopEnd).padding(8.dp)
            )
        }

        Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(product.name, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val sub = listOf(product.brand, product.model).filter { it.isNotBlank() }.joinToString(" · ")
            if (sub.isNotEmpty()) {
                Text(sub, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Spacer(Modifier.height(4.dp))
            Text(formattedPrice, style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold, color = Primary)
            if (formattedCostPrice != null) {
                Text("Cost: $formattedCostPrice", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                BadgeChip(product.condition.name, PrimaryContainer, Primary)
                if (onAddToCart != null) {
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp))
                        .background(Primary).clickable { onAddToCart(product) },
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                } else if (onEdit != null) {
                    Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant).clickable { onEdit(product) },
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Edit, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}