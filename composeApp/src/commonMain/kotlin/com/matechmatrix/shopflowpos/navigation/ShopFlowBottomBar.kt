package com.matechmatrix.shopflowpos.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import com.matechmatrix.shopflowpos.core.ui.theme.PrimaryContainer

@Composable
fun ShopFlowBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected  = selected,
                onClick   = { onNavigate(item.route) },
                icon      = { Icon(item.icon, contentDescription = item.label) },
                label     = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                colors    = NavigationBarItemDefaults.colors(
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