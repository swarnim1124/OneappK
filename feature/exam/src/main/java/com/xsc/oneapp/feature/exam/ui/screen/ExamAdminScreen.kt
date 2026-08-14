package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.theme.LocalSpacing

/** m_exam examination-administration hub - one tile per admin sub-module, reached only
 * from [ExamScreen]'s own admin quick-link, which itself only renders when
 * [ExamAdminViewModel.hasAnyExamAdminAccess] is true. Each tile here applies its own,
 * finer-grained gate on top of that coarse check - the same "show/enable only" contract
 * used across this admin surface: a session granted only exam-centre reads shouldn't
 * see a "Results" tile it has no permission to act on. A tile's gate is an OR across
 * every [ExamAdminPermissions] flag belonging to that sub-module, not just the "add"
 * flag - the tile is a door to a screen that may let the admin update/delete/publish
 * even without add rights. */
@Composable
fun ExamAdminScreen(
    onBack: () -> Unit,
    onOpenExamCentres: () -> Unit,
    onOpenSeating: () -> Unit,
    onOpenInvigilation: () -> Unit,
    onOpenHallTicketAdmin: () -> Unit,
    onOpenQuestionPaper: () -> Unit,
    onOpenExternalExam: () -> Unit,
    onOpenInternalExam: () -> Unit,
    onOpenMarksGrading: () -> Unit,
    onOpenEvaluation: () -> Unit,
    onOpenResultAdmin: () -> Unit,
    viewModel: ExamAdminViewModel
) {
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()
    val spacing = LocalSpacing.current

    val tiles = buildList {
        if (permissions.canAddExamCentre || permissions.canUpdateExamCentre || permissions.canDeleteExamCentre) {
            add(AdminTile("Exam centres", Icons.Default.Domain, onOpenExamCentres))
        }
        if (permissions.canAddSeatingPlan || permissions.canUpdateSeatingPlan || permissions.canDeleteSeatingPlan) {
            add(AdminTile("Seating", Icons.Default.EventSeat, onOpenSeating))
        }
        if (permissions.canAddInvigilatorAssignment || permissions.canUpdateInvigilatorAssignment ||
            permissions.canDeleteInvigilatorAssignment
        ) {
            add(AdminTile("Invigilation", Icons.Default.Visibility, onOpenInvigilation))
        }
        if (permissions.canGenerateHallTicket || permissions.canUpdateHallTicketAdmin ||
            permissions.canDeleteHallTicketAdmin || permissions.canPublishHallTickets ||
            permissions.canAddStudentExamBlock || permissions.canUpdateStudentExamBlock ||
            permissions.canDeleteStudentExamBlock
        ) {
            add(AdminTile("Hall tickets", Icons.Default.ConfirmationNumber, onOpenHallTicketAdmin))
        }
        if (permissions.canAddQuestionPaper || permissions.canUpdateQuestionPaper ||
            permissions.canDeleteQuestionPaper || permissions.canSubmitQuestionPaper ||
            permissions.canDeleteQuestionPaperSubmission || permissions.canApproveQuestionPaper ||
            permissions.canAddQuestionBankEntry || permissions.canUpdateQuestionBankEntry ||
            permissions.canDeleteQuestionBankEntry
        ) {
            add(AdminTile("Question papers", Icons.Default.Description, onOpenQuestionPaper))
        }
        if (permissions.canAddExternalExaminer || permissions.canUpdateExternalExaminer ||
            permissions.canDeleteExternalExaminer || permissions.canAddExternalPaperSetting ||
            permissions.canDeleteExternalPaperSetting || permissions.canAddExternalEvaluation ||
            permissions.canUpdateExternalEvaluation || permissions.canDeleteExternalEvaluation
        ) {
            add(AdminTile("External examiners", Icons.Default.People, onOpenExternalExam))
        }
        if (permissions.canAddInternalExam || permissions.canUpdateInternalExam ||
            permissions.canDeleteInternalExam || permissions.canSubmitInternalMarksEntry ||
            permissions.canConsolidateInternalMarks
        ) {
            add(AdminTile("Internal exams", Icons.Default.School, onOpenInternalExam))
        }
        if (permissions.canSubmitMarksEntry || permissions.canVerifyMarks ||
            permissions.canGenerateGrades || permissions.canLockGrades
        ) {
            add(AdminTile("Marks & grading", Icons.Default.Grade, onOpenMarksGrading))
        }
        if (permissions.canAddEvaluationBundle || permissions.canUpdateEvaluationBundle ||
            permissions.canDeleteEvaluationBundle || permissions.canRequestSecondValuation ||
            permissions.canApplyModeration
        ) {
            add(AdminTile("Evaluation", Icons.Default.FactCheck, onOpenEvaluation))
        }
        if (permissions.canGenerateResults || permissions.canApproveResults || permissions.canPublishResults) {
            add(AdminTile("Results", Icons.AutoMirrored.Filled.Assignment, onOpenResultAdmin))
        }
    }

    RecordScaffold(title = "Exam administration", onBack = onBack) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                ResponsiveContent(modifier = Modifier.padding(spacing.lg)) {
                    Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                        tiles.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                            ) {
                                row.forEach { tile ->
                                    RecordCard(modifier = Modifier.fillMaxWidth().weight(1f), onClick = tile.onClick) {
                                        Icon(
                                            tile.icon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            tile.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(top = spacing.xs)
                                        )
                                    }
                                }
                                if (row.size == 1) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AdminTile(val label: String, val icon: ImageVector, val onClick: () -> Unit)
