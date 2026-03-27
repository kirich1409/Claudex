// commonMain
package dev.androidbroadcast.claudex.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.androidbroadcast.claudex.component.chat.ChatComponent
import dev.androidbroadcast.claudex.domain.model.MessageRole
import dev.androidbroadcast.claudex.ui.component.chip.SuggestionChip
import dev.androidbroadcast.claudex.ui.component.input.MessageInput
import dev.androidbroadcast.claudex.ui.component.message.AssistantMessage
import dev.androidbroadcast.claudex.ui.component.message.UserMessageBubble
import dev.androidbroadcast.claudex.ui.component.statusbar.StatusBarChip

@Composable
internal fun ChatScreen(
    component: ChatComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val listState = rememberLazyListState()
    // Draft lives in UI layer — ChatComponent.onSendMessage takes the final text
    var draft by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.id }) { message ->
                when (message.role) {
                    MessageRole.USER -> UserMessageBubble(text = message.content)
                    MessageRole.ASSISTANT -> AssistantMessage(text = message.content)
                    MessageRole.TOOL -> AssistantMessage(
                        text = message.content,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            state.durationSeconds?.let { secs ->
                item(key = "duration") {
                    Text(
                        text = "── Worked for ${secs}s ──",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    )
                }
            }
        }

        if (state.suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.suggestions.forEach { suggestion ->
                    SuggestionChip(
                        label = suggestion,
                        recommended = suggestion == state.suggestions.firstOrNull(),
                        onClick = { component.onSuggestionSelected(suggestion) },
                    )
                }
            }
        }

        MessageInput(
            value = draft,
            onValueChange = { draft = it },
            onSend = {
                if (draft.isNotBlank()) {
                    component.onSendMessage(draft)
                    draft = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatusBarChip(label = "Local", onClick = {})
            StatusBarChip(label = "Default (config)", onClick = {})
            StatusBarChip(label = "main", onClick = {})
        }
    }
}
