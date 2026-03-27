// commonMain
package dev.androidbroadcast.claudex.ui.component.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.ClaudexSpacing
import dev.androidbroadcast.claudex.ui.theme.LocalSpacing

/** A sidebar row representing a project entry. */
@Composable
public fun ProjectItem(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val itemModifier = rememberHoverableItemModifier(
        spacing = spacing,
        hoveredColor = MaterialTheme.colorScheme.surfaceVariant,
        defaultColor = MaterialTheme.colorScheme.surface,
        onClick = onClick,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(itemModifier),
    ) {
        ProjectIconPlaceholder(size = spacing.md, cornerRadius = spacing.xs)
        Spacer(modifier = Modifier.width(spacing.sm))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** A sidebar row representing a chat session, showing name and relative timestamp. */
@Composable
public fun SessionItem(
    name: String,
    timestamp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isRunning: Boolean = false,
    isSelected: Boolean = false,
) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.surfaceContainerHighest
        hovered -> MaterialTheme.colorScheme.surfaceVariant
        else -> Color.Transparent
    }

    val itemModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(spacing.xs))
        .background(bgColor)
        .hoverable(interactionSource)
        .clickable(onClick = onClick)
        .padding(horizontal = spacing.md, vertical = spacing.sm)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(itemModifier),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isRunning) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape,
                )
                .semantics {
                    if (isRunning) contentDescription = "Session running"
                },
        )
    }
}

/** A sidebar row for a primary action such as starting a new chat. */
@Composable
public fun ActionItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalSpacing.current
    val itemModifier = rememberHoverableItemModifier(
        spacing = spacing,
        hoveredColor = MaterialTheme.colorScheme.primaryContainer,
        defaultColor = MaterialTheme.colorScheme.surface,
        onClick = onClick,
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .then(itemModifier),
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Builds the shared hover-aware modifier used by ProjectItem and ActionItem.
 * Hover background switches from [defaultColor] to [hoveredColor] on pointer entry.
 */
@Composable
private fun rememberHoverableItemModifier(
    spacing: ClaudexSpacing,
    hoveredColor: Color,
    defaultColor: Color,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bgColor = if (hovered) hoveredColor else defaultColor
    return Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(spacing.xs))
        .background(bgColor)
        .hoverable(interactionSource)
        .clickable(onClick = onClick)
        .padding(horizontal = spacing.md, vertical = spacing.sm)
}

@Composable
private fun ProjectIconPlaceholder(size: Dp, cornerRadius: Dp) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {}
}
