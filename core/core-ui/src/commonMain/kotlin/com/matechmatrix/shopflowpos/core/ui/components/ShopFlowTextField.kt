package com.matechmatrix.shopflowpos.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.ui.theme.Danger
import com.matechmatrix.shopflowpos.core.ui.theme.Primary

@Composable
fun ShopFlowTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            isError   -> Danger
            isFocused -> Primary
            else      -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        animationSpec = tween(200),
        label = "borderColor"
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelMedium,
                color    = if (isFocused) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        BasicTextField(
            value               = value,
            onValueChange       = onValueChange,
            enabled             = enabled,
            singleLine          = singleLine,
            visualTransformation = visualTransformation,
            textStyle           = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush         = SolidColor(Primary),
            keyboardOptions     = KeyboardOptions(keyboardType = keyboardType),
            modifier            = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
                .background(
                    color = if (isFocused) MaterialTheme.colorScheme.surface
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isFocused || isError) 2.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(12.dp)
                ),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)) {
                    if (value.isEmpty()) {
                        Text(
                            text  = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (isError) {
            // Error slot — caller controls the message via separate Text if needed
        }
    }
}