package com.matechmatrix.shopflowpos.core.ui.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.matechmatrix.shopflowpos.core.ui.theme.Primary

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Primary)
    }
}