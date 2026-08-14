package com.xsc.sdk.theme

import androidx.compose.ui.graphics.Color

/**
 * OneApp brand palette.
 *
 * Screens should read `MaterialTheme.colorScheme`, not these constants directly - the
 * only reason they are public is that [OneAppTheme] assembles the scheme from them.
 *
 * Values below match the Academic Interface System design system exactly (Stitch
 * export, 2026-08 - see `academic_interface_system/DESIGN.md` in the delivered zip),
 * a Material Theme Builder-generated indigo scheme. Light-mode values are the
 * design system's literal tokens. Dark mode has no Stitch-provided tokens for this
 * seed, so primary/secondary/tertiary use the design system's own `*-fixed`/
 * `*-fixed-dim` roles - the standard M3 relationship a light scheme's fixed roles
 * already encode the correct dark-mode counterpart, so these aren't invented values.
 * Error dark values are Material Theme Builder's well-known standard output for the
 * `#BA1A1A` seed. Neutral dark surfaces stay close to the existing deep-charcoal
 * baseline, consistent with the design system's own prose ("deep charcoal #1C1B1F").
 *
 * Existing names are preserved so no call site changes.
 */

// --- Brand seeds ---
val OneAppPrimaryLight = Color(0xFF24389C)
val OneAppSecondaryLight = Color(0xFF5B5D70)
val OneAppTertiaryLight = Color(0xFF5A384F)
val OneAppBackgroundLight = Color(0xFFFDF8FC)
val OneAppSurfaceLight = Color(0xFFFDF8FC)
val OneAppErrorLight = Color(0xFFBA1A1A)

val OneAppPrimaryDark = Color(0xFFBAC3FF)
val OneAppSecondaryDark = Color(0xFFC4C5DB)
val OneAppTertiaryDark = Color(0xFFE8B9D5)
val OneAppBackgroundDark = Color(0xFF1C1B1F)
val OneAppSurfaceDark = Color(0xFF1C1B1F)
val OneAppErrorDark = Color(0xFFFFB4AB)

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
internal val LightPrimaryContainer = Color(0xFF3F51B5)
internal val LightOnPrimaryContainer = Color(0xFFCACFFF)

internal val LightOnSecondary = Color(0xFFFFFFFF)
internal val LightSecondaryContainer = Color(0xFFE0E1F8)
internal val LightOnSecondaryContainer = Color(0xFF616376)

internal val LightOnTertiary = Color(0xFFFFFFFF)
internal val LightTertiaryContainer = Color(0xFF744F67)
internal val LightOnTertiaryContainer = Color(0xFFF4C5E1)

internal val LightOnBackground = Color(0xFF1C1B1E)
internal val LightOnSurface = Color(0xFF1C1B1E)
internal val LightSurfaceVariant = Color(0xFFE5E1E5)
internal val LightOnSurfaceVariant = Color(0xFF454652)
internal val LightSurfaceTint = Color(0xFF4355B9)
internal val LightInverseSurface = Color(0xFF313033)
internal val LightInverseOnSurface = Color(0xFFF4EFF4)
internal val LightInversePrimary = Color(0xFFBAC3FF)

internal val LightOutline = Color(0xFF757684)
internal val LightOutlineVariant = Color(0xFFC5C5D4)

internal val LightOnError = Color(0xFFFFFFFF)
internal val LightErrorContainer = Color(0xFFFFDAD6)
internal val LightOnErrorContainer = Color(0xFF93000A)

internal val LightScrim = Color(0xFF000000)

// M3 surface-container ladder - not part of the previous 6-role scheme, added because
// the design system uses it explicitly (e.g. surface-container-lowest for card-on-card
// contexts). Values are the design system's literal tokens.
internal val LightSurfaceDim = Color(0xFFDDD9DD)
internal val LightSurfaceBright = Color(0xFFFDF8FC)
internal val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
internal val LightSurfaceContainerLow = Color(0xFFF7F2F6)
internal val LightSurfaceContainer = Color(0xFFF1ECF1)
internal val LightSurfaceContainerHigh = Color(0xFFEBE7EB)
internal val LightSurfaceContainerHighest = Color(0xFFE5E1E5)

// --- Dark scheme, stated explicitly ---
// primary/secondary/tertiary families derived from the light scheme's own fixed roles
// (on-primary-fixed, primary-fixed, on-primary-fixed-variant, etc.) - see file doc comment.
internal val DarkOnPrimary = Color(0xFF00105C)
internal val DarkPrimaryContainer = Color(0xFF293CA0)
internal val DarkOnPrimaryContainer = Color(0xFFDEE0FF)

internal val DarkOnSecondary = Color(0xFF181B2B)
internal val DarkSecondaryContainer = Color(0xFF434658)
internal val DarkOnSecondaryContainer = Color(0xFFE0E1F8)

internal val DarkOnTertiary = Color(0xFF2E1126)
internal val DarkTertiaryContainer = Color(0xFF5E3C53)
internal val DarkOnTertiaryContainer = Color(0xFFFFD8EE)

internal val DarkOnBackground = Color(0xFFE6E1E6)
internal val DarkOnSurface = Color(0xFFE6E1E6)
internal val DarkSurfaceVariant = Color(0xFF47464F)
internal val DarkOnSurfaceVariant = Color(0xFFC9C5D0)
internal val DarkSurfaceTint = OneAppPrimaryDark
internal val DarkInverseSurface = Color(0xFFE6E1E6)
internal val DarkInverseOnSurface = Color(0xFF313033)
internal val DarkInversePrimary = Color(0xFF4355B9)

internal val DarkOutline = Color(0xFF91909E)
internal val DarkOutlineVariant = Color(0xFF47464F)

internal val DarkOnError = Color(0xFF690005)
internal val DarkErrorContainer = Color(0xFF93000A)
internal val DarkOnErrorContainer = Color(0xFFFFDAD6)

internal val DarkScrim = Color(0xFF000000)

internal val DarkSurfaceDim = Color(0xFF1C1B1F)
internal val DarkSurfaceBright = Color(0xFF444347)
internal val DarkSurfaceContainerLowest = Color(0xFF17161A)
internal val DarkSurfaceContainerLow = Color(0xFF242327)
internal val DarkSurfaceContainer = Color(0xFF28272B)
internal val DarkSurfaceContainerHigh = Color(0xFF333136)
internal val DarkSurfaceContainerHighest = Color(0xFF3E3D41)
