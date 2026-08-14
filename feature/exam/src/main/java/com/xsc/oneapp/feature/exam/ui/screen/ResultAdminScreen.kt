package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.ui.state.ExamEffect
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.textfield.PremiumTextField
import com.xsc.sdk.theme.LocalSpacing
import kotlinx.coroutines.launch

/** m_exam §7 sm_results/sm_resultApproval - action-only surface: the contract has no
 * confirmed `view` response shape for resultApproval, so there is no list to render
 * here, only schedule-scoped writes with a toast as the only feedback (see each
 * section's own [LaunchedEffect] wiring below). Each section keeps its own Schedule ID
 * field rather than sharing one across sections, since generate/approve/publish are
 * independent actions an admin may run against different schedules in one visit. */
@Composable
fun ResultAdminScreen(onBack: () -> Unit, viewModel: ExamAdminViewModel) {
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is ExamEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    RecordScaffold(title = "Result administration", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            ResponsiveContent(modifier = Modifier.padding(spacing.lg)) {
                Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                    if (permissions.canGenerateResults) {
                        GenerateResultsSection(onGenerate = viewModel::generateResults)
                    }
                    if (permissions.canApproveResults) {
                        ApproveResultsSection(onApprove = viewModel::approveResults)
                    }
                    if (permissions.canPublishResults) {
                        PublishResultsSection(
                            onPublish = viewModel::publishResults,
                            onHold = viewModel::holdResultPublication,
                            onUnpublish = viewModel::unpublishResults
                        )
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }
}

@Composable
private fun GenerateResultsSection(onGenerate: (scheduleId: String) -> Unit) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }

    RecordCard {
        Text("Generate results", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        PremiumTextField(
            text = scheduleId,
            onTextChange = { scheduleId = it },
            placeholder = "Schedule ID",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            modifier = Modifier.padding(top = spacing.sm)
        )
        Button(
            onClick = { onGenerate(scheduleId) },
            enabled = scheduleId.isNotBlank(),
            modifier = Modifier.padding(top = spacing.sm)
        ) { Text("Generate") }
    }
}

@Composable
private fun ApproveResultsSection(onApprove: (scheduleId: String, level: String, remarks: String?) -> Unit) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }
    var level by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    RecordCard {
        Text("Approve results", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Column(
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.padding(top = spacing.sm)
        ) {
            PremiumTextField(text = scheduleId, onTextChange = { scheduleId = it }, placeholder = "Schedule ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
            PremiumTextField(text = level, onTextChange = { level = it }, placeholder = "Approval level", imeAction = ImeAction.Next)
            PremiumTextField(text = remarks, onTextChange = { remarks = it }, placeholder = "Remarks (optional)", imeAction = ImeAction.Done)
        }
        Button(
            onClick = { onApprove(scheduleId, level, remarks.ifBlank { null }) },
            enabled = scheduleId.isNotBlank() && level.isNotBlank(),
            modifier = Modifier.padding(top = spacing.sm)
        ) { Text("Approve") }
    }
}

@Composable
private fun PublishResultsSection(
    onPublish: (scheduleId: String) -> Unit,
    onHold: (scheduleId: String) -> Unit,
    onUnpublish: (scheduleId: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var scheduleId by remember { mutableStateOf("") }

    RecordCard {
        Text("Publish results", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        PremiumTextField(
            text = scheduleId,
            onTextChange = { scheduleId = it },
            placeholder = "Schedule ID",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            modifier = Modifier.padding(top = spacing.sm)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            modifier = Modifier.padding(top = spacing.sm)
        ) {
            Button(onClick = { onPublish(scheduleId) }, enabled = scheduleId.isNotBlank()) { Text("Publish") }
            TextButton(onClick = { onHold(scheduleId) }, enabled = scheduleId.isNotBlank()) { Text("Hold") }
            TextButton(onClick = { onUnpublish(scheduleId) }, enabled = scheduleId.isNotBlank()) { Text("Unpublish") }
        }
    }
}
