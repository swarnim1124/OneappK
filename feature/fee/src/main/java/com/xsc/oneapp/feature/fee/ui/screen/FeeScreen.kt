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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.AppError
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.BuildConfig
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder
import com.xsc.oneapp.feature.fee.ui.state.FeeEffect
import com.xsc.oneapp.feature.fee.ui.state.FeePermissions
import com.xsc.oneapp.feature.fee.ui.viewmodel.FeeViewModel
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.LoadingState
import com.xsc.sdk.commonui.record.NotYetAvailableScreen
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.textfield.PremiumTextField
// Aliased so the seven tab bodies below keep calling `FeeCard { ... }` unchanged - the
// implementation is now the shared record card rather than a private copy of it.
import com.xsc.sdk.commonui.record.RecordCard as FeeCard
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppSuccess
import kotlinx.coroutines.launch

private val TAB_TITLES = listOf("Dues", "Invoices", "Payments", "Concessions", "Refunds", "Penalties", "Structures")

/**
 * Every tab body, amount format, ViewModel call and retry path for the five read-only
 * tabs (Invoices/Payments/Penalties, and the read side of Dues/Concessions/
 * Structures/Refunds) is unchanged from the previous restyle pass. What's new: admin
 * write actions on Structures/Dues/Concessions/Refunds, each gated on
 * [FeePermissions] - the first real (non defense-in-depth) permission check in this
 * app's UI - and a real Razorpay checkout replacing the old honest
 * [PaymentNotYetAvailableScreen] stub for the online-payment path (see
 * [PayFeesReviewScreen]).
 */
@Composable
fun FeeScreen(
    onBack: () -> Unit,
    viewModel: FeeViewModel = hiltViewModel()
) {
    val spacing = LocalSpacing.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPayFlow by remember { mutableStateOf(false) }

    val assignmentsState by viewModel.assignmentsState.collectAsStateWithLifecycle()
    val invoicesState by viewModel.invoicesState.collectAsStateWithLifecycle()
    val paymentsState by viewModel.paymentsState.collectAsStateWithLifecycle()
    val concessionsState by viewModel.concessionsState.collectAsStateWithLifecycle()
    val refundsState by viewModel.refundsState.collectAsStateWithLifecycle()
    val penaltiesState by viewModel.penaltiesState.collectAsStateWithLifecycle()
    val structuresState by viewModel.structuresState.collectAsStateWithLifecycle()
    val permissions by viewModel.permissions.collectAsStateWithLifecycle()

    // Fetches only the visible tab, and only the first time it is opened. Replaces the
    // ViewModel's old `init {}` block, which requested all seven sub-modules at once.
    LaunchedEffect(selectedTab) { viewModel.onTabSelected(selectedTab) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            if (effect is FeeEffect.ShowToast) {
                scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    var showAddStructure by remember { mutableStateOf(false) }
    var deletingStructure by remember { mutableStateOf<FeeStructure?>(null) }
    var showAssignFee by remember { mutableStateOf(false) }
    var deletingAssignment by remember { mutableStateOf<FeeAssignment?>(null) }
    var showGrantConcession by remember { mutableStateOf(false) }

    if (showPayFlow) {
        PayFeesReviewScreen(
            assignmentsState = assignmentsState,
            onBack = { showPayFlow = false },
            initiateOnlinePayment = viewModel::initiateOnlinePayment,
            onOnlinePaymentSubmitted = viewModel::onOnlinePaymentSubmitted
        )
        return
    }

    RecordScaffold(
        title = "Fees",
        onBack = onBack,
        floatingActionButton = {
            when (selectedTab) {
                0 -> if (permissions.canAssignFee) {
                    ExtendedFloatingActionButton(
                        text = { Text("Assign fee") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAssignFee = true }
                    )
                }
                3 -> if (permissions.canGrantConcession) {
                    ExtendedFloatingActionButton(
                        text = { Text("Grant concession") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showGrantConcession = true }
                    )
                }
                6 -> if (permissions.canAddFeeStructure) {
                    ExtendedFloatingActionButton(
                        text = { Text("Add structure") },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        onClick = { showAddStructure = true }
                    )
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SectionChips(
                options = TAB_TITLES,
                selectedIndex = selectedTab,
                onSelect = { selectedTab = it }
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> AssignmentsTab(
                        assignmentsState,
                        viewModel::loadAssignments,
                        onPayFees = { showPayFlow = true },
                        canDelete = permissions.canDeleteFeeAssignment,
                        onDelete = { deletingAssignment = it }
                    )
                    1 -> InvoicesTab(invoicesState, viewModel::loadInvoices)
                    2 -> PaymentsTab(paymentsState, viewModel::loadPayments)
                    3 -> ConcessionsTab(concessionsState, viewModel::loadConcessions)
                    4 -> RefundsTab(
                        refundsState,
                        viewModel::loadRefunds,
                        canApprove = permissions.canApproveRefund,
                        onApprove = { refundId, status -> viewModel.updateRefundStatus(refundId, status, null) },
                        onRequestRefund = { invoiceId, amount, reason -> viewModel.requestRefund(invoiceId, amount, reason) }
                    )
                    5 -> PenaltiesTab(penaltiesState, viewModel::loadPenalties)
                    6 -> StructuresTab(
                        structuresState,
                        viewModel::loadStructures,
                        canUpdate = permissions.canUpdateFeeStructure,
                        canDelete = permissions.canDeleteFeeStructure,
                        onToggleActive = { structureId, activate ->
                            viewModel.updateFeeStructureStatus(structureId, if (activate) "ACTIVE" else "INACTIVE")
                        },
                        onDelete = { deletingStructure = it }
                    )
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.padding(padding)) { data ->
            Snackbar(snackbarData = data)
        }
    }

    if (showAddStructure) {
        CreateFeeStructureDialog(
            onDismiss = { showAddStructure = false },
            onSubmit = { code, name, academicYearId, programmeId, component, effectiveFrom, effectiveTo ->
                viewModel.createFeeStructure(code, name, academicYearId, programmeId, listOf(component), effectiveFrom, effectiveTo)
                showAddStructure = false
            }
        )
    }

    deletingStructure?.let { structure ->
        DeactivateConfirmDialog(
            title = "Deactivate ${structure.name ?: "this fee structure"}?",
            message = "Students already assigned this structure keep their existing dues - this only stops it being assigned to anyone new.",
            onDismiss = { deletingStructure = null },
            onConfirm = {
                structure.id?.let { viewModel.deleteFeeStructure(it) }
                deletingStructure = null
            }
        )
    }

    if (showAssignFee) {
        AssignFeeDialog(
            onDismiss = { showAssignFee = false },
            onSubmit = { feeStructureId, termId, dueDate, studentIds ->
                viewModel.assignFee(feeStructureId, termId, dueDate, studentIds)
                showAssignFee = false
            }
        )
    }

    deletingAssignment?.let { assignment ->
        DeactivateConfirmDialog(
            title = "Deactivate this fee assignment?",
            message = "This removes the due from the student's outstanding balance.",
            onDismiss = { deletingAssignment = null },
            onConfirm = {
                assignment.id?.let { viewModel.deleteFeeAssignment(it) }
                deletingAssignment = null
            }
        )
    }

    if (showGrantConcession) {
        GrantConcessionDialog(
            onDismiss = { showGrantConcession = false },
            onSubmit = { studentId, assignmentId, concessionType, amountOrPercent, reason ->
                viewModel.grantConcession(studentId, assignmentId, concessionType, amountOrPercent, reason)
                showGrantConcession = false
            }
        )
    }
}

private fun formatAmount(amount: String?): String = amount?.let { "₹$it" } ?: "—"

// --- Dues / Assignments ---

@Composable
private fun AssignmentsTab(
    state: UiState<List<FeeAssignment>>,
    onRetry: () -> Unit,
    onPayFees: () -> Unit,
    canDelete: Boolean,
    onDelete: (FeeAssignment) -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No fee dues assigned yet.")
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Impossible-to-miss entry point into the pay flow, per the design
                // system - a student's actual reason for opening this tab.
                Button(
                    onClick = onPayFees,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Pay Fees", style = MaterialTheme.typography.titleMedium)
                }
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(state.data, key = { it.id ?: it.hashCode() }) { item ->
                        FeeCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    IconBadge(Icons.Default.RequestQuote)
                                    Column {
                                        Text(formatAmount(item.totalAmount), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                        if (item.dueDate != null) {
                                            Text("Due ${item.dueDate}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                                if (canDelete && item.id != null) {
                                    IconButton(onClick = { onDelete(item) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Deactivate assignment", tint = MaterialTheme.colorScheme.error)
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
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            context = (state.appError as? AppError.Traced)?.context
        )
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
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
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
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            context = (state.appError as? AppError.Traced)?.context
        )
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
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
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
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            context = (state.appError as? AppError.Traced)?.context
        )
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
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
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
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            context = (state.appError as? AppError.Traced)?.context
        )
    }
}

// --- Refunds ---

@Composable
private fun RefundsTab(
    state: UiState<List<FeeRefund>>,
    onRetry: () -> Unit,
    canApprove: Boolean,
    onApprove: (refundId: String, status: String) -> Unit,
    onRequestRefund: (invoiceId: String, amount: Double, reason: String) -> Unit
) {
    var showRequestRefund by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { showRequestRefund = true },
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
            Text("Request refund", style = MaterialTheme.typography.titleMedium)
        }

        when (state) {
            is UiState.Loading -> LoadingState()
            is UiState.Success -> if (state.data.isEmpty()) {
                EmptyState("No refunds on record.")
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(state.data, key = { it.id ?: it.hashCode() }) { item ->
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
                            // Only offered while a refund is still pending - once
                            // statusId already reflects an approved/rejected outcome,
                            // re-showing these would imply the decision can be redone.
                            if (canApprove && item.id != null && item.statusId == null) {
                                Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(onClick = { onApprove(item.id, "APPROVED") }) { Text("Approve") }
                                    TextButton(onClick = { onApprove(item.id, "REJECTED") }) {
                                        Text("Reject", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is UiState.BusinessError -> ErrorState(state.message, onRetry)
            is UiState.NetworkError -> ErrorState(state.message, onRetry)
            is UiState.UnexpectedError -> ErrorState(
                state.message,
                onRetry,
                context = (state.appError as? AppError.Traced)?.context
            )
        }
    }

    if (showRequestRefund) {
        RequestRefundDialog(
            onDismiss = { showRequestRefund = false },
            onSubmit = { invoiceId, amount, reason ->
                onRequestRefund(invoiceId, amount, reason)
                showRequestRefund = false
            }
        )
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
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
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
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            context = (state.appError as? AppError.Traced)?.context
        )
    }
}

// --- Structures ---

@Composable
private fun StructuresTab(
    state: UiState<List<FeeStructure>>,
    onRetry: () -> Unit,
    canUpdate: Boolean,
    canDelete: Boolean,
    onToggleActive: (structureId: String, activate: Boolean) -> Unit,
    onDelete: (FeeStructure) -> Unit
) {
    when (state) {
        is UiState.Loading -> LoadingState()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No fee structures published yet.")
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
                    FeeCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                            if (canDelete && item.id != null) {
                                IconButton(onClick = { onDelete(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deactivate structure", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        if (item.effectiveFrom != null) {
                            val range = item.effectiveTo?.let { "${item.effectiveFrom} – $it" } ?: "From ${item.effectiveFrom}"
                            Text(range, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                        }
                        if (canUpdate && item.id != null) {
                            val isActive = item.isActive?.equals("true", ignoreCase = true) != false
                            TextButton(
                                onClick = { onToggleActive(item.id, !isActive) },
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(if (isActive) "Mark inactive" else "Mark active")
                            }
                        }
                    }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(
            state.message,
            onRetry,
            context = (state.appError as? AppError.Traced)?.context
        )
    }
}

// --- Admin dialogs: Fee Structure ---

/** m_fees contract §3.1: feeStructure/add. Simplified to a single component per
 * structure rather than a dynamic multi-row list builder - the contract allows
 * several, but most institution fee structures in practice start as one line item
 * (e.g. "Tuition") and this keeps the form honest about what it actually submits. */
@Composable
private fun CreateFeeStructureDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        code: String,
        name: String,
        academicYearId: String,
        programmeId: String,
        component: FeeStructureComponent,
        effectiveFrom: String,
        effectiveTo: String?
    ) -> Unit
) {
    val spacing = LocalSpacing.current
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var academicYearId by remember { mutableStateOf("") }
    var programmeId by remember { mutableStateOf("") }
    var componentName by remember { mutableStateOf("") }
    var componentAmount by remember { mutableStateOf("") }
    var effectiveFrom by remember { mutableStateOf("") }
    var effectiveTo by remember { mutableStateOf("") }

    val amount = componentAmount.toDoubleOrNull()
    val canSubmit = code.isNotBlank() && name.isNotBlank() && academicYearId.isNotBlank() &&
        programmeId.isNotBlank() && componentName.isNotBlank() && amount != null && effectiveFrom.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Add fee structure", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = code, onTextChange = { code = it }, placeholder = "Structure code (e.g. FS-2026-ENG)", imeAction = ImeAction.Next)
                PremiumTextField(text = name, onTextChange = { name = it }, placeholder = "Structure name", imeAction = ImeAction.Next)
                PremiumTextField(text = academicYearId, onTextChange = { academicYearId = it }, placeholder = "Academic year ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = programmeId, onTextChange = { programmeId = it }, placeholder = "Programme ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = componentName, onTextChange = { componentName = it }, placeholder = "Component name (e.g. Tuition Fee)", imeAction = ImeAction.Next)
                PremiumTextField(text = componentAmount, onTextChange = { componentAmount = it }, placeholder = "Component amount", keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                PremiumTextField(text = effectiveFrom, onTextChange = { effectiveFrom = it }, placeholder = "Effective from (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = effectiveTo, onTextChange = { effectiveTo = it }, placeholder = "Effective to (optional)", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = {
                    val component = FeeStructureComponent(
                        headCode = componentName.trim().uppercase().take(10),
                        headName = componentName.trim(),
                        amount = amount ?: 0.0,
                        isOptional = false
                    )
                    onSubmit(code, name, academicYearId, programmeId, component, effectiveFrom, effectiveTo.ifBlank { null })
                }
            ) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

/** Shared deactivate-confirmation shape for both Fee Structure and Fee Assignment -
 * both actions deactivate rather than hard-delete, per the contract's own response
 * messages ("Deactivated successfully" / "Deactivated assessment"). */
@Composable
private fun DeactivateConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Deactivate", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Admin dialog: Fee Assignment ---

/** m_fees contract §3.2: feeAssignment/add. [studentIds] accepts a comma-separated
 * list so one admin action can assign the same structure to several students at
 * once, same shorthand pattern used for course-id lists elsewhere in this app. */
@Composable
private fun AssignFeeDialog(
    onDismiss: () -> Unit,
    onSubmit: (feeStructureId: String, termId: String, dueDate: String, studentIds: List<String>) -> Unit
) {
    val spacing = LocalSpacing.current
    var feeStructureId by remember { mutableStateOf("") }
    var termId by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var studentIdsText by remember { mutableStateOf("") }

    val studentIds = studentIdsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val canSubmit = feeStructureId.isNotBlank() && termId.isNotBlank() && dueDate.isNotBlank() && studentIds.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Assign fee", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = feeStructureId, onTextChange = { feeStructureId = it }, placeholder = "Fee structure ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = termId, onTextChange = { termId = it }, placeholder = "Term ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = dueDate, onTextChange = { dueDate = it }, placeholder = "Due date (YYYY-MM-DD)", imeAction = ImeAction.Next)
                PremiumTextField(text = studentIdsText, onTextChange = { studentIdsText = it }, placeholder = "Student ID(s), comma-separated", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(enabled = canSubmit, onClick = { onSubmit(feeStructureId, termId, dueDate, studentIds) }) { Text("Assign") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Admin dialog: Fee Concession ---

/** m_fees contract §3.3: feeConcession/add. */
@Composable
private fun GrantConcessionDialog(
    onDismiss: () -> Unit,
    onSubmit: (studentId: String, assignmentId: String, concessionType: String, amountOrPercent: Double, reason: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var studentId by remember { mutableStateOf("") }
    var assignmentId by remember { mutableStateOf("") }
    var concessionType by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull()
    val canSubmit = studentId.isNotBlank() && assignmentId.isNotBlank() && concessionType.isNotBlank() && amount != null && reason.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Grant concession", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = studentId, onTextChange = { studentId = it }, placeholder = "Student ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = assignmentId, onTextChange = { assignmentId = it }, placeholder = "Fee assignment ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = concessionType, onTextChange = { concessionType = it }, placeholder = "Concession type (e.g. MERIT_SCHOLARSHIP)", imeAction = ImeAction.Next)
                PremiumTextField(text = amountText, onTextChange = { amountText = it }, placeholder = "Amount or percent", keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                PremiumTextField(text = reason, onTextChange = { reason = it }, placeholder = "Reason", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSubmit,
                onClick = { onSubmit(studentId, assignmentId, concessionType, amount ?: 0.0, reason) }
            ) { Text("Grant") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Student dialog: Refund request ---

/** m_fees contract §3.6: feeRefund/add - studentId is resolved server-side from the
 * session, so this form only ever asks for what the student themselves knows. */
@Composable
private fun RequestRefundDialog(
    onDismiss: () -> Unit,
    onSubmit: (invoiceId: String, amount: Double, reason: String) -> Unit
) {
    val spacing = LocalSpacing.current
    var invoiceId by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull()
    val canSubmit = invoiceId.isNotBlank() && amount != null && reason.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Request refund", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.md)) {
                PremiumTextField(text = invoiceId, onTextChange = { invoiceId = it }, placeholder = "Invoice ID", keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                PremiumTextField(text = amountText, onTextChange = { amountText = it }, placeholder = "Amount", keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                PremiumTextField(text = reason, onTextChange = { reason = it }, placeholder = "Reason", imeAction = ImeAction.Done)
            }
        },
        confirmButton = {
            TextButton(enabled = canSubmit, onClick = { onSubmit(invoiceId, amount ?: 0.0, reason) }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// --- Pay Fees flow ---

/**
 * Select which dues to pay and see a real total, built entirely from
 * [FeeAssignment.totalAmount] - no fabricated fields. "Continue to Payment Method"
 * now creates a real Razorpay (test-mode) order and opens checkout - see
 * [PaymentMethodScreen].
 */
@Composable
private fun PayFeesReviewScreen(
    assignmentsState: UiState<List<FeeAssignment>>,
    onBack: () -> Unit,
    initiateOnlinePayment: suspend (invoiceId: String, amount: Double) -> RazorpayOrder,
    onOnlinePaymentSubmitted: () -> Unit
) {
    var showPaymentMethod by remember { mutableStateOf(false) }
    var pendingTotal by remember { mutableStateOf(0.0) }
    var pendingInvoiceId by remember { mutableStateOf<String?>(null) }

    if (showPaymentMethod) {
        PaymentMethodScreen(
            invoiceId = pendingInvoiceId,
            amount = pendingTotal,
            onBack = { showPaymentMethod = false },
            initiateOnlinePayment = initiateOnlinePayment,
            onPaymentSubmitted = {
                onOnlinePaymentSubmitted()
                showPaymentMethod = false
                onBack()
            }
        )
        return
    }

    RecordScaffold(title = "Select payments", onBack = onBack) { padding ->
        when (assignmentsState) {
            is UiState.Success -> {
                val assignments = assignmentsState.data
                var selectedIds by remember(assignments) {
                    mutableStateOf(assignments.mapNotNull { it.id }.toSet())
                }
                val selectedTotal = assignments
                    .filter { it.id in selectedIds }
                    .sumOf { it.totalAmount?.toDoubleOrNull() ?: 0.0 }

                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(assignments, key = { it.id ?: it.hashCode() }) { item ->
                            val checked = item.id != null && item.id in selectedIds
                            FeeCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { isChecked ->
                                                val id = item.id ?: return@Checkbox
                                                selectedIds = if (isChecked) selectedIds + id else selectedIds - id
                                            },
                                            enabled = item.id != null
                                        )
                                        Column {
                                            Text(formatAmount(item.totalAmount), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                            if (item.dueDate != null) {
                                                Text("Due ${item.dueDate}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Payment Summary
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total to pay", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "₹${"%.2f".format(selectedTotal)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = {
                                pendingTotal = selectedTotal
                                // A payment posts against one invoice; the first
                                // selected assignment's id stands in until the
                                // contract documents how a multi-assignment payment
                                // maps to a single invoiceId.
                                pendingInvoiceId = selectedIds.firstOrNull()
                                showPaymentMethod = true
                            },
                            enabled = selectedIds.isNotEmpty(),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        ) {
                            Text("Continue to Payment Method", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
            is UiState.Loading -> LoadingState()
            is UiState.BusinessError -> ErrorState(message = assignmentsState.message, onRetry = {})
            is UiState.NetworkError -> ErrorState(message = assignmentsState.message, onRetry = {})
            is UiState.UnexpectedError -> ErrorState(
                message = assignmentsState.message,
                onRetry = {},
                context = (assignmentsState.appError as? AppError.Traced)?.context
            )
        }
    }
}

/**
 * Creates a Razorpay (test-mode) order and opens checkout, or falls back to the
 * honest [PaymentNotYetAvailableScreen] when `RAZORPAY_KEY_ID` isn't configured -
 * see feature/fee/build.gradle.kts for where that value comes from. Checkout result
 * itself arrives asynchronously via [com.xsc.oneapp.core.payment.RazorpayPaymentBridge]
 * (Razorpay's SDK requires the *hosting Activity* to receive it, not this
 * Composable) - see [RazorpayCheckoutLauncher].
 */
@Composable
private fun PaymentMethodScreen(
    invoiceId: String?,
    amount: Double,
    onBack: () -> Unit,
    initiateOnlinePayment: suspend (invoiceId: String, amount: Double) -> RazorpayOrder,
    onPaymentSubmitted: () -> Unit
) {
    if (BuildConfig.RAZORPAY_KEY_ID.isBlank() || invoiceId == null) {
        PaymentNotYetAvailableScreen(onBack = onBack)
        return
    }

    RazorpayCheckoutLauncher(
        invoiceId = invoiceId,
        amount = amount,
        onBack = onBack,
        initiateOnlinePayment = initiateOnlinePayment,
        onPaymentSubmitted = onPaymentSubmitted
    )
}

/**
 * Honest terminal state for the pay flow. There is no payment gateway or
 * submit-payment use case anywhere in this module's domain layer - a "Payment
 * Successful" screen here would tell a student money moved when it didn't. This is
 * what exists instead until a real gateway is integrated.
 */
@Composable
private fun PaymentNotYetAvailableScreen(onBack: () -> Unit) {
    NotYetAvailableScreen(
        title = "Payment method",
        icon = Icons.Default.Payments,
        headline = "Online payment isn't available yet",
        message = "This institution hasn't enabled online fee payment in the app yet. Please contact your institution's finance office to complete this payment.",
        onBack = onBack
    )
}
