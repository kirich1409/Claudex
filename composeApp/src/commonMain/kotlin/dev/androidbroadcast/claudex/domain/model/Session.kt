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
