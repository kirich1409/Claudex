// Timestamps are stored as epoch-milliseconds (Long) to match SQLDelight INTEGER columns.
// kotlinx-datetime is used in the component/process layers for Clock.System.now().toEpochMilliseconds().
package dev.androidbroadcast.claudex.domain.model

/**
 * @param rawJson The original JSON from the stream for debugging/display of tool use events.
 *   Null for user messages and plain text assistant messages.
 */
public data class Message(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val rawJson: String?,
    val timestamp: Long,
)
