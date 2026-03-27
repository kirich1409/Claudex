package dev.androidbroadcast.claudex.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import dev.androidbroadcast.claudex.ui.component.statusbar.StatusBarChip
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class StatusBarChipTest {

    @Test
    fun displaysLabel() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                StatusBarChip(label = "main")
            }
        }
        onNodeWithText("main").assertIsDisplayed()
    }

    @Test
    fun displaysLabelWithIcon() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                StatusBarChip(label = "claude-sonnet-4-5", showIcon = true)
            }
        }
        onNodeWithText("claude-sonnet-4-5").assertIsDisplayed()
    }
}
