package com.xsc.oneapp.feature.attendance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.core.result.UiState
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.theme.OneAppSuccess
import com.xsc.sdk.theme.OneAppWarning

/**
 * Every Attendance destination renders through the same primitives so the section
 * screens contain layout intent only. Previously each of the eight tabs carried its own
 * copy of the `when (state) { Loading -> ...; Success -> LazyColumn { ... } ... }`
 * block plus private duplicates of LoadingState/EmptyState/MessageState.
 *
 * The scaffold, icon badge, and loading/empty/error triad are [com.xsc.sdk.commonui.record]'s
 * (imported above) rather than a private copy - they were byte-for-byte identical to
 * the versions Exam/Fee/Curriculum/Timetable already share. [AttendanceCard],
 * [AttendanceListRow] and [StatusPill] stay local: they carry this module's own bolder
 * type weight and colour defaults, which would visibly change if merged.
 */

/**
 * The single place a `UiState<List<T>>` becomes a list, a spinner, an empty state or a
 * retryable error. Callers supply only the row.
 *
 * [key] is required rather than defaulted to `hashCode()` (what the old tabs used):
 * these models are all-nullable data classes, so two rows that differ only in a null
 * field collide, and Compose then reuses the wrong item state on update.
 */
@Composable
fun <T> SectionList(
    state: UiState<List<T>>,
    emptyMessage: String,
    onRetry: () -> Unit,
    key: (T) -> Any,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingState(modifier)
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState(emptyMessage, modifier)
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.data, key = key) { row -> itemContent(row) }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry, modifier)
        is UiState.NetworkError -> ErrorState(state.message, onRetry, modifier)
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            modifier,
            context = (state.appError as? AppError.Traced)?.context
        )
    }
}

/** Standard content card. [onClick] is optional - when supplied the card becomes a
 * real M3 clickable surface, which brings correct button semantics, ripple and a
 * 48dp-minimum target for free. The old screens hand-rolled `Box(Modifier.clickable)`
 * with none of that. */
@Composable
fun AttendanceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    val shape = RoundedCornerShape(16.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            colors = colors,
            elevation = elevation,
            shape = shape
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = colors,
            elevation = elevation,
            shape = shape
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

/** A labelled row inside a card: icon badge, title, optional subtitle, optional
 * trailing slot. Every list row across the module is built from this, so titles,
 * spacing and icon sizing can't drift between screens. */
@Composable
fun AttendanceListRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(icon, iconTint)
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (trailing != null) {
            Box(modifier = Modifier.padding(start = 8.dp)) { trailing() }
        }
    }
}

@Composable
fun StatusPill(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = OneAppSuccess
) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = tint,
        modifier = modifier
            .background(tint.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Section heading inside a scrolling screen. */
@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = 8.dp, bottom = 2.dp)
    )
}

/** Presentation mapping for the backend's `risk_level` string. */
enum class AttendanceRisk(val label: String) {
    SAFE("On track"),
    WARNING("At risk"),
    SEVERE("Shortage"),
    UNKNOWN("Not evaluated");

    companion object {
        fun fromWire(raw: String?): AttendanceRisk = when (raw?.uppercase()) {
            "SAFE" -> SAFE
            "WARNING" -> WARNING
            "SEVERE" -> SEVERE
            else -> UNKNOWN
        }
    }
}

@Composable
fun AttendanceRisk.color(): Color = when (this) {
    AttendanceRisk.SAFE -> OneAppSuccess
    AttendanceRisk.WARNING -> OneAppWarning
    AttendanceRisk.SEVERE -> MaterialTheme.colorScheme.error
    AttendanceRisk.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
}

/**
 * Compact metric tile used on the overview. Announced to TalkBack as a single phrase
 * ("3 pending submissions") instead of the number and the label being read as two
 * unrelated fragments.
 */
@Composable
fun MetricTile(
    value: String,
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    AttendanceCard(
        modifier = modifier
            .heightIn(min = 96.dp)
            .clearAndSetSemantics { contentDescription = "$value $label" },
        onClick = onClick
    ) {
        IconBadge(icon, tint)
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
