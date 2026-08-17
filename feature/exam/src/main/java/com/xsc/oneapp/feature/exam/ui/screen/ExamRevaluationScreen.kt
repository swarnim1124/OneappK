package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.RateReview
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
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing

private val TABS = listOf("Requests", "Challenges")

/**
 * Revaluation requests and senior-panel challenges in one place - two stages of a
 * single "I want this mark reviewed" story (a challenge only exists after a regular
 * revaluation has completed, see [ChallengeRevaluation]'s doc comment), rather than two
 * unrelated destinations.
 *
 * View-only: the contract defines `revaluationRequest`/`challengeRevaluation` `add`
 * actions for filing a new request, but this module's data layer has only ever
 * implemented the `view` side (see ExamRepository) - filing one is a follow-up once
 * that write path exists, not a UI-only change.
 */
@Composable
fun ExamRevaluationScreen(
    onBack: () -> Unit,
    viewModel: ExamViewModel
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }

    val requestsState by viewModel.revaluationRequests.state.collectAsStateWithLifecycle()
    val challengesState by viewModel.challengeRevaluations.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.loadRevaluation() }

    RecordScaffold(title = "Revaluation & appeals", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SectionChips(options = TABS, selectedIndex = selected, onSelect = { selected = it })

            Box(modifier = Modifier.fillMaxSize()) {
                if (selected == 0) {
                    RequestsTab(requestsState, viewModel.revaluationRequests::reload)
                } else {
                    ChallengesTab(challengesState, viewModel.challengeRevaluations::reload)
                }
            }
        }
    }
}

@Composable
private fun RequestsTab(state: UiState<List<RevaluationRequest>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState(
                title = "No revaluation requests",
                message = "Requests you file for a re-check of a course mark appear here."
            )
        } else {
            val spacing = LocalSpacing.current
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                items(state.data, key = { it.id ?: it.hashCode() }) { request ->
                    ResponsiveContent { RequestCard(request) }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun ChallengesTab(state: UiState<List<ChallengeRevaluation>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState(
                title = "No challenges filed",
                message = "A senior-panel challenge against a completed revaluation appears here."
            )
        } else {
            val spacing = LocalSpacing.current
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(spacing.lg),
                verticalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                items(state.data, key = { it.id ?: it.hashCode() }) { challenge ->
                    ResponsiveContent { ChallengeCard(challenge) }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun RequestCard(request: RevaluationRequest) {
    val spacing = LocalSpacing.current

    RecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                IconBadge(Icons.Default.RateReview)
                Text(
                    "Course #${request.courseId ?: "—"}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            request.status?.let { StatusPill(it, tint = MaterialTheme.colorScheme.tertiary) }
        }

        request.reason?.takeIf { it.isNotBlank() }?.let { reason ->
            DetailText(reason, modifier = Modifier.padding(top = spacing.md))
        }
        request.createdAt?.let { createdAt ->
            DetailText("Filed $createdAt", modifier = Modifier.padding(top = spacing.xs))
        }
    }
}

@Composable
private fun ChallengeCard(challenge: ChallengeRevaluation) {
    val spacing = LocalSpacing.current

    RecordCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                IconBadge(Icons.Default.Gavel, tint = MaterialTheme.colorScheme.tertiary)
                Text(
                    "Course #${challenge.courseId ?: "—"}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            challenge.status?.let { StatusPill(it, tint = MaterialTheme.colorScheme.tertiary) }
        }

        challenge.reason?.takeIf { it.isNotBlank() }?.let { reason ->
            DetailText(reason, modifier = Modifier.padding(top = spacing.md))
        }
        challenge.revalRequestId?.let { requestId ->
            DetailText("Against request #$requestId", modifier = Modifier.padding(top = spacing.xs))
        }
        challenge.createdAt?.let { createdAt ->
            DetailText("Filed $createdAt", modifier = Modifier.padding(top = spacing.xs))
        }
    }
}
