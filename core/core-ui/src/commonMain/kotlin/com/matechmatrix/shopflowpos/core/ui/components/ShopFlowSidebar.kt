//package com.matechmatrix.shopflowpos.core.ui.components
//
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.width
//import androidx.compose.material3.Icon
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.NavigationDrawerItem
//import androidx.compose.material3.NavigationDrawerItemDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun ShopFlowSidebar(
//    items: List<NavigationItem>,
//    currentRoute: String?,
//    onItemClick: (NavigationItem) -> Unit,
//    modifier: Modifier = Modifier,
//    header: @Composable () -> Unit = {
//        Text(
//            text = "ShopFlow POS",
//            style = MaterialTheme.typography.headlineMedium,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(16.dp)
//        )
//    }
//) {
//    Column(
//        modifier = modifier
//            .width(280.dp)
//            .padding(12.dp)
//    ) {
//        header()
//        Spacer(modifier = Modifier.height(8.dp))
//        items.forEach { item ->
//            NavigationDrawerItem(
//                label = { Text(item.label) },
//                selected = currentRoute == item.route,
//                onClick = { onItemClick(item) },
//                icon = { Icon(item.icon, contentDescription = item.label) },
//                colors = NavigationDrawerItemDefaults.colors(
//                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
//                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
//                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer
//                ),
//                modifier = Modifier.padding(vertical = 4.dp)
//            )
//        }
//    }
//}
