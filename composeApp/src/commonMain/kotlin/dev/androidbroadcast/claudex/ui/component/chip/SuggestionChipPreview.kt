@file:Suppress("UnusedPrivateMember")
package dev.androidbroadcast.claudex.ui.component.chip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Preview
@Composable
private fun SuggestionChipRecommendedPreview() {
    ClaudexTheme {
        SuggestionChip(
            label = "Fix bug",
            recommended = true,
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview
@Composable
private fun SuggestionChipAlternativePreview() {
    ClaudexTheme {
        SuggestionChip(
            label = "Refactor",
            recommended = false,
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview
@Composable
private fun SuggestionChipBothVariantsPreview() {
    ClaudexTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            SuggestionChip(label = "Fix bug", recommended = true, onClick = {})
            SuggestionChip(label = "Refactor", recommended = false, onClick = {})
        }
    }
}
