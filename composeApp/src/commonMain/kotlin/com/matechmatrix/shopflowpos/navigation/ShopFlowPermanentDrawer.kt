package com.matechmatrix.shopflowpos.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PointOfSale
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
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.ui.theme.Accent
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import com.matechmatrix.shopflowpos.core.ui.theme.PrimaryContainer

@Composable
fun ShopFlowPermanentDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // ─── Logo ─────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Primary, Accent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PointOfSale, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    text       = "ShopFlow",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text("POS", style = MaterialTheme.typography.labelSmall, color = Primary)
            }
        }

        // ─── Nav items ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp)
        ) {
            var lastSection = ""
            drawerNavItems.forEach { item ->
                // Section header
                if (item.section != lastSection) {
                    if (lastSection.isNotEmpty()) Spacer(Modifier.height(4.dp))
                    Text(
                        text       = item.section,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    lastSection = item.section
                }

                // Nav item row
                val isSelected = currentRoute == item.route
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) PrimaryContainer else Color.Transparent)
                        .clickable { onNavigate(item.route) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Primary else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector        = item.icon,
                            contentDescription = item.label,
                            tint               = if (isSelected) Color.White
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text  = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}