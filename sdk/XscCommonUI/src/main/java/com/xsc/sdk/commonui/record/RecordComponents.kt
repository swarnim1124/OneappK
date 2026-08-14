@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.sdk.commonui.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppMotion
import kotlinx.coroutines.delay

/**
 * Shared primitives for the app's record-browsing screens.
 *
 * Exam, Fee, Timetable, Curriculum and Attendance each shipped private copies of the
 * same six components - a card, an icon badge, a status pill, and Loading / Empty /
 * Error bodies - so a spacing or colour change had to be made five times and never was.
 * These are deliberately generic: they take plain values, not `UiState`, so this module
 * needs no dependency on `:core` and each feature keeps its own state handling.
 */

/** Consistent chrome for a record screen. */
@Composable
fun RecordScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate up"
                        )
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = floatingActionButton,
        containerColor = MaterialTheme.colorScheme.background,
        content = content
    )
}

/** Standard content card. Supplying [onClick] makes it a real M3 clickable surface,
 * which brings button semantics, ripple and a 48dp-minimum target.
 *
 * Uses a soft drop shadow rather than a hairline border to read as elevated above the
 * page - on this palette `surface` and `background` are nearly identical tones, so a
 * border-only card was nearly invisible. Matches the design system's "Level 1 (Cards):
 * 1dp shadow" spec and the 1dp precedent already established by Attendance's own card.
 *
 * [accentColor], when supplied, paints a 4dp status stripe down the card's leading edge -
 * the design system's own status-history cards use this to make an outcome (e.g. a
 * declined vs. a successful request) scannable without reading every pill. Card's own
 * shape clip means the stripe automatically follows the rounded corners; omitted
 * entirely by default, so every existing call site renders identically to before. */
@Composable
fun RecordCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    val innerContent: @Composable ColumnScope.() -> Unit = {
        if (accentColor != null) {
            // IntrinsicSize.Min forces the Row to measure its height as whatever the
            // (intrinsically-sized) content Column needs, so the stripe's fillMaxHeight()
            // has something concrete to fill - without it a Row's height is otherwise
            // unbounded and fillMaxHeight() has nothing to measure against.
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accentColor)
                )
                Column(modifier = Modifier.padding(spacing.lg), content = content)
            }
        } else {
            Column(modifier = Modifier.padding(spacing.lg), content = content)
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            elevation = elevation,
            content = innerContent
        )
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            elevation = elevation,
            content = innerContent
        )
    }
}

/** Headline row inside a [RecordCard]: badge, title, optional subtitle, optional trailing. */
@Composable
fun RecordRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trailing: (@Composable () -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            IconBadge(icon, iconTint)
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (trailing != null) {
            Box(modifier = Modifier.padding(start = spacing.sm)) { trailing() }
        }
    }
}

/** Matches the design system's "Icon-in-tinted-circle" spec exactly: 40x40dp
 * background at 10% opacity of the tint, icon centered at full opacity. */
@Composable
fun IconBadge(icon: ImageVector, tint: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(tint.copy(alpha = 0.10f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = tint,
        modifier = modifier
            .background(tint.copy(alpha = 0.12f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/**
 * A small tinted label, optionally with a leading icon - Timetable's room/faculty
 * badge and Curriculum's course-code badge were separately-written versions of this
 * same idea (a compact contextual tag), just with an icon in one and not the other,
 * and two different colour/shape treatments. This follows [StatusPill]'s established
 * alpha-tint convention (already the app's dominant "pill" language) rather than
 * inventing a third, so a badge and a status pill read as the same visual family.
 */
@Composable
fun TintedChip(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .background(tint.copy(alpha = 0.1f), MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

/** Secondary detail line under a record's headline. */
@Composable
fun DetailText(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

/**
 * Section switcher. Chips rather than a ScrollableTabRow, which overflowed the width on
 * every phone and left later options permanently off-screen with no affordance that
 * they existed.
 */
@Composable
fun SectionChips(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            FilterChip(
                selected = selected,
                onClick = { onSelect(index) },
                label = { Text(label) },
                leadingIcon = if (selected) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
            )
        }
    }
}

/**
 * Loading body. Held back briefly so a fast response looks instant rather than
 * flashing a spinner for one frame.
 */
@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(SPINNER_DELAY_MS)
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = "Loading" },
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(visible = visible, enter = OneAppMotion.bannerEnter()) {
            CircularProgressIndicator()
        }
    }
}

/** [actionLabel]/[onAction], when both supplied, render a [PrimaryButton] below
 * [message] - the profile detail screens' "Reload" affordance on an empty record. */
@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = spacing.xxl)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Inbox,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(spacing.xl))
                Box(modifier = Modifier.widthIn(max = 280.dp)) {
                    PrimaryButton(text = actionLabel, onClick = onAction)
                }
            }
        }
    }
}

/**
 * [context], when supplied, renders as a second, smaller line below [message] - the
 * permission/endpoint/reference-id detail of a traced API failure (see
 * [com.xsc.oneapp.core.result.AppError.Traced.context]). Kept as a plain optional
 * String rather than taking an `AppError` directly: this module has no dependency on
 * `:core`, so the caller (which does) destructures its own `UiState`/`AppError`
 * before calling this - see CurriculumScreen/FeeScreen/TimetableScreen/ExamScreen
 * for the pattern.
 */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier, context: String? = null) {
    val spacing = LocalSpacing.current

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = spacing.xxl)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.errorContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(spacing.lg))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (context != null) {
                Text(
                    context,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = spacing.xs)
                )
            }
            TextButton(onClick = onRetry, modifier = Modifier.padding(top = spacing.xs)) {
                Text("Try again")
            }
        }
    }
}

/** Caps content width so cards do not stretch to an unreadable line length on tablets.
 * [verticalArrangement]/[horizontalAlignment] default to [Column]'s own defaults (no
 * gap, start-aligned) - Profile's form/detail screens pass a spaced arrangement for
 * their stacked section cards. */
@Composable
fun ResponsiveContent(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = spacing.contentMaxWidth),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = content
        )
    }
}

/**
 * Honest terminal state for a flow whose real backend action doesn't exist yet (no
 * payment gateway, no submit-correction use case, etc.) - a fabricated success screen
 * here would tell the user something happened when it didn't. Fee and Attendance each
 * shipped a private copy of this same scaffold-plus-centered-icon-and-message layout;
 * this is that layout, with the icon/copy left to the caller since those are the only
 * things that differ per feature.
 */
@Composable
fun NotYetAvailableScreen(
    title: String,
    icon: ImageVector,
    headline: String,
    message: String,
    onBack: () -> Unit
) {
    RecordScaffold(title = title, onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.weight(1f))
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                headline,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            Box(modifier = Modifier.weight(1f))
        }
    }
}

private const val SPINNER_DELAY_MS = 250L
