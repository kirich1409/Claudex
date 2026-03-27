package dev.androidbroadcast.claudex.ui.component.message

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import dev.androidbroadcast.claudex.ui.theme.LocalCodeFont

@Composable
public fun AssistantMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    val monoFamily = LocalCodeFont.current
    val annotated = remember(text, monoFamily) { parseInlineCode(text, monoFamily) }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

@Suppress("LoopWithTooManyJumpStatements")
private fun parseInlineCode(
    text: String,
    monoFamily: FontFamily,
) = buildAnnotatedString {
    var cursor = 0
    while (cursor < text.length) {
        val backtick = text.indexOf('`', cursor)
        if (backtick == -1) {
            append(text.substring(cursor))
            break
        }
        append(text.substring(cursor, backtick))
        val closing = text.indexOf('`', backtick + 1)
        if (closing == -1) {
            // No closing backtick — treat rest as plain text
            append(text.substring(backtick))
            break
        }
        withStyle(SpanStyle(fontFamily = monoFamily)) {
            append(text.substring(backtick + 1, closing))
        }
        cursor = closing + 1
    }
}
