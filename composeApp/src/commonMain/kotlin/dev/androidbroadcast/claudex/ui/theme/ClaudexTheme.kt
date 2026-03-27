package dev.androidbroadcast.claudex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember

@Composable
public fun ClaudexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) ClaudexColors.darkScheme else ClaudexColors.lightScheme
    val spacing = remember { ClaudexSpacing() }
    val codeFont = robotoMonoFontFamily()
    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalCodeFont provides codeFont,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = claudexTypography(),
            content = content,
        )
    }
}
