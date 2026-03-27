// commonMain
package dev.androidbroadcast.claudex.ui.screen

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneExpansionStateKey
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.androidbroadcast.claudex.component.root.RootComponent
import dev.androidbroadcast.claudex.ui.component.layout.DragHandle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
public fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.subscribeAsState()
    val child by component.child.subscribeAsState()
    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator()
    val coroutineScope = rememberCoroutineScope()

    val paneExpansionState = rememberPaneExpansionState(
        key = PaneExpansionStateKey.Default,
        anchors = listOf(
            PaneExpansionAnchor.Proportion(0.25f),
            PaneExpansionAnchor.Proportion(0.35f),
            PaneExpansionAnchor.Proportion(0.45f),
        ),
    )

    ListDetailPaneScaffold(
        directive = scaffoldNavigator.scaffoldDirective,
        value = scaffoldNavigator.scaffoldValue,
        listPane = {
            AnimatedPane {
                SidebarContent(
                    state = state,
                    onProjectSelected = component::onProjectSelected,
                    onSessionSelected = { id ->
                        component.onSessionSelected(id)
                        coroutineScope.launch {
                            scaffoldNavigator.navigateTo(ListDetailPaneScaffoldRole.Detail)
                        }
                    },
                    onNewSession = component::onNewSessionRequested,
                    onNewProject = component::onNewProjectRequested,
                )
            }
        },
        detailPane = {
            AnimatedPane {
                when (val c = child) {
                    is RootComponent.Child.Welcome -> WelcomeScreen(
                        projectName = state.projects
                            .find { it.id == state.selectedProjectId }
                            ?.name,
                        onNewSession = {
                            state.selectedProjectId
                                ?.let(component::onNewSessionRequested)
                        },
                    )
                    is RootComponent.Child.Chat -> ChatScreen(component = c.component)
                }
            }
        },
        paneExpansionState = paneExpansionState,
        paneExpansionDragHandle = { _ ->
            DragHandle()
        },
        modifier = modifier,
    )
}
