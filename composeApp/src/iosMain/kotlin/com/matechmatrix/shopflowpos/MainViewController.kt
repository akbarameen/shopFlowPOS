package com.matechmatrix.shopflowpos

import androidx.compose.ui.window.ComposeUIViewController
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.di.allModules
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(allModules)
        }
    }
) {
    App(windowSize = AppWindowSize.COMPACT)
}