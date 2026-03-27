@file:Suppress("UnusedPrivateMember")

package dev.androidbroadcast.claudex.ui.component.message

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Preview
@Composable
private fun UserMessageBubblePreview() {
    ClaudexTheme {
        UserMessageBubble(
            text = "How do I list files in a directory?",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview
@Composable
private fun AssistantMessagePreview() {
    ClaudexTheme {
        AssistantMessage(
            text = "You can use `ls -la` to list all files with details.",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ToolUseCard — collapsed")
@Composable
private fun ToolUseCardCollapsedPreview() {
    ClaudexTheme {
        ToolUseCard(
            toolName = "bash",
            content = """{"command": "ls -la"}""",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "ToolUseCard — all three together")
@Composable
private fun MessageComponentsPreview() {
    ClaudexTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            UserMessageBubble(text = "How do I list files in a directory?")
            Spacer(modifier = Modifier.height(8.dp))
            AssistantMessage(text = "You can use `ls -la` to list all files with details.")
            Spacer(modifier = Modifier.height(8.dp))
            ToolUseCard(
                toolName = "bash",
                content = """{"command": "ls -la"}""",
            )
        }
    }
}
