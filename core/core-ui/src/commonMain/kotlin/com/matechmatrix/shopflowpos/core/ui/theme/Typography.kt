package com.matechmatrix.shopflowpos.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val ShopFlowTypography = Typography(
    displayLarge   = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineLarge  = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
    headlineSmall  = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
    titleLarge     = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold),
    titleMedium    = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
    titleSmall     = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge      = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall      = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge     = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelMedium    = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall     = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.4.sp)
)