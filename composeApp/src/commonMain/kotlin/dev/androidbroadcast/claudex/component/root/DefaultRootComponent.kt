package dev.androidbroadcast.claudex.component.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import dev.androidbroadcast.claudex.component.chat.ChatComponent
import dev.androidbroadcast.claudex.domain.model.ClaudeRunOptions
import dev.androidbroadcast.claudex.domain.model.Session
import dev.androidbroadcast.claudex.domain.model.SessionEnvironment
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import dev.androidbroadcast.claudex.domain.repository.ProjectRepository
import dev.androidbroadcast.claudex.domain.repository.SessionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

internal class DefaultRootComponent(
    componentContext: ComponentContext,
    private val projectRepository: ProjectRepository,
    private val sessionRepository: SessionRepository,
    @Suppress("UnusedPrivateProperty")
    private val chatComponentFactory: (ComponentContext, Session) -> ChatComponent,
) : RootComponent, ComponentContext by componentContext {
    private val scope = coroutineScope()
    private val _state = MutableValue(RootComponent.State())

    override val state: Value<RootComponent.State> = _state

    @Suppress("UnusedPrivateProperty")
    private var activeChatComponent: ChatComponent? = null
    private val _child = MutableValue<RootComponent.Child>(RootComponent.Child.Welcome)
    override val child: Value<RootComponent.Child> = _child

    private var sessionObserverJob: Job? = null

    init {
        scope.launch {
            projectRepository.observeAll().collect { projects ->
                _state.update { it.copy(projects = projects) }
            }
        }
    }

    override fun onProjectSelected(projectId: String) {
        Napier.d(tag = TAG) { "Project selected: $projectId" }
        _state.update { it.copy(selectedProjectId = projectId, selectedSessionId = null) }
        sessionObserverJob?.cancel()
        sessionObserverJob =
            scope.launch {
                sessionRepository.observeByProject(projectId).collect { sessions ->
                    _state.update { it.copy(sessions = sessions) }
                }
            }
    }

    override fun onSessionSelected(sessionId: String) {
        Napier.d(tag = TAG) { "Session selected: $sessionId" }
        // chatComponentFactory wired in follow-up iteration — session selection updates state only
        _state.update { it.copy(selectedSessionId = sessionId) }
    }

    override fun onNewSessionRequested(projectId: String) {
        val projectPath = _state.value.projects.find { it.id == projectId }?.path ?: return
        Napier.d(tag = TAG) { "New session for project: $projectId at $projectPath" }
        val session =
            Session(
                id = generateId(),
                projectId = projectId,
                name = "New Session",
                environment = SessionEnvironment.Local,
                runOptions = ClaudeRunOptions(),
                status = SessionStatus.ACTIVE,
                createdAt = Clock.System.now().toEpochMilliseconds(),
            )
        scope.launch {
            sessionRepository.insert(session)
                .onSuccess { onSessionSelected(session.id) }
                .onFailure { Napier.e(tag = TAG, throwable = it) { "Failed to create session" } }
        }
    }

    override fun onNewProjectRequested() {
        Napier.d(tag = TAG) { "New project dialog requested" }
        // Handled by UI layer — signal via state update in a future iteration
    }

    private fun generateId(): String =
        Clock.System.now()
            .toEpochMilliseconds()
            .toString(16)

    private companion object {
        private const val TAG = "RootComponent"
    }
}
