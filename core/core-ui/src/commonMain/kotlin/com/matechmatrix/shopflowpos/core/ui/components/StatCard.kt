package com.matechmatrix.shopflowpos.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import com.matechmatrix.shopflowpos.core.ui.theme.PrimaryVariant

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    subtitleColor: Color = Color.Unspecified,
    isPrimary: Boolean = false,
    isVisible: Boolean = true,
    onClick: () -> Unit = {}
) {
    AnimatedVisibility(
        visible = isVisible,
        enter   = fadeIn() + expandVertically(),
        exit    = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isPrimary) Modifier.background(
                        Brush.linearGradient(listOf(Primary, PrimaryVariant))
                    ) else Modifier.background(MaterialTheme.colorScheme.surface)
                )
                .clickable(onClick = onClick)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isPrimary) Color.White.copy(alpha = 0.2f) else iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = if (isPrimary) Color.White else iconTint,
                        modifier           = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text       = value,
                    style      = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (isPrimary) Color.White else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text  = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPrimary) Color.White.copy(alpha = 0.8f)
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subtitle != null) {
                    Text(
                        text       = subtitle,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isPrimary) Color.White.copy(alpha = 0.9f) else subtitleColor
                    )
                }
            }
        }
    }
}