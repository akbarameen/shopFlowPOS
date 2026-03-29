package com.matechmatrix.shopflowpos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.model.Product
import com.matechmatrix.shopflowpos.core.ui.theme.Danger
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import com.matechmatrix.shopflowpos.core.ui.theme.Success

@Composable
fun ShopFlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    containerColor: Color = Primary,
    contentColor: Color = Color.White
) {
    Button(
        onClick  = onClick,
        modifier = modifier.height(48.dp),
        enabled  = enabled && !isLoading,
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = containerColor,
            contentColor           = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor   = contentColor.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(20.dp),
                color       = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, null, modifier = Modifier.size(18.dp))
                }
                Text(text = text, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ShopFlowOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick  = onClick,
        modifier = modifier.height(48.dp),
        enabled  = enabled,
        shape    = RoundedCornerShape(14.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, null, modifier = Modifier.size(18.dp))
            }
            Text(text = text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ShopFlowTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text = text, color = Primary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ShopFlowDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    ShopFlowButton(text, onClick, modifier, isLoading, enabled, containerColor = Danger)
}

@Composable
fun ShopFlowSuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    ShopFlowButton(text, onClick, modifier, isLoading, enabled, leadingIcon, containerColor = Success)
}