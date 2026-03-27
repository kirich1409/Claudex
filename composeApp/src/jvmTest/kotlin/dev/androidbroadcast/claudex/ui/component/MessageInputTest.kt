package dev.androidbroadcast.claudex.ui.component

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import dev.androidbroadcast.claudex.ui.component.input.MessageInput
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class MessageInputTest {
    @Test
    fun displaysTypedText() =
        runComposeUiTest {
            var value = ""
            setContent {
                ClaudexTheme {
                    MessageInput(
                        value = value,
                        onValueChange = { value = it },
                        onSend = {},
                    )
                }
            }
            onNodeWithContentDescription("Message input").performTextInput("Hello")
            assertEquals("Hello", value)
        }

    @Test
    fun sendButtonIsVisible() =
        runComposeUiTest {
            setContent {
                ClaudexTheme {
                    MessageInput(value = "", onValueChange = {}, onSend = {})
                }
            }
            onNodeWithContentDescription("Send message").assertExists()
        }

    @Test
    fun sendButtonClickFiresOnSend() =
        runComposeUiTest {
            var sent = false
            setContent {
                ClaudexTheme {
                    MessageInput(
                        value = "Hello",
                        onValueChange = {},
                        onSend = { sent = true },
                    )
                }
            }
            onNodeWithContentDescription("Send message").performClick()
            assertTrue(sent)
        }
}
