package dev.androidbroadcast.claudex.component.sidebar

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import dev.androidbroadcast.claudex.component.root.RootComponent

internal class DefaultSidebarComponent(
    componentContext: ComponentContext,
    rootState: Value<RootComponent.State>,
) : SidebarComponent, ComponentContext by componentContext {
    private val _state = MutableValue(rootState.value.toSidebarState())

    override val state: Value<SidebarComponent.State> = _state

    init {
        rootState.subscribe { root ->
            _state.value = root.toSidebarState()
        }
    }
}

private fun RootComponent.State.toSidebarState() =
    SidebarComponent.State(
        projects = projects,
        sessions = sessions,
        selectedProjectId = selectedProjectId,
        selectedSessionId = selectedSessionId,
    )
