@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.profile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppMotion
import kotlinx.coroutines.delay

/**
 * Shared Profile presentation kit.
 *
 * The six detail screens each carried private copies of the same section card, label/
 * value row, loading spinner and empty state. Consolidated here so a change to "how a
 * profile field looks" happens once. No state, event or ViewModel call moved.
 *
 * [ErrorState]'s signature is deliberately unchanged - all six screens call it as
 * `ErrorState(message = ...) { retry }`.
 */

/**
 * Consistent chrome for every Profile destination.
 *
 * Note the back affordance: the detail screens already received an `onNavigateBack`
 * callback and never rendered anything that called it, so the only way out was the
 * system back gesture. This wires the existing callback to a standard up button - no new
 * navigation, just an affordance for navigation that was already there.
 */
@Composable
fun ProfileScaffold(
    title: String,
    onNavigateBack: () -> Unit,
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
                    IconButton(onClick = onNavigateBack) {
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

/** Caps content width so forms and value lists stay readable on tablets. */
@Composable
fun ProfileContentColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier.widthIn(max = spacing.contentMaxWidth),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
            content = content
        )
    }
}

/** Gradient monogram used by the profile header and the personal detail screen. */
@Composable
fun ProfileAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 80.dp) {
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
            // Decorative: the name is rendered beside it, so announcing the initial
            // again would just be noise.
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.trim().take(1).ifBlank { "?" }.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )
    }
}

/**
 * A titled group of label/value pairs.
 *
 * Rows whose value is blank are dropped rather than rendered as an em dash. A column of
 * placeholders is noise; the absence of the row carries the same information with less
 * to read past. Pass `showBlankRows = true` where the gap itself matters.
 */
@Composable
fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
    showBlankRows: Boolean = false
) {
    val spacing = LocalSpacing.current
    val visibleRows = if (showBlankRows) rows else rows.filter { it.second.isNotBlank() }
    if (visibleRows.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        visibleRows.forEachIndexed { index, (label, value) ->
            ProfileValueRow(label, value)
            if (index != visibleRows.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = spacing.lg)
                )
            }
        }
    }
}

/**
 * One label/value line. The pair is merged into a single announcement so TalkBack reads
 * "Mobile, 98765 43210" rather than two disconnected fragments.
 */
@Composable
fun ProfileValueRow(label: String, value: String, modifier: Modifier = Modifier) {
    val spacing = LocalSpacing.current
    val displayValue = value.ifBlank { "—" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = spacing.lg, vertical = spacing.md)
            .clearAndSetSemantics { contentDescription = "$label, $displayValue" },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = displayValue,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = spacing.md)
        )
    }
}

/**
 * Icon + caption strip that introduces a screen's purpose. Four detail screens each had
 * their own copy of this exact 40dp-circle-plus-text row.
 */
@Composable
fun ProfileHeaderRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * A person record with a monogram, optional badge, contact lines and edit/delete
 * actions. Family Details and Emergency Contacts rendered structurally identical cards
 * with two independent copies of the layout; this is that card, once.
 */
@Composable
fun ProfilePersonCard(
    name: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    badge: String? = null,
    contactLines: List<Pair<ImageVector, String>> = emptyList(),
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = name.trim().take(1).ifBlank { "?" }.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Text(
                                text = name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (badge != null) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer,
                                            CircleShape
                                        )
                                        .padding(horizontal = spacing.sm, vertical = 2.dp)
                                )
                            }
                        }
                        if (!subtitle.isNullOrBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Row {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit $name",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "Delete $name",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            val visibleLines = contactLines.filter { it.second.isNotBlank() }
            if (visibleLines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(spacing.md))
                Column(verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    visibleLines.forEach { (icon, value) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                value,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Container for a group of editable fields.
 *
 * Replaces the `Column(background + border + padding)` block each form screen built by
 * hand, so field spacing and the card treatment can't drift between screens.
 */
@Composable
fun ProfileFormCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalSpacing.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg),
            content = content
        )
    }
}

/**
 * Loading indicator that only appears after a short delay.
 *
 * A spinner that flashes for 80ms on a fast response reads as a glitch; holding it back
 * makes quick loads look instant while slow ones still get feedback.
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
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
            text = message,
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

/**
 * Signature unchanged - every Profile detail screen calls this as
 * `ErrorState(message = ...) { retry }`.
 */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xxl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(spacing.lg))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(spacing.xl))
        Box(modifier = Modifier.widthIn(max = 280.dp)) {
            PrimaryButton(text = "Retry", onClick = onRetry)
        }
    }
}

private const val SPINNER_DELAY_MS = 250L
