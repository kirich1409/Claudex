package dev.androidbroadcast.claudex.ui.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.androidbroadcast.claudex.ui.theme.LocalSpacing

public enum class ButtonVariant { Primary, Ghost, Destructive }

@Composable
public fun ClaudexButton(
    label: String,
    variant: ButtonVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val spacing = LocalSpacing.current
    val contentPadding =
        remember(spacing.md, spacing.sm) {
            PaddingValues(horizontal = spacing.md, vertical = spacing.sm)
        }
    when (variant) {
        ButtonVariant.Primary ->
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                contentPadding = contentPadding,
            ) { Text(label) }

        ButtonVariant.Ghost ->
            OutlinedButton(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                contentPadding = contentPadding,
            ) { Text(label) }

        ButtonVariant.Destructive ->
            Button(
                onClick = onClick,
                enabled = enabled,
                modifier = modifier,
                contentPadding = contentPadding,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
            ) { Text(label) }
    }
}

/**
 * An icon-only button with an accessible [contentDescription].
 *
 * When [icon] is null the button renders as a tappable area with no visual content — useful
 * for wrapping custom content via slot or for accessibility-only nodes in tests.
 */
@Composable
public fun ClaudexIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
