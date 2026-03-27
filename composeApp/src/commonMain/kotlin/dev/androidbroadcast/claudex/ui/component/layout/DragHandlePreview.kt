// commonMain
package dev.androidbroadcast.claudex.ui.component.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.ClaudexTheme

@Preview
@Composable
private fun DragHandlePreview() {
    ClaudexTheme {
        Box(modifier = Modifier.height(200.dp).padding(8.dp)) {
            DragHandle()
        }
    }
}
