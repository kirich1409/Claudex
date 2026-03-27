package dev.androidbroadcast.claudex.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class PermissionMode(public val cliValue: String) {
    @SerialName("auto")
    AUTO("auto"),

    @SerialName("default")
    DEFAULT("default"),

    @SerialName("plan")
    PLAN("plan"),

    @SerialName("bypassPermissions")
    BYPASS_PERMISSIONS("bypassPermissions"),
}
