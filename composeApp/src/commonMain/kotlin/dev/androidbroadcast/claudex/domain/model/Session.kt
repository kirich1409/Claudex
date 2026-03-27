// Timestamps are stored as epoch-milliseconds (Long) to match SQLDelight INTEGER columns.
// kotlinx-datetime is used in the component/process layers for Clock.System.now().toEpochMilliseconds().
package dev.androidbroadcast.claudex.domain.model

public data class Session(
    val id: String,
    val projectId: String,
    val name: String,
    val environment: SessionEnvironment,
    val runOptions: ClaudeRunOptions,
    val status: SessionStatus,
    val createdAt: Long,
)
