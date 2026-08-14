package com.xsc.oneapp.feature.attendance.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceCard
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceListRow
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceRisk
import com.xsc.oneapp.feature.attendance.ui.components.MetricTile
import com.xsc.oneapp.feature.attendance.ui.components.SectionHeading
import com.xsc.oneapp.feature.attendance.ui.components.StatusPill
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.oneapp.feature.attendance.ui.components.color
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceOverviewUiState
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceViewModel
import com.xsc.sdk.theme.OneAppSuccess
import com.xsc.sdk.theme.OneAppWarning

/**
 * The Attendance landing screen.
 *
 * The question a student opens this module to answer is "am I short?", and the question
 * faculty open it for is "what have I not submitted?". Both are answered here without a
 * tap. Everything else is a labelled route into a grouped section rather than a
 * competing top-level tab.
 */
@Composable
fun AttendanceOverviewScreen(
    onBack: () -> Unit,
    onOpenRecords: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenPolicy: () -> Unit,
    viewModel: AttendanceViewModel,
) {
    val state by viewModel.overview.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadOverview() }

    RecordScaffold(
        title = "Attendance",
        onBack = onBack,
        actions = { OverviewActions(onRefresh = viewModel::refreshOverview, onOpenPolicy = onOpenPolicy) }
    ) { padding ->
        // A failed headline call is the one case worth taking over the screen: without
        // it there is no attendance figure to show, and every tile below would read 0.
        val fatalError = state.errorMessage
        if ((fatalError != null) && !state.isLoading) {
            ErrorState(
                message = fatalError,
                onRetry = viewModel::refreshOverview,
                modifier = Modifier.padding(padding)
            )
            return@RecordScaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "headline") {
                ResponsiveContent { AttendanceHeadlineCard(state) }
            }

            item(key = "metrics") {
                ResponsiveContent {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricTile(
                            value = state.pendingSubmissions.toString(),
                            label = "Pending submissions",
                            icon = Icons.AutoMirrored.Filled.FactCheck,
                            tint = if (state.pendingSubmissions > 0) OneAppWarning else OneAppSuccess,
                            onClick = onOpenRecords,
                            modifier = Modifier.weight(1f)
                        )
                        MetricTile(
                            value = state.openRequests.toString(),
                            label = "Open requests",
                            icon = Icons.Default.ReportProblem,
                            tint = MaterialTheme.colorScheme.tertiary,
                            onClick = onOpenRequests,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item(key = "browse-heading") {
                ResponsiveContent { SectionHeading("Browse") }
            }

            item(key = "browse") {
                ResponsiveContent {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        NavigationCard(
                            icon = Icons.AutoMirrored.Filled.EventNote,
                            title = "Records",
                            subtitle = "Sessions, submissions and individual markings",
                            onClick = onOpenRecords
                        )
                        NavigationCard(
                            icon = Icons.Default.ReportProblem,
                            title = "Requests",
                            subtitle = "Correction exceptions and condonations",
                            onClick = onOpenRequests
                        )
                    }
                }
            }

            if (state.recentSessions.isNotEmpty()) {
                item(key = "recent-heading") {
                    ResponsiveContent { SectionHeading("Recent sessions") }
                }
                items(
                    items = state.recentSessions,
                    key = { session -> session.id ?: session.classSessionId.orEmpty() }
                ) { session ->
                    ResponsiveContent { RecentSessionCard(session) }
                }
            }
        }
    }
}

@Composable
private fun OverviewActions(onRefresh: () -> Unit, onOpenPolicy: () -> Unit) {
    var menuOpen by remember { mutableStateOf(value = false) }

    IconButton(onClick = onRefresh) {
        Icon(Icons.Default.Refresh, contentDescription = "Refresh attendance")
    }
    IconButton(onClick = { menuOpen = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More attendance options")
    }
    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        // Policy and marking types are institution reference data - correct to keep
        // reachable, wrong to spend a top-level slot on. Demoted from two of the eight
        // original tabs to one overflow entry.
        DropdownMenuItem(
            text = { Text("Policy & marking types") },
            leadingIcon = { Icon(Icons.Default.Policy, contentDescription = null) },
            onClick = {
                menuOpen = false
                onOpenPolicy()
            }
        )
    }
}

@Composable
private fun AttendanceHeadlineCard(state: AttendanceOverviewUiState) {
    val shortage = state.shortage
    val risk = AttendanceRisk.fromWire(shortage?.riskLevel)
    val riskColor = risk.color()
    val percent = shortage?.attendancePercentage?.toFloatOrNull()
    val required = shortage?.minRequiredPercentage

    AttendanceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "My attendance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatusPill(risk.label, tint = riskColor)
        }

        if (state.isLoading && shortage == null) {
            Text(
                "Loading your attendance…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
            return@AttendanceCard
        }

        if (shortage == null) {
            Text(
                "No attendance has been recorded for you yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp)
            )
            return@AttendanceCard
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AttendanceRing(percent = percent, tint = riskColor)

            Column(modifier = Modifier.weight(1f)) {
                if (required != null) {
                    Text(
                        "of $required% required",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val presentLine = listOfNotNull(shortage.presentSessions, shortage.totalSessions)
                if (presentLine.size == 2) {
                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatChip(label = "Present", value = shortage.presentSessions.orEmpty(), tint = OneAppSuccess)
                        StatChip(label = "Total", value = shortage.totalSessions.orEmpty(), tint = MaterialTheme.colorScheme.secondary)
                    }
                }

                if (shortage.isShortage == "true" && shortage.shortagePercentage != null) {
                    Text(
                        "${shortage.shortagePercentage}% short of the requirement",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = riskColor,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
    }
}

/** Determinate ring echoing the mockup's percentage dial, built from the same
 * [AttendanceShortage.attendancePercentage] the old linear bar read - no new data. */
@Composable
private fun AttendanceRing(percent: Float?, tint: Color) {
    Box(modifier = Modifier.size(88.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { 1f },
            color = MaterialTheme.colorScheme.surfaceVariant,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 8.dp,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxSize()
        )
        if (percent != null) {
            CircularProgressIndicator(
                progress = { (percent / 100f).coerceIn(0f, 1f) },
                color = tint,
                trackColor = Color.Transparent,
                strokeWidth = 8.dp,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "$percent percent attendance" }
            )
        }
        Text(
            percent?.let { "${it.toInt()}%" } ?: "—",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatChip(label: String, value: String, tint: Color) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.medium)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

@Composable
private fun NavigationCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    AttendanceCard(onClick = onClick) {
        AttendanceListRow(
            icon = icon,
            title = title,
            subtitle = subtitle,
            trailing = {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

@Composable
private fun RecentSessionCard(session: AttendanceSession) {
    AttendanceCard {
        AttendanceListRow(
            icon = Icons.Default.EventAvailable,
            title = session.remarks?.takeIf { it.isNotBlank() }
                ?: "Session ${session.classSessionId ?: "—"}",
            subtitle = session.sessionDate,
            iconTint = if (session.submittedAt != null) OneAppSuccess else OneAppWarning,
            trailing = {
                StatusPill(
                    if (session.submittedAt != null) "Submitted" else "Pending",
                    tint = if (session.submittedAt != null) OneAppSuccess else OneAppWarning
                )
            }
        )
    }
}
