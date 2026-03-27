package dev.androidbroadcast.claudex.component.chat

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.androidbroadcast.claudex.domain.model.ClaudeEvent
import dev.androidbroadcast.claudex.domain.model.Message
import dev.androidbroadcast.claudex.domain.model.MessageRole
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.repository.MessageRepository
import dev.androidbroadcast.claudex.process.ClaudeProcessDriver
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

internal class DefaultChatComponent(
    componentContext: ComponentContext,
    private val session: Session,
    private val driver: ClaudeProcessDriver,
    private val messageRepository: MessageRepository,
) : ChatComponent, ComponentContext by componentContext {
    private val scope = coroutineScope()
    private val _state = MutableValue(ChatComponent.State())

    override val state: Value<ChatComponent.State> = _state

    init {
        scope.launch {
            messageRepository.observeBySession(session.id).collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
        scope.launch {
            driver.events.collect { event -> handleEvent(event) }
        }
        _state.update { it.copy(status = ChatComponent.Status.Idle) }
    }

    override fun onSendMessage(text: String) {
        scope.launch {
            val userMsg =
                Message(
                    id = generateId(),
                    sessionId = session.id,
                    role = MessageRole.USER,
                    content = text,
                    rawJson = null,
                    timestamp = now(),
                )
            messageRepository.insert(userMsg)
            _state.update { it.copy(status = ChatComponent.Status.Running, suggestions = emptyList()) }
            driver.send(text)
        }
    }

    override fun onSuggestionSelected(text: String) {
        onSendMessage(text)
    }

    override fun onStopSession() {
        scope.launch {
            driver.stop()
            _state.update { it.copy(status = ChatComponent.Status.Stopped) }
        }
    }

    private suspend fun handleEvent(event: ClaudeEvent) {
        when (event) {
            is ClaudeEvent.AssistantText -> {
                val msg = Message(generateId(), session.id, MessageRole.ASSISTANT, event.text, null, now())
                messageRepository.insert(msg)
                _state.update { it.copy(status = ChatComponent.Status.Running) }
            }
            is ClaudeEvent.ToolUse -> {
                val content = "Tool: ${event.name}"
                val msg = Message(generateId(), session.id, MessageRole.TOOL, content, event.input.toString(), now())
                messageRepository.insert(msg)
            }
            is ClaudeEvent.ToolResult -> Unit
            is ClaudeEvent.Suggestions -> {
                _state.update { it.copy(suggestions = event.choices) }
            }
            is ClaudeEvent.SystemInit -> {
                Napier.i(tag = TAG) { "Session init: model=${event.model}" }
                _state.update { it.copy(status = ChatComponent.Status.Idle) }
            }
            is ClaudeEvent.ResultEnd -> {
                val secs = (event.durationMs / 1000).toInt()
                _state.update { it.copy(status = ChatComponent.Status.Idle, durationSeconds = secs) }
            }
            is ClaudeEvent.ProcessError -> {
                Napier.e(tag = TAG) { "Process error: exit=${event.exitCode}" }
                _state.update {
                    it.copy(status = ChatComponent.Status.Error("Session ended unexpectedly (exit ${event.exitCode})"))
                }
            }
            is ClaudeEvent.Unknown -> {
                @Suppress("MagicNumber")
                Napier.v(tag = TAG) { "Unknown event: ${event.raw.take(80)}" }
            }
        }
    }

    @Suppress("MagicNumber")
    private fun generateId(): String =
        Clock.System.now().toEpochMilliseconds().toString(16) +
            (0..999).random().toString().padStart(3, '0')

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    private companion object {
        private const val TAG = "ChatComponent"
    }
}
