package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ClaudeRunOptions(
    @SerialName("model")
    val model: ClaudeModel = ClaudeModel.SONNET_4_6,
    @SerialName("max_turns")
    val maxTurns: Int? = null,
    @SerialName("permission_mode")
    val permissionMode: PermissionMode = PermissionMode.DEFAULT,
    @SerialName("system_prompt")
    val systemPrompt: String? = null,
    @SerialName("append_system_prompt")
    val appendSystemPrompt: String? = null,
    @SerialName("allowed_tools")
    val allowedTools: List<String> = emptyList(),
    @SerialName("disallowed_tools")
    val disallowedTools: List<String> = emptyList(),
    @SerialName("mcp_config_path")
    val mcpConfigPath: String? = null,
    @SerialName("verbose")
    val verbose: Boolean = false,
    @SerialName("additional_args")
    val additionalArgs: List<String> = emptyList(),
)
