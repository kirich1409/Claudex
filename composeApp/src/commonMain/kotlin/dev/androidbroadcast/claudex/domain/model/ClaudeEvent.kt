package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.json.JsonObject

/**
 * A parsed event from Claude Code's `--output-format stream-json` stdout.
 * Events are parsed imperatively by [ClaudeEventParser] — not via kotlinx.serialization.
 */
public sealed interface ClaudeEvent {
    public class AssistantText(public val text: String) : ClaudeEvent

    public class ToolUse(
        public val name: String,
        public val toolUseId: String,
        public val input: JsonObject,
    ) : ClaudeEvent

    public class ToolResult(
        public val toolUseId: String,
        public val content: String,
    ) : ClaudeEvent

    public class Suggestions(public val choices: List<String>) : ClaudeEvent

    public class SystemInit(
        /** Raw model identifier string as returned by Claude (e.g. "claude-sonnet-4-6"). May not match a known [ClaudeModel] entry. */
        public val model: String,
        public val sessionId: String,
    ) : ClaudeEvent

    public class ResultEnd(
        public val durationMs: Long,
        public val costUsd: Double,
    ) : ClaudeEvent

    public data class ProcessError(
        val exitCode: Int,
        val stderr: String,
    ) : ClaudeEvent

    public data class Unknown(val raw: String) : ClaudeEvent
}
