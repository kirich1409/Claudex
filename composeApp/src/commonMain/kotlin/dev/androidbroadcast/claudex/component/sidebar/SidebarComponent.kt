package dev.androidbroadcast.claudex.component.sidebar

import com.arkivanov.decompose.value.Value
import dev.androidbroadcast.claudex.domain.model.Project
import dev.androidbroadcast.claudex.domain.model.Session

public interface SidebarComponent {
    public val state: Value<State>

    public data class State(
        val projects: List<Project> = emptyList(),
        val sessions: List<Session> = emptyList(),
        val selectedProjectId: String? = null,
        val selectedSessionId: String? = null,
    )
}
