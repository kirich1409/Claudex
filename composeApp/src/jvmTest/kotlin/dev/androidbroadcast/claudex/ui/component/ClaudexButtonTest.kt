package dev.androidbroadcast.claudex.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import dev.androidbroadcast.claudex.ui.component.button.ButtonVariant
import dev.androidbroadcast.claudex.ui.component.button.ClaudexButton
import dev.androidbroadcast.claudex.ui.component.button.ClaudexIconButton
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ClaudexButtonTest {

    @Test
    fun primaryButtonClickFiresCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            ClaudexTheme {
                ClaudexButton(
                    label = "Click me",
                    variant = ButtonVariant.Primary,
                    onClick = { clicked = true },
                )
            }
        }
        onNodeWithText("Click me").performClick()
        assertTrue(clicked)
    }

    @Test
    fun ghostButtonClickFiresCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            ClaudexTheme {
                ClaudexButton(
                    label = "Ghost",
                    variant = ButtonVariant.Ghost,
                    onClick = { clicked = true },
                )
            }
        }
        onNodeWithText("Ghost").performClick()
        assertTrue(clicked)
    }

    @Test
    fun destructiveButtonClickFiresCallback() = runComposeUiTest {
        var clicked = false
        setContent {
            ClaudexTheme {
                ClaudexButton(
                    label = "Delete",
                    variant = ButtonVariant.Destructive,
                    onClick = { clicked = true },
                )
            }
        }
        onNodeWithText("Delete").performClick()
        assertTrue(clicked)
    }

    @Test
    fun disabledButtonDoesNotFireCallback() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                ClaudexButton(
                    label = "Disabled",
                    variant = ButtonVariant.Primary,
                    onClick = {},
                    enabled = false,
                )
            }
        }
        onNodeWithText("Disabled").assertIsNotEnabled()
    }

    @Test
    fun iconButtonHasContentDescription() = runComposeUiTest {
        setContent {
            ClaudexTheme {
                ClaudexIconButton(
                    contentDescription = "Send message",
                    onClick = {},
                )
            }
        }
        onNodeWithContentDescription("Send message").assertExists()
    }
}
