package com.matechmatrix.shopflowpos.core.ui.theme

@androidx.compose.runtime.Composable
actual fun provideColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): androidx.compose.material3.ColorScheme {
    // JVM (Desktop) doesn't support dynamic color
    return if (darkTheme) DarkColorScheme else LightColorScheme
}