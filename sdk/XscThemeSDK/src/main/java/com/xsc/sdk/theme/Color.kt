package com.xsc.sdk.theme

import androidx.compose.ui.graphics.Color

/**
 * OneApp brand palette.
 *
 * Screens should read `MaterialTheme.colorScheme`, not these constants directly - the
 * only reason they are public is that [OneAppTheme] assembles the scheme from them.
 *
 * The previous version defined six roles per mode (primary, secondary, tertiary,
 * background, surface, error) and let Material derive the other ~25. Those derived
 * values are computed from the *default* baseline palette rather than from the brand
 * indigo, which is why surfaces, outlines and every `*Container` role skewed grey-purple
 * and looked unrelated to the brand. Each role below is now stated explicitly, so a
 * card, a chip and a filled button share one visual family.
 *
 * Existing names are preserved so no call site changes.
 */

// --- Brand seeds (unchanged values - kept so the app's identity is untouched) ---
val OneAppPrimaryLight = Color(0xFF4F46E5)
val OneAppSecondaryLight = Color(0xFF06B6D4)
val OneAppTertiaryLight = Color(0xFF8B5CF6)
val OneAppBackgroundLight = Color(0xFFFAFAFA)
val OneAppSurfaceLight = Color(0xFFFFFFFF)
val OneAppErrorLight = Color(0xFFDC2626)

val OneAppPrimaryDark = Color(0xFF818CF8)
val OneAppSecondaryDark = Color(0xFF22D3EE)
val OneAppTertiaryDark = Color(0xFFA78BFA)
val OneAppBackgroundDark = Color(0xFF121212)
val OneAppSurfaceDark = Color(0xFF1E1E1E)
val OneAppErrorDark = Color(0xFFEF4444)

// --- Semantic status colors outside Material3's role set (no success/warning slot) ---
// Values deliberately unchanged: these are referenced directly by exam, fee, timetable
// and attendance screens, several as default parameter values. They are mid-tones that
// hold up on both light and dark surfaces.
//
// New code should prefer LocalStatusColors (see Theme.kt), which resolves the correct
// tone per mode - on a dark surface the light-mode pair sits closer to the background
// than it should for small label text.
val OneAppWarning = Color(0xFFF59E0B)
val OneAppSuccess = Color(0xFF10B981)

internal val OneAppWarningOnLight = Color(0xFFB45309)
internal val OneAppSuccessOnLight = Color(0xFF047857)
internal val OneAppWarningOnDark = Color(0xFFFBBF24)
internal val OneAppSuccessOnDark = Color(0xFF34D399)

// --- Light scheme, stated explicitly ---
internal val LightOnPrimary = Color(0xFFFFFFFF)
internal val LightPrimaryContainer = Color(0xFFE0E7FF)
internal val LightOnPrimaryContainer = Color(0xFF1E1B4B)

internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFCFFAFE)
internal val LightOnSecondaryContainer = Color(0xFF083344)

internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFFEDE9FE)
internal val LightOnTertiaryContainer = Color(0xFF2E1065)

internal val LightOnBackground = Color(0xFF111827)
internal val LightOnSurface = Color(0xFF111827)
// Neutral-cool grey rather than Material's default purple-tinted variant - the latter
// read as "unfinished template" against the indigo brand.
internal val LightSurfaceVariant = Color(0xFFF1F5F9)
internal val LightOnSurfaceVariant = Color(0xFF64748B)
internal val LightSurfaceTint = OneAppPrimaryLight
internal val LightInverseSurface = Color(0xFF1F2937)
internal val LightInverseOnSurface = Color(0xFFF9FAFB)
internal val LightInversePrimary = Color(0xFFA5B4FC)

internal val LightOutline = Color(0xFFCBD5E1)
internal val LightOutlineVariant = Color(0xFFE2E8F0)

internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFEE2E2)
internal val LightOnErrorContainer = Color(0xFF7F1D1D)

internal val LightScrim = Color(0xFF000000)

// --- Dark scheme, stated explicitly ---
internal val DarkOnPrimary = Color(0xFF1E1B4B)
internal val DarkPrimaryContainer = Color(0xFF312E81)
internal val DarkOnPrimaryContainer = Color(0xFFE0E7FF)

internal val DarkOnSecondary = Color(0xFF083344)
internal val DarkSecondaryContainer = Color(0xFF155E75)
internal val DarkOnSecondaryContainer = Color(0xFFCFFAFE)

internal val DarkOnTertiary = Color(0xFF2E1065)
internal val DarkTertiaryContainer = Color(0xFF5B21B6)
internal val DarkOnTertiaryContainer = Color(0xFFEDE9FE)

internal val DarkOnBackground = Color(0xFFF3F4F6)
internal val DarkOnSurface = Color(0xFFF3F4F6)
internal val DarkSurfaceVariant = Color(0xFF2A2A2E)
internal val DarkOnSurfaceVariant = Color(0xFF9CA3AF)
internal val DarkSurfaceTint = OneAppPrimaryDark
internal val DarkInverseSurface = Color(0xFFF3F4F6)
internal val DarkInverseOnSurface = Color(0xFF1F2937)
internal val DarkInversePrimary = Color(0xFF4F46E5)

internal val DarkOutline = Color(0xFF3F3F46)
internal val DarkOutlineVariant = Color(0xFF2A2A2E)

internal val DarkOnError = Color(0xFF450A0A)
internal val DarkErrorContainer = Color(0xFF7F1D1D)
internal val DarkOnErrorContainer = Color(0xFFFEE2E2)

internal val DarkScrim = Color(0xFF000000)
