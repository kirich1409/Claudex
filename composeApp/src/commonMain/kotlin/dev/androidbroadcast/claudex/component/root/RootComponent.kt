package dev.androidbroadcast.claudex.component.root

import com.arkivanov.decompose.value.Value
import dev.androidbroadcast.claudex.component.chat.ChatComponent
import dev.androidbroadcast.claudex.domain.model.Project
import dev.androidbroadcast.claudex.domain.model.Session

public interface RootComponent {
    public val state: Value<State>

    public fun onProjectSelected(projectId: String)
    public fun onSessionSelected(sessionId: String)
    public fun onNewSessionRequested(projectId: String)
    public fun onNewProjectRequested()

    public data class State(
        val projects: List<Project> = emptyList(),
        val selectedProjectId: String? = null,
        val selectedSessionId: String? = null,
        val sessions: List<Session> = emptyList(),
    )

    public sealed interface Child {
        public data object Welcome : Child
        public data class Chat(val component: ChatComponent) : Child
    }
}
