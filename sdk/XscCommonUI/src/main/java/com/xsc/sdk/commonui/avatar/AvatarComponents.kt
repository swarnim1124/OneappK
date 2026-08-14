package com.xsc.sdk.commonui.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gradient initials circle. Dashboard (its top bar, sidebar header, and profile tab)
 * and Profile each carried an independent copy of this exact same primary-to-tertiary
 * gradient circle - not because the design differed, but because the two feature
 * modules can't depend on each other, so neither could reuse the other's copy. This is
 * that component, once, at a home both can import.
 *
 * Decorative by default (the name is almost always rendered next to or below it, so
 * announcing the initial again would be noise) - callers that use this as the sole
 * content of a clickable target (e.g. a top-bar icon button) should put their own
 * `contentDescription` on that outer element instead.
 */
@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium
) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                ),
                CircleShape
            )
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().take(1).ifBlank { "?" }.uppercase(),
            style = textStyle,
            color = Color.White
        )
    }
}
