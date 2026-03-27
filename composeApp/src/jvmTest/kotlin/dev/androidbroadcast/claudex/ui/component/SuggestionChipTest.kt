package dev.androidbroadcast.claudex.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.androidbroadcast.claudex.ui.component.chip.SuggestionChip
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SuggestionChipTest {

    @Test
    fun recommendedChipDisplaysLabel() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                SuggestionChip(label = "Fix bug", recommended = true, onClick = {})
            }
        }
        onNodeWithText("Fix bug").assertExists()
    }

    @Test
    fun alternativeChipDisplaysLabel() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                SuggestionChip(label = "Refactor", recommended = false, onClick = {})
            }
        }
        onNodeWithText("Refactor").assertExists()
    }

    @Test
    fun chipClickFiresCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            ClaudexTheme {
                SuggestionChip(label = "Go", recommended = true, onClick = { clicked = true })
            }
        }
        onNodeWithText("Go").performClick()
        assertTrue(clicked)
    }
}
