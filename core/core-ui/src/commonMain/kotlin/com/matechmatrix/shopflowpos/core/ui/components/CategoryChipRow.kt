package com.matechmatrix.shopflowpos.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matechmatrix.shopflowpos.core.ui.theme.Primary
import com.matechmatrix.shopflowpos.core.ui.theme.PrimaryContainer

@Composable
fun CategoryChipRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SingleChip("All", selectedCategory == null) { onCategorySelected(null) }
        categories.forEach { cat ->
            SingleChip(cat, selectedCategory == cat) { onCategorySelected(cat) }
        }
    }
}

@Composable
private fun SingleChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text       = label,
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color      = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier   = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (isSelected) PrimaryContainer else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}