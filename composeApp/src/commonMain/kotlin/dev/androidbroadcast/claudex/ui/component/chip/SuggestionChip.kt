package dev.androidbroadcast.claudex.ui.component.chip

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
public fun SuggestionChip(
    label: String,
    recommended: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors =
        if (recommended) {
            SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            SuggestionChipDefaults.suggestionChipColors(
                containerColor = MaterialTheme.colorScheme.surface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    val border =
        if (recommended) {
            null
        } else {
            SuggestionChipDefaults.suggestionChipBorder(
                enabled = true,
                borderColor = MaterialTheme.colorScheme.outlineVariant,
                borderWidth = 1.dp,
            )
        }
    SuggestionChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        colors = colors,
        border = border,
    )
}
