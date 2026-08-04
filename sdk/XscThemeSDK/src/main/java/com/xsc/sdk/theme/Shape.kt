package com.xsc.sdk.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * The app's corner scale.
 *
 * [OneAppTheme] passed no shapes before, so components used Material's defaults while
 * screens hardcoded their own - 14dp on buttons, 16dp on attendance cards, 20dp on the
 * dummy module tile, 24dp on the login card. Those are all "rounded", but the
 * inconsistency is visible when two of them sit on one screen.
 *
 * Slightly softer than Material's baseline at the medium and large steps, which is what
 * gives cards a premium rather than utilitarian feel, while keeping small controls
 * tight enough to read as controls.
 */
val OneAppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

/** Fully rounded - status pills, avatars, icon buttons. */
val OneAppPillShape = RoundedCornerShape(percent = 50)
