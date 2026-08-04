package com.xsc.oneapp.feature.exam.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.ui.viewmodel.HallTicketViewModel
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordCard
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.ResponsiveContent
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors

/**
 * Restyled only. The ViewModel contract, the schedule id read from SavedStateHandle and
 * every retry path are unchanged.
 *
 * The seat number keeps its deliberately oversized treatment - it is the one value a
 * student opens this screen to read, often at arm's length in an exam hall.
 */
@Composable
fun HallTicketScreen(
    onBack: () -> Unit,
    viewModel: HallTicketViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecordScaffold(title = "Hall ticket", onBack = onBack) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val current = state) {
                is UiState.Loading -> LoadingState()
                is UiState.Success -> if (current.data.isEmpty()) {
                    EmptyState("Your hall ticket hasn't been generated for this exam yet.")
                } else {
                    HallTicketList(current.data)
                }
                is UiState.BusinessError -> ErrorState(current.message, viewModel::load)
                is UiState.NetworkError -> ErrorState(current.message, viewModel::load)
                is UiState.UnexpectedError -> ErrorState(current.message, viewModel::load)
            }
        }
    }
}

@Composable
private fun HallTicketList(tickets: List<HallTicket>) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        items(tickets, key = { it.id ?: it.hashCode() }) { ticket ->
            ResponsiveContent {
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
                            IconBadge(Icons.Default.ConfirmationNumber)
                            Text(
                                "Seat ${ticket.seatNumber ?: "—"}",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        ticket.status?.let { status ->
                            StatusPill(status, tint = statusColors.success)
                        }
                    }

                    ticket.venueDetails?.let { venue ->
                        Row(
                            modifier = Modifier.padding(top = spacing.lg),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                        ) {
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                venue,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
