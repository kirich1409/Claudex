package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ClaudeModel(public val cliValue: String) {
    @SerialName("claude-opus-4-6")
    OPUS_4_6("claude-opus-4-6"),

    @SerialName("claude-sonnet-4-6")
    SONNET_4_6("claude-sonnet-4-6"),

    @SerialName("claude-haiku-4-5-20251001")
    HAIKU_4_5("claude-haiku-4-5-20251001"),
}
