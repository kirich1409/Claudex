package dev.androidbroadcast.claudex

import androidx.compose.runtime.Composable
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Composable
public fun App() {
    ClaudexTheme {
        // RootContent wired in main.kt after DI is set up (Task 9)
    }
}
