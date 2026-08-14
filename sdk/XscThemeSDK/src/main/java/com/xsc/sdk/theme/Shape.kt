package com.xsc.sdk.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The app's corner scale.
 *
 * Matches the Academic Interface System design system's rounded scale (Stitch export,
 * 2026-08): sm=4dp, DEFAULT=8dp (buttons/inputs, per the design system's own docs),
 * md=12dp, lg=16dp (cards, per the design system's own docs), xl=24dp. `medium` below
 * is deliberately the design system's `lg`(16dp), not `md`(12dp) - verified against
 * XscCommonUI's RecordCard, the shared card component every feature module's list
 * screens build on, which reads `MaterialTheme.shapes.medium` directly, so this is
 * what actually determines real card corner radii app-wide, not just a label choice.
 */
val OneAppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

/** Fully rounded - status pills, avatars, icon buttons. */
val OneAppPillShape = RoundedCornerShape(percent = 50)
