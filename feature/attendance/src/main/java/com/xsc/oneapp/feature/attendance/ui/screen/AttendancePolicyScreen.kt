package com.xsc.oneapp.feature.attendance.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceCard
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceListRow
import com.xsc.oneapp.feature.attendance.ui.components.SectionList
import com.xsc.oneapp.feature.attendance.ui.components.StatusPill
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceViewModel
import com.xsc.sdk.theme.OneAppSuccess
import com.xsc.sdk.theme.OneAppWarning

/**
 * Institution reference data: the attendance policy in force, and the marking codes it
 * recognises.
 *
 * Reached from the overview's overflow menu rather than a top-level tab. These are the
 * two least frequently opened data sets in the module and the only two a student never
 * acts on - they answer "what are the rules?", not "what is my situation?". Keeping
 * them one tap deeper is the single biggest reduction in top-level noise.
 */
private val TABS = listOf("Policies", "Marking types")

@Composable
fun AttendancePolicyScreen(
    onBack: () -> Unit,
    viewModel: AttendanceViewModel
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }

    val configurationsState by viewModel.configurations.state.collectAsStateWithLifecycle()
    val typesState by viewModel.types.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadPolicy() }

    RecordScaffold(title = "Policy & marking types", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            if (selected == 0) {
                SectionList(
                    state = configurationsState,
                    emptyMessage = "No attendance policy has been published yet.",
                    onRetry = viewModel.configurations::reload,
                    key = { it.id ?: it.hashCode() }
                ) { PolicyRow(it) }
            } else {
                SectionList(
                    state = typesState,
                    emptyMessage = "No attendance marking types configured yet.",
                    onRetry = viewModel.types::reload,
                    key = { it.id ?: it.hashCode() }
                ) { TypeRow(it) }
            }
        }
    }
}

/**
 * Policy flags were previously concatenated into one run-on sentence joined by
 * middots. They are discrete boolean facts, so they read as chips - scannable, and
 * they wrap instead of truncating on a narrow screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PolicyRow(config: AttendanceConfiguration) {
    AttendanceCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (config.minAttendancePercent != null) {
                Text(
                    "${config.minAttendancePercent}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            AttendanceListRow(
                icon = Icons.Default.Policy,
                title = config.policyName ?: "Attendance policy",
                subtitle = config.minAttendancePercent?.let { "Minimum required" },
                trailing = if (config.isActive == "true") {
                    { StatusPill("Active") }
                } else null,
                modifier = Modifier.weight(1f)
            )
        }

        val flags = buildList {
            if (config.allowLateMarking == "true") add("Late marking allowed")
            if (config.requireApproval == "true") add("Approval required")
            if (config.requireMedicalProof == "true") add("Medical proof required")
            config.lateMarkingWindowMinutes?.let { add("$it min late window") }
            config.autoLockAfterHours?.let { add("Auto-locks after ${it}h") }
        }
        if (flags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Read-only tags, so a plain styled label rather than a disabled
                // AssistChip - a disabled chip renders greyed out and reads to
                // accessibility services as an unavailable control, which these
                // are not. They are facts, not actions.
                flags.forEach { flag ->
                    StatusPill(flag, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (config.effectiveFrom != null) {
            val range = config.effectiveTo?.let { "${config.effectiveFrom} – $it" }
                ?: "From ${config.effectiveFrom}"
            DetailText(range, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun TypeRow(type: AttendanceType) {
    val (icon, tint) = markingTypeStyle(type.lookupCode, type.lookupName)

    AttendanceCard {
        AttendanceListRow(
            icon = icon,
            title = type.lookupName ?: "Marking type",
            subtitle = type.lookupCode,
            iconTint = tint,
            trailing = if (type.isActive == "true") {
                { StatusPill("Active") }
            } else null
        )
    }
}

/**
 * Reads the icon/colour from this institution's own marking code (`lookupCode`) and
 * name rather than assuming a fixed universal set of codes - institutions define their
 * own via sch_lookup.tb_lookup cat_id=70 (see AttendanceType's doc comment), so a code
 * this doesn't recognise falls open to a neutral badge instead of guessing.
 */
@Composable
private fun markingTypeStyle(code: String?, name: String?): Pair<ImageVector, Color> {
    val key = (code ?: name)?.trim()?.uppercase()
    return when {
        key == null -> Icons.Default.Category to MaterialTheme.colorScheme.secondary
        key.startsWith("P") -> Icons.Default.CheckCircle to OneAppSuccess
        key.startsWith("A") -> Icons.Default.Cancel to MaterialTheme.colorScheme.error
        key.startsWith("L") -> Icons.Default.Schedule to OneAppWarning
        key.startsWith("E") || key.startsWith("M") -> Icons.Default.MedicalServices to MaterialTheme.colorScheme.tertiary
        else -> Icons.Default.Category to MaterialTheme.colorScheme.secondary
    }
}
