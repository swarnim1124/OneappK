package com.xsc.sdk.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * Material 3 motion tokens, defined once so animation across the app shares a rhythm.
 *
 * Durations follow the M3 spec: interaction feedback under 100ms, small transitions
 * 200-300ms, larger container transitions 400-500ms. Anything longer starts to feel
 * like lag rather than polish.
 *
 * Two rules these encode:
 *  - Emphasized easing (the slow-in/slow-out curve) for anything entering or leaving
 *    the screen; standard easing for state changes within an element.
 *  - Springs, not tweens, for anything the user is directly manipulating (press,
 *    drag), because a spring reacts to interruption instead of restarting.
 */
object OneAppMotion {

    // --- Durations (ms) ---
    const val DurationInstant = 80
    const val DurationShort = 150
    const val DurationMedium = 250
    const val DurationLong = 400
    const val DurationExtraLong = 500

    // --- Easing curves, per the M3 motion spec ---
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Press/release feedback. Interruptible by design - a spring retargets rather
     * than restarting, so rapid taps never look stuttery. */
    fun <T> pressSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    /** Content settling into place - elevation, size, colour. */
    fun <T> settleSpring(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    fun <T> shortTween(): FiniteAnimationSpec<T> = tween(DurationShort, easing = Standard)

    fun <T> mediumTween(): FiniteAnimationSpec<T> = tween(DurationMedium, easing = Standard)

    /**
     * Standard content entrance: a short rise with a fade.
     *
     * [indexOffset] staggers a list of sections so they arrive in sequence rather than
     * all at once - the detail that separates "animated" from "designed". Kept small;
     * a long stagger delays the user's first possible interaction.
     */
    fun contentEnter(indexOffset: Int = 0): EnterTransition {
        val delay = (indexOffset * 40).coerceAtMost(200)
        return fadeIn(
            animationSpec = tween(DurationMedium, delayMillis = delay, easing = Standard)
        ) + slideInVertically(
            animationSpec = tween(DurationLong, delayMillis = delay, easing = EmphasizedDecelerate),
            initialOffsetY = { fullHeight -> (fullHeight * 0.15f).toInt().coerceAtMost(48) }
        )
    }

    fun contentExit(): ExitTransition = fadeOut(
        animationSpec = tween(DurationShort, easing = EmphasizedAccelerate)
    ) + slideOutVertically(
        animationSpec = tween(DurationShort, easing = EmphasizedAccelerate),
        targetOffsetY = { fullHeight -> -(fullHeight * 0.05f).toInt() }
    )

    /** Inline banners and validation messages. Expands from zero height so surrounding
     * content moves smoothly instead of jumping. */
    fun bannerEnter(): EnterTransition =
        fadeIn(animationSpec = tween(DurationShort)) +
            expandVertically(
                animationSpec = tween(DurationMedium, easing = EmphasizedDecelerate)
            )

    fun bannerExit(): ExitTransition =
        fadeOut(animationSpec = tween(DurationInstant)) +
            shrinkVertically(
                animationSpec = tween(DurationShort, easing = EmphasizedAccelerate)
            )

    /** Scale a pressed surface settles to. 0.97 is deliberately subtle - large press
     * scales read as toy-like on an enterprise surface. */
    const val PressedScale = 0.97f
}
