// commonMain
package dev.androidbroadcast.claudex.ui.component.statusbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.LocalSpacing

private val ChipShape = RoundedCornerShape(4.dp)

/**
 * A compact chip for use in the application status bar.
 *
 * @param label Text displayed inside the chip.
 * @param showIcon When true, renders a small circular leading indicator.
 * @param onClick Optional click handler; if null the chip is non-interactive.
 * @param modifier Optional [Modifier].
 */
@Composable
public fun StatusBarChip(
    label: String,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val spacing = LocalSpacing.current
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier,
        shape = ChipShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.xs),
            modifier = Modifier.padding(horizontal = spacing.sm, vertical = spacing.xs),
        ) {
            if (showIcon) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = CircleShape,
                        ),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
