package dev.androidbroadcast.claudex.component.chat

import com.arkivanov.decompose.value.Value
import dev.androidbroadcast.claudex.domain.model.Message

public interface ChatComponent {
    public val state: Value<State>

    public fun onSendMessage(text: String)
    public fun onSuggestionSelected(text: String)
    public fun onStopSession()

    public data class State(
        val messages: List<Message> = emptyList(),
        val suggestions: List<String> = emptyList(),
        val status: Status = Status.Connecting,
        val inputDraft: String = "",
        val durationSeconds: Int? = null,
    )

    public sealed interface Status {
        public data object Connecting : Status
        public data object Running : Status
        public data object Idle : Status
        public data object Stopped : Status
        public data class Error(val message: String) : Status
    }
}
