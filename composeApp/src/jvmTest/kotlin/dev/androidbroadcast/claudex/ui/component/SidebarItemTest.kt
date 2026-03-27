package dev.androidbroadcast.claudex.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.androidbroadcast.claudex.ui.component.sidebar.ActionItem
import dev.androidbroadcast.claudex.ui.component.sidebar.ProjectItem
import dev.androidbroadcast.claudex.ui.component.sidebar.SessionItem
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SidebarItemTest {
    @Test
    fun projectItemRendersNameAndClickFires() =
        runComposeUiTest {
            var clicked = false
            setContent {
                ClaudexTheme {
                    ProjectItem(name = "Claudex", onClick = { clicked = true })
                }
            }
            onNodeWithText("Claudex").assertExists()
            onNodeWithText("Claudex").performClick()
            assertTrue(clicked)
        }

    @Test
    fun sessionItemRendersNameAndClickFires() =
        runComposeUiTest {
            var clicked = false
            setContent {
                ClaudexTheme {
                    SessionItem(name = "Session 1", timestamp = "2h ago", onClick = { clicked = true })
                }
            }
            onNodeWithText("Session 1").assertExists()
            onNodeWithText("Session 1").performClick()
            assertTrue(clicked)
        }

    @Test
    fun actionItemRendersLabelAndClickFires() =
        runComposeUiTest {
            var clicked = false
            setContent {
                ClaudexTheme {
                    ActionItem(label = "New chat", onClick = { clicked = true })
                }
            }
            onNodeWithText("New chat").assertExists()
            onNodeWithText("New chat").performClick()
            assertTrue(clicked)
        }
}
