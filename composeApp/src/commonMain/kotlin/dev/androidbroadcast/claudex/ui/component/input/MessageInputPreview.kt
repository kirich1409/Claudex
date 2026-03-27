@file:Suppress("UnusedPrivateMember")
package dev.androidbroadcast.claudex.ui.component.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Preview
@Composable
private fun MessageInputEmptyPreview() {
    ClaudexTheme {
        MessageInput(
            value = "",
            onValueChange = {},
            onSend = {},
        )
    }
}

@Preview
@Composable
private fun MessageInputPopulatedPreview() {
    ClaudexTheme {
        MessageInput(
            value = "Hello, how can I help you today?",
            onValueChange = {},
            onSend = {},
        )
    }
}

@Preview
@Composable
private fun MessageInputDarkPreview() {
    ClaudexTheme(darkTheme = true) {
        MessageInput(
            value = "Dark theme message",
            onValueChange = {},
            onSend = {},
        )
    }
}
