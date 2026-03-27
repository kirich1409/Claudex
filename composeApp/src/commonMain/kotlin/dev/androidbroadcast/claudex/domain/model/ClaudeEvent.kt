package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.json.JsonObject

/**
 * A parsed event from Claude Code's `--output-format stream-json` stdout.
 * Events are parsed imperatively by [ClaudeEventParser] — not via kotlinx.serialization.
 */
public sealed interface ClaudeEvent {
    public class AssistantText(val text: String) : ClaudeEvent

    public class ToolUse(
        val name: String,
        val toolUseId: String,
        val input: JsonObject,
    ) : ClaudeEvent

    public class ToolResult(
        val toolUseId: String,
        val content: String,
    ) : ClaudeEvent

    public class Suggestions(val choices: List<String>) : ClaudeEvent

    public class SystemInit(
        /** Raw model identifier string as returned by Claude (e.g. "claude-sonnet-4-6"). May not match a known [ClaudeModel] entry. */
        val model: String,
        val sessionId: String,
    ) : ClaudeEvent

    public class ResultEnd(
        val durationMs: Long,
        val costUsd: Double,
    ) : ClaudeEvent

    public data class ProcessError(
        val exitCode: Int,
        val stderr: String,
    ) : ClaudeEvent

    public data class Unknown(val raw: String) : ClaudeEvent
}
