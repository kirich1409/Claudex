// commonMain
package dev.androidbroadcast.claudex.ui.component.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.semantics.clearAndSetSemantics
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
    val itemModifier =
        hoverableItemModifier(
            spacing = spacing,
            hoveredColor = MaterialTheme.colorScheme.surfaceVariant,
            defaultColor = MaterialTheme.colorScheme.surface,
            onClick = onClick,
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
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
    val itemModifier =
        hoverableItemModifier(
            spacing = spacing,
            hoveredColor = MaterialTheme.colorScheme.surfaceVariant,
            defaultColor = Color.Transparent,
            onClick = onClick,
            isSelected = isSelected,
            selectedColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )

    val dotModifier =
        if (isRunning) {
            Modifier
                .size(8.dp)
                .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                .semantics { contentDescription = "Session running" }
        } else {
            Modifier
                .size(8.dp)
                .clearAndSetSemantics { }
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = modifier.then(itemModifier),
    ) {
        Box(modifier = dotModifier)
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
    val itemModifier =
        hoverableItemModifier(
            spacing = spacing,
            hoveredColor = MaterialTheme.colorScheme.primaryContainer,
            defaultColor = MaterialTheme.colorScheme.surface,
            onClick = onClick,
        )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
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
 * Builds the shared hover-aware modifier used by sidebar items.
 * Hover background switches from [defaultColor] to [hoveredColor] on pointer entry.
 * When [isSelected] is true and [selectedColor] is provided, the selected color takes priority.
 */
@Composable
private fun hoverableItemModifier(
    spacing: ClaudexSpacing,
    hoveredColor: Color,
    defaultColor: Color,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    selectedColor: Color = Color.Unspecified,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val bgColor =
        when {
            isSelected && selectedColor != Color.Unspecified -> selectedColor
            hovered -> hoveredColor
            else -> defaultColor
        }
    return Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(spacing.xs))
        .background(bgColor)
        .hoverable(interactionSource)
        .clickable(onClick = onClick)
        .padding(horizontal = spacing.md, vertical = spacing.sm)
}

@Composable
private fun ProjectIconPlaceholder(
    size: Dp,
    cornerRadius: Dp,
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {}
}
