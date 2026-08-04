@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.sdk.commonui.record

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
        containerColor = MaterialTheme.colorScheme.background,
        content = content
    )
}

/** Standard content card. Supplying [onClick] makes it a real M3 clickable surface,
 * which brings button semantics, ripple and a 48dp-minimum target. */
@Composable
fun RecordCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    val border = androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outlineVariant
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            border = border,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(spacing.lg), content = content)
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = colors,
            border = border,
            elevation = elevation
        ) {
            Column(modifier = Modifier.padding(spacing.lg), content = content)
        }
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

@Composable
fun IconBadge(icon: ImageVector, tint: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(tint.copy(alpha = 0.12f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
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

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
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
        }
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
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
            TextButton(onClick = onRetry, modifier = Modifier.padding(top = spacing.xs)) {
                Text("Try again")
            }
        }
    }
}

/** Caps content width so cards do not stretch to an unreadable line length on tablets. */
@Composable
fun ResponsiveContent(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val spacing = LocalSpacing.current
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.widthIn(max = spacing.contentMaxWidth), content = content)
    }
}

private const val SPINNER_DELAY_MS = 250L
