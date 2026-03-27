package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public sealed interface SessionEnvironment {
    @Serializable
    @SerialName("local")
    public data object Local : SessionEnvironment

    @Serializable
    @SerialName("docker")
    public data class Docker(
        @SerialName("image")
        val image: String = "docker/claude-code-sandbox:latest",
        @SerialName("container_id")
        val containerId: String? = null,
    ) : SessionEnvironment
}
