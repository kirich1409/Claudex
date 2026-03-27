@file:Suppress("UnusedPrivateMember")
// commonMain

package dev.androidbroadcast.claudex.ui.component.sidebar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Preview(name = "ProjectItem - light")
@Composable
private fun ProjectItemLightPreview() {
    ClaudexTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ProjectItem(name = "Claudex", onClick = {})
                ProjectItem(name = "My Side Project", onClick = {})
            }
        }
    }
}

@Preview(name = "ProjectItem - dark")
@Composable
private fun ProjectItemDarkPreview() {
    ClaudexTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ProjectItem(name = "Claudex", onClick = {})
                ProjectItem(name = "My Side Project", onClick = {})
            }
        }
    }
}

@Preview(name = "SessionItem - light")
@Composable
private fun SessionItemLightPreview() {
    ClaudexTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                SessionItem(name = "Session 1", timestamp = "2h ago", onClick = {})
                SessionItem(name = "Refactor the auth flow", timestamp = "Yesterday", onClick = {})
            }
        }
    }
}

@Preview(name = "SessionItem - dark")
@Composable
private fun SessionItemDarkPreview() {
    ClaudexTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                SessionItem(name = "Session 1", timestamp = "2h ago", onClick = {})
                SessionItem(name = "Refactor the auth flow", timestamp = "Yesterday", onClick = {})
            }
        }
    }
}

@Preview(name = "ActionItem - light")
@Composable
private fun ActionItemLightPreview() {
    ClaudexTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ActionItem(label = "New chat", onClick = {})
            }
        }
    }
}

@Preview(name = "ActionItem - dark")
@Composable
private fun ActionItemDarkPreview() {
    ClaudexTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ActionItem(label = "New chat", onClick = {})
            }
        }
    }
}

@Preview(name = "All variants - light")
@Composable
private fun AllVariantsLightPreview() {
    ClaudexTheme(darkTheme = false) {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                ProjectItem(name = "Claudex", onClick = {})
                SessionItem(name = "Session 1", timestamp = "2h ago", onClick = {})
                ActionItem(label = "New chat", onClick = {})
            }
        }
    }
}
