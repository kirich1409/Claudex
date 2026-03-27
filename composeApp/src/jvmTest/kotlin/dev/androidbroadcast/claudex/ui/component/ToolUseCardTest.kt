package dev.androidbroadcast.claudex.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.androidbroadcast.claudex.ui.component.message.ToolUseCard
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ToolUseCardTest {

    @Test
    fun collapsedByDefaultShowsToolNameOnly() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                ToolUseCard(
                    toolName = "bash",
                    content = """{"command": "ls -la"}""",
                )
            }
        }
        onNodeWithText("bash").assertIsDisplayed()
        onNodeWithText("""{"command": "ls -la"}""").assertDoesNotExist()
    }

    @Test
    fun clickExpandsToShowContent() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                ToolUseCard(
                    toolName = "bash",
                    content = """{"command": "ls -la"}""",
                )
            }
        }
        onNodeWithContentDescription("Expand tool use").performClick()
        onNodeWithText("""{"command": "ls -la"}""").assertIsDisplayed()
    }
}
