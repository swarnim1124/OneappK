package com.xsc.oneapp.feature.fee.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.dataOrNull
import com.xsc.oneapp.feature.fee.BuildConfig
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.usecase.ConfirmOnlinePaymentUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.CreateOnlinePaymentOrderUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeePenaltiesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeeStructuresUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeAssignmentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeConcessionsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeePaymentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeRefundsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeStatementUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.RequestFeeRefundUseCase
import com.xsc.oneapp.feature.fee.payment.RazorpayCheckoutRequest
import com.xsc.oneapp.feature.fee.payment.RazorpayCheckoutResult
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToLong

/** One-shot things the Fees screen has to act on rather than render. */
sealed interface FeeEffect {
    /** Hand off to Razorpay Checkout. The screen owns the activity launcher. */
    data class LaunchCheckout(val request: RazorpayCheckoutRequest) : FeeEffect

    /** Show a snackbar. [isError] only changes styling - a payment that succeeded but
     * could not be reconciled is *not* an error to the user. */
    data class Notify(val message: String, val isError: Boolean = false) : FeeEffect
}

/** Common fee categories shown on the refund filter and the "Request refund" form.
 * Not a confirmed backend enum (see [FeeRefund.feeType]) - a refund's actual
 * `feeType`, once returned by the backend, is added to this list too so a value the
 * institution uses that isn't one of these defaults still shows up as a filter. */
val DEFAULT_REFUND_TYPES = listOf("Academic Fees", "Hostel Fees", "Transport Fees", "Library Fees", "Other")

/** Result of validating a partial-payment amount typed into the "Pay a different
 * amount" sheet. Kept as a pure function of (input, outstanding) so the same rule
 * drives both the inline error message under the field and whether "Continue to
 * payment" is enabled - there is exactly one definition of "valid" here. */
sealed interface PaymentAmountValidation {
    data class Valid(val amount: Double) : PaymentAmountValidation
    data class Invalid(val message: String) : PaymentAmountValidation
}

/**
 * Rules (m_fees redesign spec): amount must be `> 0` and `<= outstanding`. Empty,
 * zero, negative and non-numeric input are all invalid, each with its own message
 * rather than one generic "invalid amount" - a validation message the user can't
 * act on is worse than none.
 */
fun validatePaymentAmount(input: String, outstanding: Double): PaymentAmountValidation {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return PaymentAmountValidation.Invalid("Enter an amount")

    val amount = trimmed.toDoubleOrNull()
        ?: return PaymentAmountValidation.Invalid("Enter a valid number")

    return when {
        amount <= 0.0 -> PaymentAmountValidation.Invalid("Amount must be greater than ₹0")
        amount > outstanding -> PaymentAmountValidation.Invalid("Amount cannot be more than the outstanding balance")
        else -> PaymentAmountValidation.Valid(amount)
    }
}

/**
 * One [SectionLoader] per fee sub-module, plus the online-payment flow.
 *
 * Sections load the first time their tab is opened ([onTabSelected]) rather than all
 * seven from `init {}`, which used to fire seven concurrent requests at the single
 * `POST /` dispatcher for tabs the user never looked at.
 *
 * The Invoices section changed shape on 2026-08-14: `feeInvoice:view` returns one
 * statement object (totalDebits/totalCredits/outstandingBalance), not a row list, so
 * [statementState] replaced the old `invoicesState`. The previous list modelling is
 * why that tab rendered a single blank card.
 */
@HiltViewModel
class FeeViewModel @Inject constructor(
    getFeeStructuresUseCase: GetFeeStructuresUseCase,
    getMyFeeAssignmentsUseCase: GetMyFeeAssignmentsUseCase,
    getMyFeeConcessionsUseCase: GetMyFeeConcessionsUseCase,
    getMyFeeStatementUseCase: GetMyFeeStatementUseCase,
    getMyFeePaymentsUseCase: GetMyFeePaymentsUseCase,
    getMyFeeRefundsUseCase: GetMyFeeRefundsUseCase,
    getFeePenaltiesUseCase: GetFeePenaltiesUseCase,
    private val createOnlinePaymentOrderUseCase: CreateOnlinePaymentOrderUseCase,
    private val confirmOnlinePaymentUseCase: ConfirmOnlinePaymentUseCase,
    private val requestFeeRefundUseCase: RequestFeeRefundUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val structures = SectionLoader(viewModelScope) { getFeeStructuresUseCase() }
    private val assignments = SectionLoader(viewModelScope) { getMyFeeAssignmentsUseCase() }
    private val concessions = SectionLoader(viewModelScope) { getMyFeeConcessionsUseCase() }
    private val statement = SectionLoader(viewModelScope) { getMyFeeStatementUseCase() }
    private val payments = SectionLoader(viewModelScope) { getMyFeePaymentsUseCase() }
    private val refunds = SectionLoader(viewModelScope) { getMyFeeRefundsUseCase() }
    private val penalties = SectionLoader(viewModelScope) { getFeePenaltiesUseCase() }

    val structuresState: StateFlow<UiState<List<FeeStructure>>> = structures.state
    val assignmentsState: StateFlow<UiState<List<FeeAssignment>>> = assignments.state
    val concessionsState: StateFlow<UiState<List<FeeConcession>>> = concessions.state
    val statementState: StateFlow<UiState<FeeStatement>> = statement.state
    val paymentsState: StateFlow<UiState<List<FeePayment>>> = payments.state
    val refundsState: StateFlow<UiState<List<FeeRefund>>> = refunds.state
    val penaltiesState: StateFlow<UiState<List<FeePenalty>>> = penalties.state

    private val _paymentInFlight = MutableStateFlow(false)
    /** True from "Pay now" until the gateway result has been handled. Disables the
     * button so a double tap cannot open two orders for the same balance. */
    val paymentInFlight: StateFlow<Boolean> = _paymentInFlight.asStateFlow()

    private val _effects = Channel<FeeEffect>(Channel.BUFFERED)
    val effects: Flow<FeeEffect> = _effects.receiveAsFlow()

    private val _refundRequestInFlight = MutableStateFlow(false)
    /** True while a "Request refund" submission is in flight. Disables the dialog's
     * submit button so a double tap cannot file the same request twice. */
    val refundRequestInFlight: StateFlow<Boolean> = _refundRequestInFlight.asStateFlow()

    /** Loads the selected tab's data if it hasn't been fetched yet. Indices match
     * FeeScreen's TAB_TITLES order. */
    fun onTabSelected(index: Int) {
        when (index) {
            0 -> assignments.loadOnce()
            1 -> statement.loadOnce()
            2 -> payments.loadOnce()
            3 -> concessions.loadOnce()
            4 -> refunds.loadOnce()
            5 -> penalties.loadOnce()
            6 -> structures.loadOnce()
        }
    }

    // Explicit user-driven refresh, wired to each tab's retry button.
    fun loadStructures() = structures.reload()
    fun loadAssignments() = assignments.reload()
    fun loadConcessions() = concessions.reload()
    fun loadStatement() = statement.reload()
    fun loadPayments() = payments.reload()
    fun loadRefunds() = refunds.reload()
    fun loadPenalties() = penalties.reload()

    /** Loads the outstanding-balance summary as soon as the Fees screen opens,
     * independent of which tab is selected - the summary card and payment actions
     * now live above the tabs (always visible), not inside the Invoices tab, so it
     * can no longer wait for [onTabSelected] to pick tab 1. Safe to call on every
     * composition: [SectionLoader.loadOnce] only fetches once.
     *
     * Also loads fee structures up front (not just when tab 6 is opened) so the Dues
     * tab can resolve each [FeeAssignment.feeStructureId] to a real
     * [FeeStructure.name] without waiting for the user to visit Structures first -
     * that join is the only way to show an actual fee name instead of a generic
     * "Fee due" label. */
    fun loadSummary() {
        statement.loadOnce()
        structures.loadOnce()
    }

    /**
     * Step 1 of paying: ask the backend for a gateway order, then hand the screen a
     * [RazorpayCheckoutRequest]. Nothing is charged here.
     *
     * [amount] defaults to the full outstanding balance when null (the original
     * "Pay now" contract - a client-supplied figure was never accepted from a text
     * field here). Passing a smaller, non-null value is the partial-payment path:
     * still bounded by the statement's own [FeeStatement.payableAmount] rather than
     * trusted as-is, so a stale amount-entry sheet can't submit more than what is
     * actually outstanding right now.
     *
     * [useUpiIntent] routes straight to Razorpay's UPI-intent flow - the sheet that
     * hands off to whichever UPI apps (GPay, PhonePe, Paytm, ...) are installed on
     * the phone - instead of the full Checkout method list. See
     * [RazorpayCheckoutRequest.useUpiIntentFlow].
     */
    fun startOnlinePayment(amount: Double? = null, useUpiIntent: Boolean = false) {
        if (_paymentInFlight.value) return

        val current = statementState.value.dataOrNull()
        val payable = current?.payableAmount
        if (current == null || payable == null) {
            _effects.trySend(FeeEffect.Notify("There is nothing outstanding to pay right now."))
            return
        }

        val chargeAmount = amount ?: payable
        if (chargeAmount <= 0.0 || chargeAmount > payable) {
            _effects.trySend(FeeEffect.Notify("Enter a valid amount to pay.", isError = true))
            return
        }

        // The statement's own lines carry the invoice id `feePayment:add` documents as
        // required, but they're only populated when the backend nests them (see
        // FeeStatement.lines) - the plain aggregate-object response the contract
        // documents has none. Falling back to the matching fee assignment's id keeps
        // "Pay now" working in that (the common) case instead of the request silently
        // going out without an invoiceId and failing the backend's own validation -
        // this is the concrete cause behind fee payment's "validation failed" report.
        val invoiceId = current.lines.firstNotNullOfOrNull { it.id }
            ?: assignmentsState.value.dataOrNull()?.firstNotNullOfOrNull { it.id }

        _paymentInFlight.value = true
        viewModelScope.launch {
            try {
                val order = createOnlinePaymentOrderUseCase(
                    invoiceId = invoiceId,
                    amount = chargeAmount,
                    paidBy = sessionManager.getDisplayName()
                )
                pendingBackendPaymentId = order.backendPaymentId

                _effects.send(
                    FeeEffect.LaunchCheckout(
                        RazorpayCheckoutRequest(
                            keyId = BuildConfig.RAZORPAY_KEY_ID,
                            // Dropped unless the backend produced a real Razorpay
                            // order - see PaymentOrder.isRealGatewayOrder.
                            orderId = order.orderId.takeIf { order.isRealGatewayOrder },
                            amountInPaise = order.amountInPaise.takeIf { it > 0L }
                                ?: (chargeAmount * 100).roundToLong(),
                            currency = order.currency,
                            merchantName = "OneApp",
                            description = "Fee payment",
                            email = sessionManager.currentEmail.value,
                            contact = null,
                            useUpiIntentFlow = useUpiIntent
                        )
                    )
                )
            } catch (e: APIError) {
                // Each APIError subclass carries its own `errorMessage`, but the sealed
                // base only has Throwable.message - which it is constructed with, so
                // this is the same string without a per-subclass `when`.
                _paymentInFlight.value = false
                _effects.send(
                    FeeEffect.Notify(
                        e.message ?: "Could not start the payment. Please try again.",
                        isError = true
                    )
                )
            } catch (e: Exception) {
                _paymentInFlight.value = false
                _effects.send(
                    FeeEffect.Notify(
                        e.message ?: "Could not start the payment. Please try again.",
                        isError = true
                    )
                )
            }
        }
    }

    /**
     * Step 2: the gateway came back.
     *
     * On success the ledger update is best effort. The card has already been charged
     * by this point, so a rejected `feePayment:update` must not read as a failed
     * payment - the user is told it is paid and reconciling, and the statement and
     * payment history are refetched either way so whatever the server does believe
     * is what gets shown.
     */
    fun onCheckoutResult(result: RazorpayCheckoutResult) {
        viewModelScope.launch {
            when (result) {
                is RazorpayCheckoutResult.Success -> {
                    val reconciled = confirmOnlinePaymentUseCase(
                        paymentId = pendingBackendPaymentId ?: result.paymentId,
                        succeeded = true
                    )
                    statement.reload()
                    payments.reload()
                    _effects.send(
                        FeeEffect.Notify(
                            if (reconciled) {
                                "Payment successful. Reference ${result.paymentId}."
                            } else {
                                "Payment successful (${result.paymentId}). It may take a few " +
                                    "minutes to appear on your statement."
                            }
                        )
                    )
                }

                is RazorpayCheckoutResult.Failed -> {
                    pendingBackendPaymentId?.let { confirmOnlinePaymentUseCase(it, succeeded = false) }
                    _effects.send(FeeEffect.Notify(result.message, isError = true))
                }

                RazorpayCheckoutResult.Cancelled ->
                    _effects.send(FeeEffect.Notify("Payment cancelled. You have not been charged."))
            }
            pendingBackendPaymentId = null
            _paymentInFlight.value = false
        }
    }

    /**
     * The backend's own payment-row id, when `feePayment:add` returned one. Preferred
     * over the gateway's id when reporting the outcome, since that is the key the
     * ledger is actually keyed on. Null for the documented response shape, which only
     * carries the order.
     */
    private var pendingBackendPaymentId: String? = null

    /**
     * Submits a refund request (`feeRefund:add`) for a payment the student already
     * made. [paymentId] is optional - the office can look a payment up from
     * [reason]/[amount] alone - but is sent when the "Request refund" form has one
     * selected. On success the Refunds tab is reloaded so the new (likely "pending")
     * request shows up immediately.
     */
    fun requestRefund(paymentId: String?, amount: Double, reason: String, feeType: String?) {
        if (_refundRequestInFlight.value) return
        _refundRequestInFlight.value = true
        viewModelScope.launch {
            try {
                requestFeeRefundUseCase(paymentId, amount, reason, feeType)
                refunds.reload()
                _effects.send(FeeEffect.Notify("Refund request submitted."))
            } catch (e: APIError) {
                _effects.send(
                    FeeEffect.Notify(
                        e.message ?: "Could not submit the refund request. Please try again.",
                        isError = true
                    )
                )
            } catch (e: Exception) {
                _effects.send(
                    FeeEffect.Notify(
                        e.message ?: "Could not submit the refund request. Please try again.",
                        isError = true
                    )
                )
            } finally {
                _refundRequestInFlight.value = false
            }
        }
    }
}
