//package com.matechmatrix.shopflowpos.core.ui.components
//
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.vector.ImageVector
//
//data class NavigationItem(
//    val label: String,
//    val icon: ImageVector,
//    val route: String
//)
//
//@Composable
//fun ShopFlowBottomNav(
//    items: List<NavigationItem>,
//    currentRoute: String?,
//    onItemClick: (NavigationItem) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    NavigationBar(
//        modifier = modifier
//    ) {
//        items.forEach { item ->
//            NavigationBarItem(
//                selected = currentRoute == item.route,
//                onClick = { onItemClick(item) },
//                icon = {
//                    Icon(imageVector = item.icon, contentDescription = item.label)
//                },
//                label = {
//                    Text(text = item.label)
//                }
//            )
//        }
//    }
//}
