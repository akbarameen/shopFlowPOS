package com.matechmatrix.shopflowpos

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.core.ui.theme.ShopFlowTheme
import com.matechmatrix.shopflowpos.navigation.AppNavigation

@Composable
fun App(windowSize: AppWindowSize = AppWindowSize.COMPACT) {
    ShopFlowTheme {
        val navController = rememberNavController()
        AppNavigation(
            navController = navController,
            windowSize    = windowSize
        )
    }
}