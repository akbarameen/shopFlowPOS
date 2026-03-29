package com.matechmatrix.shopflowpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.matechmatrix.shopflowpos.core.ui.adaptive.AppWindowSize
import com.matechmatrix.shopflowpos.di.allModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Simple width-based detection without extra library
            val widthDp = resources.displayMetrics.let {
                it.widthPixels / it.density
            }
            val windowSize = when {
                widthDp < 600f  -> AppWindowSize.COMPACT
                widthDp < 1200f -> AppWindowSize.MEDIUM
                else            -> AppWindowSize.EXPANDED
            }
            App(windowSize = windowSize)
        }
    }
}