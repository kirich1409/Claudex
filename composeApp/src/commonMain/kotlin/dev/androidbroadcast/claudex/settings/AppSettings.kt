package dev.androidbroadcast.claudex.settings

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AppSettings(
    @SerialName("sidebar_width_proportion") val sidebarWidthProportion: Float = 0.3f,
    @SerialName("dark_theme_override") val darkThemeOverride: Boolean? = null,
    @SerialName("last_open_project_id") val lastOpenProjectId: String? = null,
    @SerialName("settings_version") val settingsVersion: Int = 1,
)
