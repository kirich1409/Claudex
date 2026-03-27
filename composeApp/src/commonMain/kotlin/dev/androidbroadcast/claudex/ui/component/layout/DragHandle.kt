// commonMain
package dev.androidbroadcast.claudex.ui.component.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp

/**
 * A subtle vertical drag handle for resizing adjacent panels.
 *
 * Renders a 4.dp-wide bar that fills its parent's height, coloured with
 * [MaterialTheme.colorScheme.surfaceContainerHigh]. On desktop, the cursor
 * changes to [PointerIcon.Hand] on hover.
 *
 * @param modifier Optional [Modifier].
 */
@Composable
public fun DragHandle(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .pointerHoverIcon(PointerIcon.Hand),
    )
}
