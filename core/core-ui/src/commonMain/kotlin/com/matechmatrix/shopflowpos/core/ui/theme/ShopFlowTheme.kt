package com.matechmatrix.shopflowpos.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

 val LightColorScheme = lightColorScheme(
    primary            = Primary,
    onPrimary          = Color.White,
    primaryContainer   = PrimaryContainer,
    onPrimaryContainer = Primary,
    secondary          = Accent,
    onSecondary        = Color.White,
    secondaryContainer = AccentContainer,
    background         = BgLight,
    onBackground       = Text1Light,
    surface            = SurfaceLight,
    onSurface          = Text1Light,
    surfaceVariant     = Surface2Light,
    onSurfaceVariant   = Text2Light,
    outline            = BorderLight,
    error              = Danger,
    onError            = Color.White
)

val DarkColorScheme = darkColorScheme(
    primary            = Primary,
    onPrimary          = Color.White,
    primaryContainer   = PrimaryContainer,
    onPrimaryContainer = PrimaryVariant,
    secondary          = Accent,
    onSecondary        = Color.White,
    secondaryContainer = AccentContainer,
    background         = BgDark,
    onBackground       = Text1Dark,
    surface            = SurfaceDark,
    onSurface          = Text1Dark,
    surfaceVariant     = Surface2Dark,
    onSurfaceVariant   = Text2Dark,
    error              = Danger,
    onError            = Color.White
)

//@Composable
//fun ShopFlowTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    MaterialTheme(
//        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
////        colorScheme = DarkColorScheme,
//        typography  = ShopFlowTypography,
//        shapes      = ShopFlowShapes,
//        content     = content
//    )
//}

@Composable
expect fun provideColorScheme(
    darkTheme: Boolean,
    dynamicColor: Boolean
): ColorScheme
@Composable
fun ShopFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = provideColorScheme(darkTheme, dynamicColor)

    MaterialTheme(
        colorScheme = colors,
        typography = ShopFlowTypography,
        shapes = ShopFlowShapes,
        content = content
    )
}