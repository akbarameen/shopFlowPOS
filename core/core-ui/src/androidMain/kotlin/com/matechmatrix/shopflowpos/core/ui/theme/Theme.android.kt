package com.matechmatrix.shopflowpos.core.ui.theme

import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun provideColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme {

    val context = LocalContext.current

    return when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            if (darkTheme) dynamicDarkColorScheme(context)
//            else dynamicLightColorScheme(context)
//        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
}