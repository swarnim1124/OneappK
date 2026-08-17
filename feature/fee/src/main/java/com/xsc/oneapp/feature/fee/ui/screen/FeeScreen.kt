@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.fee.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Rule
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.dataOrNull
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.payment.RazorpayCheckoutContract
import com.xsc.oneapp.feature.fee.ui.viewmodel.DEFAULT_REFUND_TYPES
import com.xsc.oneapp.feature.fee.ui.viewmodel.FeeEffect
import com.xsc.oneapp.feature.fee.ui.viewmodel.FeeViewModel
import com.xsc.oneapp.feature.fee.ui.viewmodel.PaymentAmountValidation
import com.xsc.oneapp.feature.fee.ui.viewmodel.validatePaymentAmount
import com.xsc.sdk.commonui.button.PrimaryButton
import com.xsc.sdk.commonui.record.EmptyState
import com.xsc.sdk.commonui.record.ErrorState
import com.xsc.sdk.commonui.record.IconBadge
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.DetailText
import com.xsc.sdk.commonui.record.SectionChips
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.commonui.textfield.PremiumTextField
// Aliased so the seven tab bodies below keep calling `FeeCard { ... }` unchanged - the
// implementation is now the shared record card rather than a private copy of it.
import com.xsc.sdk.commonui.record.RecordCard as FeeCard
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalStatusColors

private val TAB_TITLES = listOf("Dues", "Invoices", "Payments", "Concessions", "Refunds", "Penalties", "Structures")

/**
 * Fee amount/method the user has chosen but not yet confirmed. Both "Pay the full
 * balance" and "Pay a different amount" (once validated) resolve to one of these and
 * hand it to [PaymentConfirmationDialog] - there is exactly one path from "an amount
 * was chosen" to "the gateway order is created", whether the amount is the full
 * balance, a partial figure, or routed through UPI-intent.
 */
private data class PendingPayment(
    val amount: Double,
    val methodLabel: String,
    val useUpiIntent: Boolean
)

/**
 * The outstanding-balance card and payment actions now live above the tab chips as a
 * persistent header - visible regardless of which tab is selected - rather than
 * buried inside the "Invoices" tab where a fabricated statement-as-invoice list used
 * to live. Every tab body below renders only what the backend actually returned: no
 * invented invoice numbers, statuses or dates.
 */
@Composable
fun FeeScreen(
    onBack: () -> Unit,
    viewModel: FeeViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAmountEntry by remember { mutableStateOf(false) }
    var pendingPayment by remember { mutableStateOf<PendingPayment?>(null) }

    val assignmentsState by viewModel.assignmentsState.collectAsStateWithLifecycle()
    val statementState by viewModel.statementState.collectAsStateWithLifecycle()
    val paymentsState by viewModel.paymentsState.collectAsStateWithLifecycle()
    val concessionsState by viewModel.concessionsState.collectAsStateWithLifecycle()
    val refundsState by viewModel.refundsState.collectAsStateWithLifecycle()
    val penaltiesState by viewModel.penaltiesState.collectAsStateWithLifecycle()
    val structuresState by viewModel.structuresState.collectAsStateWithLifecycle()
    val paymentInFlight by viewModel.paymentInFlight.collectAsStateWithLifecycle()
    val refundRequestInFlight by viewModel.refundRequestInFlight.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Razorpay reports its outcome as an activity result rather than a callback the
    // ViewModel could own, so the launcher lives here and the result is handed
    // straight back down. See RazorpayCheckoutContract.
    val checkoutLauncher = rememberLauncherForActivityResult(
        contract = RazorpayCheckoutContract(),
        onResult = viewModel::onCheckoutResult
    )

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is FeeEffect.LaunchCheckout -> checkoutLauncher.launch(effect.request)
                is FeeEffect.Notify -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // The summary card is visible on every tab, so it loads once up front rather than
    // waiting for tab 1 to be selected. Fetches only the visible tab's own data, and
    // only the first time it is opened.
    LaunchedEffect(Unit) { viewModel.loadSummary() }
    LaunchedEffect(selectedTab) { viewModel.onTabSelected(selectedTab) }

    // fee_structure_id -> a real published fee name, so the Dues tab can show
    // "Tuition Fee - Term 1" instead of a generic placeholder. Falls back to nothing
    // (never a made-up name) when the structure hasn't loaded or has no match.
    val structureNames = remember(structuresState) {
        structuresState.dataOrNull().orEmpty().associate { it.id to it.name }
    }

    val outstanding = statementState.dataOrNull()?.payableAmount ?: 0.0

    RecordScaffold(title = "Fees", onBack = onBack) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                FeeSummarySection(
                    state = statementState,
                    paymentInFlight = paymentInFlight,
                    onRetry = viewModel::loadStatement,
                    onPayFull = { payable ->
                        pendingPayment = PendingPayment(payable, "Card / UPI / Netbanking", useUpiIntent = false)
                    },
                    onPayDifferentAmount = { showAmountEntry = true },
                    onPayWithUpi = { payable ->
                        pendingPayment = PendingPayment(payable, "UPI app on this phone", useUpiIntent = true)
                    }
                )
                SectionChips(
                    options = TAB_TITLES,
                    selectedIndex = selectedTab,
                    onSelect = { selectedTab = it }
                )
                // `weight(1f)` (not a bare `fillMaxSize()`) is required here: this Box
                // is the third, non-weighted child of the Column above. Without a
                // weight, fillMaxSize() asked it to be as tall as the *entire* column's
                // incoming constraint - on top of the space FeeSummarySection and
                // SectionChips already used - which pushed every tab's content
                // (and, on the Refunds tab, its own button/chip row above the list)
                // taller than the visible viewport and off the bottom of the screen.
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        0 -> DuesTab(assignmentsState, structureNames, viewModel::loadAssignments)
                        1 -> InvoicesTab(statementState, viewModel::loadStatement)
                        2 -> PaymentsTab(paymentsState, viewModel::loadPayments)
                        3 -> ConcessionsTab(concessionsState, viewModel::loadConcessions)
                        4 -> RefundsTab(
                            state = refundsState,
                            payments = paymentsState.dataOrNull().orEmpty(),
                            requestInFlight = refundRequestInFlight,
                            onRetry = viewModel::loadRefunds,
                            onRequestRefund = viewModel::requestRefund
                        )
                        5 -> PenaltiesTab(penaltiesState, viewModel::loadPenalties)
                        6 -> StructuresTab(structuresState, viewModel::loadStructures)
                    }
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showAmountEntry) {
        AmountEntryDialog(
            outstanding = outstanding,
            onDismiss = { showAmountEntry = false },
            onContinue = { amount ->
                showAmountEntry = false
                pendingPayment = PendingPayment(amount, "Card / UPI / Netbanking", useUpiIntent = false)
            }
        )
    }

    pendingPayment?.let { pending ->
        PaymentConfirmationDialog(
            amount = pending.amount,
            outstanding = outstanding,
            methodLabel = pending.methodLabel,
            paymentInFlight = paymentInFlight,
            onDismiss = { pendingPayment = null },
            onConfirm = {
                viewModel.startOnlinePayment(amount = pending.amount, useUpiIntent = pending.useUpiIntent)
                pendingPayment = null
            }
        )
    }
}

private fun formatAmount(amount: String?): String = amount?.let { raw ->
    raw.trim().toDoubleOrNull()?.let { value -> formatAmount(value) } ?: "₹$raw"
} ?: "—"

/** Grouped, two-decimal rupees. The statement totals arrive as raw doubles, and
 * "₹40000.0" next to "₹10000.0" is genuinely hard to compare at a glance. */
private fun formatAmount(amount: Double): String = "₹%,.2f".format(amount)

/** A soft, shared pulse used for every tab's loading placeholder - one loading
 * surface reads as "your data is coming", where five independently blinking rows or
 * a bare "Loading..." caption reads as broken. */
@Composable
private fun rememberSkeletonColor(): androidx.compose.ui.graphics.Color {
    val transition = rememberInfiniteTransition(label = "feeSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "feeSkeletonAlpha"
    )
    return MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
}

/** Five pulsing placeholder rows, shaped like a real [FeeCard] row - reused by every
 * list tab (Dues/Invoices/Payments/Concessions/Refunds/Penalties/Structures) so the
 * loading state always looks like "this screen" rather than a generic spinner. */
@Composable
private fun FeeListSkeleton() {
    val shimmer = rememberSkeletonColor()
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
        userScrollEnabled = false
    ) {
        items(5) {
            FeeCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(36.dp).background(shimmer, CircleShape))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.width(130.dp).height(16.dp).background(shimmer, MaterialTheme.shapes.small))
                        Box(modifier = Modifier.width(85.dp).height(12.dp).background(shimmer, MaterialTheme.shapes.small))
                    }
                }
            }
        }
    }
}

/**
 * Maps a raw backend `statusId`/`status` string to a tinted [StatusPill]. The label
 * shown is always the backend's own value (title-cased) - never a made-up one - only
 * the colour is inferred, and only from common, unambiguous keywords so an
 * institution-specific status that doesn't match anything still renders, just in a
 * neutral tint instead of guessing.
 */
@Composable
private fun FeeStatusPill(statusId: String, modifier: Modifier = Modifier) {
    val statusColors = LocalStatusColors.current
    val normalized = statusId.trim().lowercase()
    val tint = when {
        normalized.contains("paid") || normalized.contains("success") ||
            normalized.contains("complete") || normalized.contains("approve") ||
            normalized.contains("active") -> statusColors.success
        normalized.contains("pending") || normalized.contains("process") ||
            normalized.contains("initiat") || normalized.contains("wait") ||
            normalized.contains("partial") -> statusColors.warning
        normalized.contains("fail") || normalized.contains("reject") ||
            normalized.contains("cancel") || normalized.contains("overdue") ||
            normalized.contains("declin") -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    StatusPill(text = statusId.replaceFirstChar { it.uppercaseChar() }, tint = tint, modifier = modifier)
}

// --- Summary header (outstanding balance + payment actions) ---

/**
 * Persistent header shown above the tab chips regardless of which tab is selected.
 * Reuses [FeeViewModel.statementState] - the same data the Invoices tab's real line
 * items come from - so the balance shown here can never drift from what "Invoices"
 * later explains it with.
 */
@Composable
private fun FeeSummarySection(
    state: UiState<FeeStatement>,
    paymentInFlight: Boolean,
    onRetry: () -> Unit,
    onPayFull: (Double) -> Unit,
    onPayDifferentAmount: () -> Unit,
    onPayWithUpi: (Double) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        when (state) {
            is UiState.Loading -> SummarySkeleton()
            is UiState.Success -> {
                val statement = state.data
                if (statement.isEmpty) {
                    SummaryEmptyCard()
                } else {
                    OutstandingBalanceCard(statement, paymentInFlight, onPayFull, onPayDifferentAmount, onPayWithUpi)
                }
            }
            is UiState.BusinessError -> SummaryErrorCard(state.message, onRetry)
            is UiState.NetworkError -> SummaryErrorCard(state.message, onRetry)
            is UiState.UnexpectedError -> SummaryErrorCard(state.message, onRetry)
        }
    }
}

@Composable
private fun SummarySkeleton() {
    val shimmer = rememberSkeletonColor()
    FeeCard {
        Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).background(shimmer, MaterialTheme.shapes.small))
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth(0.55f).height(32.dp).background(shimmer, MaterialTheme.shapes.small))
        Spacer(modifier = Modifier.height(18.dp))
        Box(modifier = Modifier.fillMaxWidth().height(46.dp).background(shimmer, MaterialTheme.shapes.medium))
    }
}

@Composable
private fun SummaryErrorCard(message: String, onRetry: () -> Unit) {
    FeeCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Default.ErrorOutline, tint = MaterialTheme.colorScheme.error)
            Column(modifier = Modifier.weight(1f)) {
                Text("Couldn't load your balance", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Composable
private fun SummaryEmptyCard() {
    FeeCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Default.CheckCircle, tint = LocalStatusColors.current.success)
            Column {
                Text("You're all caught up", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text("There's no pending fee payment.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Outstanding (most prominent, attention-tinted while anything is owed) above Total
 * billed / Total paid (neutral / positive-tinted) - the hierarchy the redesign asked
 * for. Every value is read straight from [FeeStatement]; nothing here is hardcoded.
 *
 * The three payment actions are now visually tiered rather than three near-identical
 * rows: a filled primary CTA for the full balance, an outlined secondary action for a
 * partial amount, and UPI as a bordered, tappable *payment method* row - not a plain
 * text button - so it reads as "another way to pay" rather than an afterthought link.
 */
@Composable
private fun OutstandingBalanceCard(
    statement: FeeStatement,
    paymentInFlight: Boolean,
    onPayFull: (Double) -> Unit,
    onPayDifferentAmount: () -> Unit,
    onPayWithUpi: (Double) -> Unit
) {
    val payable = statement.payableAmount
    val statusColors = LocalStatusColors.current

    FeeCard {
        Text("Outstanding", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            statement.outstandingBalance?.let { formatAmount(it) } ?: "—",
            style = MaterialTheme.typography.displaySmall,
            color = if (payable != null) statusColors.warning else statusColors.success
        )

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        AmountRow("Total billed", statement.totalDebits)
        AmountRow("Total paid", statement.totalCredits, tint = statusColors.success)

        if (payable != null) {
            Spacer(modifier = Modifier.height(16.dp))
            PrimaryButton(
                text = "Pay ${formatAmount(payable)}",
                onClick = { onPayFull(payable) },
                isLoading = paymentInFlight,
                enabled = !paymentInFlight,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onPayDifferentAmount,
                enabled = !paymentInFlight,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text("Pay a different amount", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Skips straight to Razorpay's UPI-intent sheet (GPay/PhonePe/Paytm/... -
            // whichever apps are installed) instead of the full method list above.
            UpiPaymentOption(
                enabled = !paymentInFlight,
                onClick = { onPayWithUpi(payable) }
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp))
            DetailText("Nothing is due right now.")
        }
    }
}

/**
 * UPI as a real selectable payment method - icon, label, one-line description and a
 * trailing chevron on a bordered surface - rather than the plain [TextButton] row it
 * used to be, which read as a stray caption instead of a third way to pay.
 */
@Composable
private fun UpiPaymentOption(enabled: Boolean, onClick: () -> Unit) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(MaterialTheme.shapes.small)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = spacing.md, vertical = spacing.sm)
            .alpha(if (enabled) 1f else 0.5f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Smartphone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text("Pay with UPI", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                "Any UPI app on this phone",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AmountRow(
    label: String,
    amount: Double?,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            amount?.let { formatAmount(it) } ?: "—",
            style = MaterialTheme.typography.titleSmall,
            color = tint
        )
    }
}

// --- Partial payment amount entry ---

/**
 * "Pay a different amount" sheet. Validation is the single [validatePaymentAmount]
 * rule the ViewModel also exposes - amount must be `> 0` and `<=` outstanding - shown
 * inline under the field via [PremiumTextField]'s own error slot rather than a
 * toast, and "Continue to Payment" stays disabled until the typed amount is valid.
 *
 * The outstanding balance, the amount being entered, and the balance that would
 * remain after paying it are always shown together, live, so the user never has to
 * do that subtraction themselves before confirming.
 */
@Composable
private fun AmountEntryDialog(
    outstanding: Double,
    onDismiss: () -> Unit,
    onContinue: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    val validation = remember(amountInput, outstanding) { validatePaymentAmount(amountInput, outstanding) }
    val errorMessage = (validation as? PaymentAmountValidation.Invalid)?.message?.takeIf { amountInput.isNotBlank() }
    val enteredAmount = (validation as? PaymentAmountValidation.Valid)?.amount
    val remaining = (outstanding - (enteredAmount ?: 0.0)).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pay a different amount", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                AmountSummaryRow("Outstanding balance", formatAmount(outstanding))

                PremiumTextField(
                    text = amountInput,
                    onTextChange = { input -> amountInput = input.filter { it.isDigit() || it == '.' } },
                    placeholder = "Amount to pay",
                    icon = Icons.Default.CurrencyRupee,
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done,
                    error = errorMessage
                )

                HorizontalDivider()

                AmountSummaryRow("You'll pay", enteredAmount?.let { formatAmount(it) } ?: "—")
                AmountSummaryRow(
                    "Remaining after this payment",
                    if (enteredAmount != null) formatAmount(remaining) else "—",
                    tint = if (enteredAmount != null) LocalStatusColors.current.warning else MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { enteredAmount?.let(onContinue) },
                enabled = enteredAmount != null
            ) {
                Text("Continue to Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AmountSummaryRow(
    label: String,
    value: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, color = tint)
    }
}

// --- Payment confirmation ---

/**
 * The confirmation step required before any online payment is initiated (full,
 * partial or UPI-intent): fee amount, remaining balance after this payment, the
 * chosen method, and a "Pay ₹X" button that finally triggers
 * [FeeViewModel.startOnlinePayment]. Nothing is charged until this dialog's button is
 * pressed.
 */
@Composable
private fun PaymentConfirmationDialog(
    amount: Double,
    outstanding: Double,
    methodLabel: String,
    paymentInFlight: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val remaining = (outstanding - amount).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = { if (!paymentInFlight) onDismiss() },
        title = { Text("Confirm payment", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ConfirmationRow("Fee amount", formatAmount(amount))
                ConfirmationRow("Remaining balance after this payment", formatAmount(remaining))
                ConfirmationRow("Payment method", methodLabel)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !paymentInFlight) {
                if (paymentInFlight) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Pay ${formatAmount(amount)}")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !paymentInFlight) { Text("Cancel") }
        }
    )
}

@Composable
private fun ConfirmationRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

// --- Dues (feeAssignment:view) ---

/**
 * Real pending fee items, one per [FeeAssignment]. [structureNames] joins each item's
 * `feeStructureId` against the Structures tab's own data (loaded up front by
 * [FeeViewModel.loadSummary]) so the fee actually has a name instead of a generic
 * placeholder - never an invented one.
 */
@Composable
private fun DuesTab(
    state: UiState<List<FeeAssignment>>,
    structureNames: Map<String?, String?>,
    onRetry: () -> Unit
) {
    when (state) {
        is UiState.Loading -> FeeListSkeleton()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState(
                title = "You're all caught up",
                message = "There's no pending fee payment.",
                icon = Icons.Default.CheckCircle
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
                    DueCard(item, structureNames[item.feeStructureId])
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun DueCard(item: FeeAssignment, feeName: String?) {
    FeeCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconBadge(Icons.Default.RequestQuote)
            Column(modifier = Modifier.weight(1f)) {
                Text(feeName ?: "Fee due", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(formatAmount(item.totalAmount), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            }
            item.statusId?.let { FeeStatusPill(it) }
        }
        if (item.dueDate != null) {
            Spacer(modifier = Modifier.height(10.dp))
            DetailText("Due ${item.dueDate}")
        }
    }
}

// --- Invoices (feeInvoice:view line items) ---

/**
 * `feeInvoice:view` returns one computed statement per student - the outstanding
 * card above already renders its totals. This tab shows only the statement's real
 * [FeeStatement.lines] - actual invoices, each with the backend's own invoice
 * reference, amount, date and status. When the backend hasn't itemised anything yet
 * (the common case - see [FeeStatement]), this is a genuine empty state, not a
 * fabricated list.
 */
@Composable
private fun InvoicesTab(state: UiState<FeeStatement>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> FeeListSkeleton()
        is UiState.Success -> {
            val lines = state.data.lines
            if (lines.isEmpty()) {
                EmptyState(
                    title = "No invoices yet",
                    message = "Itemized invoices will appear here once your fee office publishes them.",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong
                )
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(lines, key = { it.id ?: it.hashCode() }) { line -> InvoiceCard(line) }
                }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun InvoiceCard(item: FeeInvoice) {
    FeeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.AutoMirrored.Filled.ReceiptLong)
                Column {
                    Text(item.description ?: "Invoice", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(formatAmount(item.amount), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            item.statusId?.let { FeeStatusPill(it) }
        }
        val reference = item.referenceId ?: item.id
        val date = item.transactionDate ?: item.dueDate
        if (reference != null || date != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                reference?.let { DetailText("Invoice #$it") }
                date?.let { DetailText(it) }
            }
        }
    }
}

// --- Payments ---

@Composable
private fun PaymentsTab(state: UiState<List<FeePayment>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> FeeListSkeleton()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState(
                title = "No payments yet",
                message = "Your payment history will show up here once you make a payment.",
                icon = Icons.Default.CurrencyRupee
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.id ?: it.hashCode() }) { item -> PaymentCard(item) }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun PaymentCard(item: FeePayment) {
    FeeCard {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.Default.CurrencyRupee, tint = LocalStatusColors.current.success)
                Column {
                    Text(formatAmount(item.amount), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    item.paymentDate?.let { DetailText(it) }
                }
            }
            item.statusId?.let { FeeStatusPill(it) }
        }
        if (item.paymentMode != null || item.transactionReference != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                item.paymentMode?.let { DetailText(it) }
                item.transactionReference?.let { DetailText("Ref: $it") }
            }
        }
    }
}

// --- Concessions ---

@Composable
private fun ConcessionsTab(state: UiState<List<FeeConcession>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> FeeListSkeleton()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState(
                title = "No concessions",
                message = "You don't have any fee concessions on record.",
                icon = Icons.Default.Discount
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.id ?: it.hashCode() }) { item -> ConcessionCard(item) }
            }
        }
        is UiState.BusinessError -> ErrorState(state.message, onRetry)
        is UiState.NetworkError -> ErrorState(state.message, onRetry)
        is UiState.UnexpectedError -> ErrorState(state.message, onRetry)
    }
}

@Composable
private fun ConcessionCard(item: FeeConcession) {
    FeeCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(Icons.Default.Discount, tint = LocalStatusColors.current.success)
                Column {
                    val amountLabel = item.amount?.let { formatAmount(it) } ?: item.percentage?.let { "$it%" } ?: "—"
                    Text(amountLabel, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    item.approvedOn?.let { DetailText("Approved $it") }
                }
            }
            item.statusId?.let { FeeStatusPill(it) }
        }
        if (item.reason != null || item.approvedBy != null) {
            Spacer(modifier = Modifier.height(10.dp))
            item.reason?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item.approvedBy?.let { DetailText("Approved by $it") }
        }
    }
}

// --- Refunds ---

/**
 * Refunds cover several fee categories (academic, hostel, transport, ...), so this
 * tab offers two things a plain list didn't: a category filter row - built from
 * [DEFAULT_REFUND_TYPES] plus whatever [FeeRefund.feeType] values actually come back,
 * so an institution-specific category still shows up - and a "Request refund" button
 * that opens [RequestRefundDialog] to file a new one (`feeRefund:add`).
 *
 * The filter is rendered with [FeeCategoryChipRow]: a single-line, horizontally
 * scrolling chip row. It never shrinks a chip to fit the screen and never wraps a
 * category name onto a second line - the fix for "Transport Fees" (and any other
 * category) being squeezed into a sliver-thin column of letters.
 */
@Composable
private fun RefundsTab(
    state: UiState<List<FeeRefund>>,
    payments: List<FeePayment>,
    requestInFlight: Boolean,
    onRetry: () -> Unit,
    onRequestRefund: (paymentId: String?, amount: Double, reason: String, feeType: String?) -> Unit
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showRequestDialog by remember { mutableStateOf(false) }

    if (showRequestDialog) {
        RequestRefundDialog(
            payments = payments,
            inFlight = requestInFlight,
            onDismiss = { showRequestDialog = false },
            onSubmit = { paymentId, amount, reason, feeType ->
                onRequestRefund(paymentId, amount, reason, feeType)
                showRequestDialog = false
            }
        )
    }

    when (state) {
        is UiState.Loading -> FeeListSkeleton()
        is UiState.Success -> {
            val types = remember(state.data) {
                (DEFAULT_REFUND_TYPES + state.data.mapNotNull { it.feeType }).distinct()
            }
            val visible = if (selectedType == null) {
                state.data
            } else {
                state.data.filter { it.feeType == selectedType }
            }

            // Filter chips and the "Request refund" action are fixed-height siblings
            // above the (weighted) scrollable list, in that order, each with the same
            // 16dp page margin - a deliberate stack instead of two controls that used
            // to compete for the same row and read as unrelated to one another.
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.data.isNotEmpty()) {
                    FeeCategoryChipRow(
                        categories = types,
                        selected = selectedType,
                        onSelect = { selectedType = it }
                    )
                }
                RequestRefundButtonRow(onClick = { showRequestDialog = true })

                if (state.data.isEmpty()) {
                    EmptyState("No refunds on record.", icon = Icons.Default.Replay, modifier = Modifier.weight(1f))
                } else if (visible.isEmpty()) {
                    EmptyState("No refunds for this category.", modifier = Modifier.weight(1f))
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(visible, key = { it.hashCode() }) { item ->
                            FeeCard {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    IconBadge(Icons.Default.Replay)
                                    Column {
                                        Text(formatAmount(item.amount), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                                        item.refundDate?.let { DetailText(it) }
                                    }
                                }
                                if (item.feeType != null) {
                                    Text(
                                        item.feeType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .padding(top = 10.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (item.reason != null) {
                                    Text(item.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
                                }
                            }
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

/**
 * "Request refund" action, placed cleanly below the category chips with the same
 * horizontal page margin they use, rather than floating in its own disconnected row.
 */
@Composable
private fun RequestRefundButtonRow(onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        horizontalArrangement = Arrangement.End
    ) {
        OutlinedButton(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.heightIn(min = 44.dp)
        ) {
            Icon(Icons.Default.RequestQuote, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Request refund", style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * A proper horizontally scrollable, single-line category filter - `[ All ] [ Academic
 * Fees ] [ Hostel Fees ] [ Transport Fees ] ...` - built from [FeeCategoryChip]. Chips
 * size to their own content and the row scrolls once they exceed the screen width;
 * nothing here ever shrinks a chip's width or wraps its label onto a second line.
 */
@Composable
private fun FeeCategoryChipRow(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        FeeCategoryChip(label = "All", selected = selected == null, onClick = { onSelect(null) })
        categories.forEach { category ->
            FeeCategoryChip(label = category, selected = selected == category, onClick = { onSelect(category) })
        }
    }
}

/**
 * One chip in [FeeCategoryChipRow] (and reused by [RequestRefundDialog]'s category and
 * payment pickers). A fixed 48dp-minimum height and a single-line, non-wrapping label
 * are load-bearing, not decorative - a category name is never allowed to wrap
 * letter-by-letter, whatever width it ends up measured against.
 */
@Composable
private fun FeeCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                maxLines = 1,
                softWrap = false,
                style = MaterialTheme.typography.labelLarge
            )
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        modifier = Modifier.heightIn(min = 48.dp)
    )
}

/**
 * "Request refund" form. [payments] (already-fetched Payments tab data) lets the
 * user pick which payment the refund is against without retyping a reference by
 * hand; picking one is optional since the accounts office can also work from
 * [amount]/[reason] alone.
 *
 * Both chip rows below (fee type, payment) are horizontally scrollable via
 * [FeeCategoryChip] rather than a plain wrapping [Row] - inside an [AlertDialog]'s
 * fixed width, an un-scrollable row of five category chips (or six payment chips) is
 * exactly the layout that squeezed a chip's label into a sliver and wrapped it
 * letter-by-letter.
 */
@Composable
private fun RequestRefundDialog(
    payments: List<FeePayment>,
    inFlight: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (paymentId: String?, amount: Double, reason: String, feeType: String?) -> Unit
) {
    var selectedPayment by remember { mutableStateOf<FeePayment?>(null) }
    var selectedType by remember { mutableStateOf(DEFAULT_REFUND_TYPES.first()) }
    var amountInput by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("") }

    val amount = amountInput.trim().toDoubleOrNull()
    val canSubmit = !inFlight && amount != null && amount > 0.0 && reasonInput.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!inFlight) onDismiss() },
        title = { Text("Request a refund") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Fee type", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DEFAULT_REFUND_TYPES.forEach { type ->
                        FeeCategoryChip(
                            label = type,
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                    }
                }

                if (payments.isNotEmpty()) {
                    Text("Payment to refund (optional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        payments.take(6).forEach { payment ->
                            FeeCategoryChip(
                                label = formatAmount(payment.amount),
                                selected = selectedPayment?.id == payment.id,
                                onClick = {
                                    selectedPayment = if (selectedPayment?.id == payment.id) null else payment
                                    if (amountInput.isBlank()) {
                                        payment.amount?.let { amountInput = it }
                                    }
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { amount?.let { onSubmit(selectedPayment?.id, it, reasonInput.trim(), selectedType) } },
                enabled = canSubmit
            ) {
                if (inFlight) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !inFlight) { Text("Cancel") }
        }
    )
}

// --- Penalties ---

@Composable
private fun PenaltiesTab(state: UiState<List<FeePenalty>>, onRetry: () -> Unit) {
    when (state) {
        is UiState.Loading -> FeeListSkeleton()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No late-payment penalties on record.", icon = Icons.Default.Gavel)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
                    FeeCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                IconBadge(Icons.Default.Gavel, tint = MaterialTheme.colorScheme.error)
                                Column {
                                    Text(formatAmount(item.amount), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                                    item.appliedDate?.let { DetailText("Applied $it") }
                                }
                            }
                            item.statusId?.let { FeeStatusPill(it) }
                        }
                        if (item.reason != null) {
                            Text(item.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp))
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
        is UiState.Loading -> FeeListSkeleton()
        is UiState.Success -> if (state.data.isEmpty()) {
            EmptyState("No fee structures published yet.", icon = Icons.AutoMirrored.Filled.Rule)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(state.data, key = { it.id ?: it.hashCode() }) { item ->
                    FeeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            IconBadge(Icons.AutoMirrored.Filled.Rule)
                            Column {
                                Text(item.name ?: "—", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                if (item.code != null) {
                                    Text(
                                        item.code,
                                        style = MaterialTheme.typography.labelSmall,
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
                            DetailText(range, modifier = Modifier.padding(top = 10.dp))
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
