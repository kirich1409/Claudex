package dev.androidbroadcast.claudex.domain.model

public data class Message(
    val id: String,
    val sessionId: String,
    val role: MessageRole,
    val content: String,
    val rawJson: String?,
    val timestamp: Long,
)
