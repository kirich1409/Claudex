// commonMain
package dev.androidbroadcast.claudex.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.component.root.RootComponent
import dev.androidbroadcast.claudex.domain.model.SessionStatus
import dev.androidbroadcast.claudex.ui.component.sidebar.ActionItem
import dev.androidbroadcast.claudex.ui.component.sidebar.ProjectItem
import dev.androidbroadcast.claudex.ui.component.sidebar.SessionItem
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
internal fun SidebarContent(
    state: RootComponent.State,
    onProjectSelected: (String) -> Unit,
    onSessionSelected: (String) -> Unit,
    onNewSession: (String) -> Unit,
    onNewProject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = "Claudex",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        )
        HorizontalDivider()
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                ActionItem(
                    label = "New Session",
                    onClick = {
                        state.selectedProjectId?.let(onNewSession) ?: onNewProject()
                    },
                )
            }
            item {
                Text(
                    text = "PROJECTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            state.projects.forEach { project ->
                item(key = project.id) {
                    ProjectItem(
                        name = project.name,
                        onClick = { onProjectSelected(project.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (project.id == state.selectedProjectId) {
                    items(state.sessions, key = { "session-${it.id}" }) { session ->
                        SessionItem(
                            name = session.name,
                            timestamp = session.createdAt.toRelativeTimestamp(),
                            isRunning = session.status == SessionStatus.ACTIVE,
                            isSelected = session.id == state.selectedSessionId,
                            onClick = { onSessionSelected(session.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                        )
                    }
                }
            }
        }
        HorizontalDivider()
        ActionItem(label = "Settings", onClick = {})
    }
}

/**
 * Converts an epoch-millisecond timestamp to a short human-readable relative label.
 * e.g. "Today", "Yesterday", or "MMM d" for older dates.
 */
private fun Long.toRelativeTimestamp(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }} ${local.dayOfMonth}"
}
