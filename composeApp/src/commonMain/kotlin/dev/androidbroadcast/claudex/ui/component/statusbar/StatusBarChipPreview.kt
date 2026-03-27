@file:Suppress("UnusedPrivateMember")
// commonMain

package dev.androidbroadcast.claudex.ui.component.statusbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Preview
@Composable
private fun StatusBarChipLabelOnlyPreview() {
    ClaudexTheme {
        StatusBarChip(
            label = "main",
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview
@Composable
private fun StatusBarChipWithIconPreview() {
    ClaudexTheme {
        StatusBarChip(
            label = "claude-sonnet-4-5",
            onClick = {},
            leadingIcon = { Text("●") },
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview
@Composable
private fun StatusBarChipClickablePreview() {
    ClaudexTheme {
        StatusBarChip(
            label = "2 errors",
            onClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Preview
@Composable
private fun StatusBarChipRowPreview() {
    ClaudexTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp),
        ) {
            StatusBarChip(label = "main", onClick = {})
            StatusBarChip(label = "claude-sonnet-4-5", onClick = {}, leadingIcon = { Text("●") })
            StatusBarChip(label = "2 errors", onClick = {})
        }
    }
}
