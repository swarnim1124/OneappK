package com.xsc.oneapp.feature.exam.ui.screen

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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.ui.viewmodel.HallTicketViewModel
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
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
 *
 * Deliberately does NOT include a download/share action, despite the design system
 * showing one: [HallTicket] carries no document/file reference to download or share,
 * and there's no PDF-generation or share-intent wiring anywhere in this module. A
 * button that looked like it saved or shared something without actually doing either
 * would be worse than no button - see the exam Stitch prompt's flagged gap for what
 * that would actually need (real backend support, not just a client-side control).
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
                is UiState.Success -> {
                    val page = current.data
                    val activeBlocks = page.blocks.filter { it.isActive() }
                    if (page.tickets.isEmpty() && activeBlocks.isEmpty()) {
                        EmptyState("Your hall ticket hasn't been generated for this exam yet.")
                    } else {
                        HallTicketList(page.tickets, activeBlocks)
                    }
                }
                is UiState.BusinessError -> ErrorState(current.message, viewModel::load)
                is UiState.NetworkError -> ErrorState(current.message, viewModel::load)
                is UiState.UnexpectedError -> ErrorState(
                    current.message,
                    viewModel::load,
                    context = (current.appError as? AppError.Traced)?.context
                )
            }
        }
    }
}

/** A block is "active" unless the row explicitly says it was lifted. The status field
 * is free text from the backend, so this treats only the known cleared spellings as
 * resolved and anything else (including a null status) as still blocking - a hold the
 * client doesn't recognise should be shown, not silently swallowed. */
private fun StudentExamBlock.isActive(): Boolean = when (status?.lowercase()) {
    "released", "cleared", "resolved", "revoked", "inactive", "deleted" -> false
    else -> true
}

@Composable
private fun HallTicketList(
    tickets: List<HallTicket>,
    activeBlocks: List<StudentExamBlock>
) {
    val spacing = LocalSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        // Shown above the ticket, not instead of it: a hold can exist alongside an
        // already-issued ticket, and in that case the student needs to see both.
        if (activeBlocks.isNotEmpty()) {
            item {
                ResponsiveContent {
                    ExamBlockCard(activeBlocks, hasTicket = tickets.isNotEmpty())
                }
            }
        }
        items(tickets, key = { it.id ?: it.hashCode() }) { ticket ->
            ResponsiveContent {
                HallTicketCard(ticket)
            }
        }
    }
}

/** m_exam §4.3 - why a hall ticket is being withheld. Without this the screen showed a
 * bare "not generated yet", which is indistinguishable from an administrative hold the
 * student could actually act on (clear fee arrears, resolve an attendance shortage). */
@Composable
private fun ExamBlockCard(blocks: List<StudentExamBlock>, hasTicket: Boolean) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current

    RecordCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.sm)
        ) {
            Icon(
                Icons.Default.Block,
                contentDescription = null,
                tint = statusColors.warning,
                modifier = Modifier.size(20.dp)
            )
            Text(
                if (hasTicket) "Examination hold on your record" else "Hall ticket withheld",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            if (hasTicket) {
                "Your ticket is issued, but the hold below may still stop you sitting the exam. " +
                    "Contact the examination office to clear it."
            } else {
                "Your hall ticket can't be issued until this is cleared. " +
                    "Contact the examination office for details."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = spacing.xs)
        )

        blocks.forEach { block ->
            Column(modifier = Modifier.padding(top = spacing.md)) {
                Text(
                    block.reason ?: "No reason recorded",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                block.blockedBy?.let { by ->
                    Text(
                        "Raised by $by",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HallTicketCard(ticket: HallTicket) {
    val spacing = LocalSpacing.current
    val statusColors = LocalStatusColors.current

    // RecordCard is shared across every feature module and always applies its own
    // internal padding - an edge-to-edge accent header (as the design system shows)
    // would need changing that shared component's contract for one screen, so this
    // uses an inset accent block instead of a full-bleed one.
    RecordCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primary)
                .padding(spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Hall Ticket",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            ticket.status?.let { status ->
                StatusPill(status, tint = statusColors.success)
            }
        }

        Column(
            modifier = Modifier.padding(top = spacing.lg),
            verticalArrangement = Arrangement.spacedBy(spacing.lg)
        ) {
            // The seat number keeps its oversized treatment - the one value read at
            // arm's length in an exam hall.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "SEAT NUMBER",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    ticket.seatNumber ?: "—",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }

            ticket.venueDetails?.let { venue ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(spacing.md),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(spacing.sm)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "Exam venue",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            venue,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
