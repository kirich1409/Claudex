package dev.androidbroadcast.claudex.component.sidebar

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.androidbroadcast.claudex.component.root.RootComponent

internal class DefaultSidebarComponent(
    componentContext: ComponentContext,
    rootState: Value<RootComponent.State>,
) : SidebarComponent, ComponentContext by componentContext {

    private val _state = MutableValue(
        SidebarComponent.State(
            projects = rootState.value.projects,
            sessions = rootState.value.sessions,
            selectedProjectId = rootState.value.selectedProjectId,
            selectedSessionId = rootState.value.selectedSessionId,
        ),
    )

    override val state: Value<SidebarComponent.State> = _state
}
