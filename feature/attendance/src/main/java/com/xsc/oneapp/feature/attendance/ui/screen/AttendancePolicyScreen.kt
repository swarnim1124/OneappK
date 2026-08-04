package com.xsc.oneapp.feature.attendance.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceCard
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceListRow
import com.xsc.oneapp.feature.attendance.ui.components.AttendanceScaffold
import com.xsc.oneapp.feature.attendance.ui.components.DetailText
import com.xsc.oneapp.feature.attendance.ui.components.SectionChips
import com.xsc.oneapp.feature.attendance.ui.components.SectionList
import com.xsc.oneapp.feature.attendance.ui.components.StatusPill
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceViewModel

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

    AttendanceScaffold(title = "Policy & marking types", onBack = onBack) { padding ->
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
        AttendanceListRow(
            icon = Icons.Default.Policy,
            title = config.policyName ?: "Attendance policy",
            subtitle = config.minAttendancePercent?.let { "Minimum $it% required" },
            trailing = if (config.isActive == "true") {
                { StatusPill("Active") }
            } else null
        )

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
    AttendanceCard {
        AttendanceListRow(
            icon = Icons.Default.Category,
            title = type.lookupName ?: "Marking type",
            subtitle = type.lookupCode,
            iconTint = MaterialTheme.colorScheme.secondary,
            trailing = if (type.isActive == "true") {
                { StatusPill("Active") }
            } else null
        )
    }
}
