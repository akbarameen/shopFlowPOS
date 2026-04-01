package com.matechmatrix.shopflowpos.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import com.matechmatrix.shopflowpos.core.ui.theme.PrimaryContainer

@Composable
fun ShopFlowNavigationRail(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor   = MaterialTheme.colorScheme.onSurface
    ) {
        Spacer(Modifier.height(12.dp))
        // Only show the bottom nav items on rail (keep it concise on tablet)
        drawerNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationRailItem(
                selected  = selected,
                onClick   = { onNavigate(item.route) },
                icon      = { Icon(item.icon, contentDescription = item.label) },
                label     = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors    = NavigationRailItemDefaults.colors(
                    selectedIconColor   = Primary,
                    selectedTextColor   = Primary,
                    indicatorColor      = PrimaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}