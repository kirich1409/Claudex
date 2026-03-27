@file:Suppress("UnusedPrivateMember")

package dev.androidbroadcast.claudex.ui.component.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Preview
@Composable
private fun PrimaryButtonPreview() {
    ClaudexTheme { ClaudexButton(label = "Send", variant = ButtonVariant.Primary, onClick = {}) }
}

@Preview
@Composable
private fun GhostButtonPreview() {
    ClaudexTheme { ClaudexButton(label = "Cancel", variant = ButtonVariant.Ghost, onClick = {}) }
}

@Preview
@Composable
private fun DestructiveButtonPreview() {
    ClaudexTheme { ClaudexButton(label = "Delete", variant = ButtonVariant.Destructive, onClick = {}) }
}

@Preview
@Composable
private fun DisabledButtonPreview() {
    ClaudexTheme {
        ClaudexButton(
            label = "Disabled",
            variant = ButtonVariant.Primary,
            onClick = {},
            enabled = false,
        )
    }
}

@Preview
@Composable
private fun IconButtonPreview() {
    ClaudexTheme {
        ClaudexIconButton(
            contentDescription = "Send message",
            onClick = {},
        )
    }
}
