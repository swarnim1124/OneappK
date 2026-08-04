@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.fee.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.ui.viewmodel.FeeViewModel
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.SectionChips
// Aliased so the seven tab bodies below keep calling `FeeCard { ... }` unchanged - the
// implementation is now the shared record card rather than a private copy of it.
import com.xsc.sdk.commonui.record.RecordCard as FeeCard
import com.xsc.sdk.theme.OneAppSuccess

private val TAB_TITLES = listOf("Dues", "Invoices", "Payments", "Concessions", "Refunds", "Penalties", "Structures")

/**
 * Restyled only. Every tab body, amount format, ViewModel call and retry path below is
 * unchanged - what changed is that this screen no longer carries private copies of
 * LoadingState / MessageState / EmptyState / IconBadge / FeeCard, and the seven-tab
 * ScrollableTabRow (which overflowed the width on every phone, leaving "Structures"
 * permanently off-screen) is now scrollable chips.
 */
@Composable
fun FeeScreen(
    onBack: () -> Unit,
    viewModel: FeeViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val assignmentsState by viewModel.assignmentsState.collectAsStateWithLifecycle()
    val invoicesState by viewModel.invoicesState.collectAsStateWithLifecycle()
    val paymentsState by viewModel.paymentsState.collectAsStateWithLifecycle()
    val concessionsState by viewModel.concessionsState.collectAsStateWithLifecycle()
    val refundsState by viewModel.refundsState.collectAsStateWithLifecycle()
    val penaltiesState by viewModel.penaltiesState.collectAsStateWithLifecycle()
    val structuresState by viewModel.structuresState.collectAsStateWithLifecycle()

    // Fetches only the visible tab, and only the first time it is opened. Replaces the
    // ViewModel's old `init {}` block, which requested all seven sub-modules at once.
    LaunchedEffect(selectedTab) { viewModel.onTabSelected(selectedTab) }

    RecordScaffold(title = "Fees", onBack = onBack) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(
                options = TAB_TITLES,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> AssignmentsTab(assignmentsState, viewModel::loadAssignments)
                    1 -> InvoicesTab(invoicesState, viewModel::loadInvoices)
                    2 -> PaymentsTab(paymentsState, viewModel::loadPayments)
                    3 -> ConcessionsTab(concessionsState, viewModel::loadConcessions)
                    4 -> RefundsTab(refundsState, viewModel::loadRefunds)
                    5 -> PenaltiesTab(penaltiesState, viewModel::loadPenalties)
                    6 -> StructuresTab(structuresState, viewModel::loadStructures)
                }
            }
        }
    }
}

private fun formatAmount(amount: String?): String = amount?.let { "₹$it" } ?: "—"

// --- Dues / Assignments ---

@Composable
private fun AssignmentsTab(state: UiState<List<FeeAssignment>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No fee dues assigned yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { item ->
                    FeeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.RequestQuote)
                            Column {
                                Text(formatAmount(item.totalAmount), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (item.dueDate != null) {
                                    Text("Due ${item.dueDate}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (item.statusId != null) {
                            Text(
                                "Status ${item.statusId}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Invoices ---

@Composable
private fun InvoicesTab(state: UiState<List<FeeInvoice>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No transaction history yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { item ->
                    FeeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.ReceiptLong)
                            Column {
                                Text(formatAmount(item.amount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (item.transactionDate != null) {
                                    Text(item.transactionDate, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (item.description != null) {
                            Text(item.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Payments ---

@Composable
private fun PaymentsTab(state: UiState<List<FeePayment>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No payments recorded yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { item ->
                    FeeCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.CurrencyRupee, tint = OneAppSuccess)
                                Column {
                                    Text(formatAmount(item.amount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    if (item.paymentDate != null) {
                                        Text(item.paymentDate, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (item.paymentMode != null) {
                                Text(
                                    item.paymentMode,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OneAppSuccess,
                                    modifier = Modifier
                                        .background(OneAppSuccess.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        if (item.transactionReference != null) {
                            Text(
                                "Ref: ${item.transactionReference}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 10.dp)
                            )
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Concessions ---

@Composable
private fun ConcessionsTab(state: UiState<List<FeeConcession>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No fee concessions on record.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { item ->
                    FeeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.Discount)
                            Column {
                                val amountLabel = item.amount?.let { formatAmount(it) } ?: item.percentage?.let { "$it%" } ?: "—"
                                Text(amountLabel, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (item.approvedOn != null) {
                                    Text("Approved ${item.approvedOn}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (item.reason != null) {
                            Text(item.reason, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Refunds ---

@Composable
private fun RefundsTab(state: UiState<List<FeeRefund>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No refunds on record.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { item ->
                    FeeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.Replay)
                            Column {
                                Text(formatAmount(item.amount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (item.refundDate != null) {
                                    Text(item.refundDate, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (item.reason != null) {
                            Text(item.reason, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Penalties ---

@Composable
private fun PenaltiesTab(state: UiState<List<FeePenalty>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No late-payment penalties on record.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { item ->
                    FeeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.Gavel, tint = MaterialTheme.colorScheme.error)
                            Column {
                                Text(formatAmount(item.amount), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                if (item.appliedDate != null) {
                                    Text("Applied ${item.appliedDate}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        if (item.reason != null) {
                            Text(item.reason, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

// --- Structures ---

@Composable
private fun StructuresTab(state: UiState<List<FeeStructure>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No fee structures published yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.hashCode() }) { item ->
                    FeeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.Default.Rule)
                            Column {
                                Text(item.name ?: "—", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                if (item.code != null) {
                                    Text(
                                        item.code,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (item.effectiveFrom != null) {
                            val range = item.effectiveTo?.let { "${item.effectiveFrom} – $it" } ?: "From ${item.effectiveFrom}"
                            Text(range, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}
