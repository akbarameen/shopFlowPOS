package com.matechmatrix.shopflowpos

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.di.allModules
import org.koin.core.context.startKoin

fun main() {
    startKoin {
        modules(allModules)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title          = "ShopFlowPOS"
        ) {
            App(windowSize = AppWindowSize.EXPANDED)
        }
    }
}