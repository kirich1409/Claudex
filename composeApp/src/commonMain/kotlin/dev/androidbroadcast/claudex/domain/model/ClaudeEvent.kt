package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.json.JsonObject

public sealed class ClaudeEvent {
    public data class AssistantText(val text: String) : ClaudeEvent()

    public data class ToolUse(
        val name: String,
        val toolUseId: String,
        val input: JsonObject,
    ) : ClaudeEvent()

    public data class ToolResult(
        val toolUseId: String,
        val content: String,
    ) : ClaudeEvent()

    public data class Suggestions(val choices: List<String>) : ClaudeEvent()

    public data class SystemInit(
        val model: String,
        val sessionId: String,
    ) : ClaudeEvent()

    public data class ResultEnd(
        val durationMs: Long,
        val costUsd: Double,
    ) : ClaudeEvent()

    public data class ProcessError(
        val exitCode: Int,
        val stderr: String,
    ) : ClaudeEvent()

    public data class Unknown(val raw: String) : ClaudeEvent()
}
