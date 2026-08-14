package com.xsc.oneapp.feature.fee.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.fee.data.network.FeeEndpoint
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder
import com.xsc.oneapp.feature.fee.domain.usecase.AssignFeeUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.CreateFeeStructureUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.DeleteFeeAssignmentUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.DeleteFeeStructureUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeePenaltiesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeeStructuresUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeAssignmentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeConcessionsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeInvoicesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeePaymentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeRefundsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GrantConcessionUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.InitiateOnlinePaymentUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.RequestRefundUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.UpdateFeeStructureStatusUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.UpdateRefundStatusUseCase
import com.xsc.oneapp.feature.fee.ui.state.FeeEffect
import com.xsc.oneapp.feature.fee.ui.state.FeePermissions
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.auth.requiredPermissionFor
import com.xsc.sdk.network.APIError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One [SectionLoader] per fee sub-module.
 *
 * The previous version fired all seven use cases from `init {}`, so opening the Fees
 * screen dispatched seven concurrent requests at the single `POST /` dispatcher for tabs
 * the user had not looked at - visible in the server log as feeConcession, feePayment,
 * feeInvoice, feeAssignment, feeStructure, feePenalty and feeRefund all landing inside
 * the same 70ms window, six of them for content that was never rendered.
 *
 * A section now loads the first time its tab is selected, via [onTabSelected]. Public
 * names are unchanged - `structuresState`, `loadStructures()` and friends still exist
 * with the same meaning, so the screen's retry wiring is untouched.
 */
@HiltViewModel
class FeeViewModel @Inject constructor(
    getFeeStructuresUseCase: GetFeeStructuresUseCase,
    getMyFeeAssignmentsUseCase: GetMyFeeAssignmentsUseCase,
    getMyFeeConcessionsUseCase: GetMyFeeConcessionsUseCase,
    getMyFeeInvoicesUseCase: GetMyFeeInvoicesUseCase,
    getMyFeePaymentsUseCase: GetMyFeePaymentsUseCase,
    getMyFeeRefundsUseCase: GetMyFeeRefundsUseCase,
    getFeePenaltiesUseCase: GetFeePenaltiesUseCase,
    private val createFeeStructureUseCase: CreateFeeStructureUseCase,
    private val updateFeeStructureStatusUseCase: UpdateFeeStructureStatusUseCase,
    private val deleteFeeStructureUseCase: DeleteFeeStructureUseCase,
    private val assignFeeUseCase: AssignFeeUseCase,
    private val deleteFeeAssignmentUseCase: DeleteFeeAssignmentUseCase,
    private val grantConcessionUseCase: GrantConcessionUseCase,
    private val initiateOnlinePaymentUseCase: InitiateOnlinePaymentUseCase,
    private val requestRefundUseCase: RequestRefundUseCase,
    private val updateRefundStatusUseCase: UpdateRefundStatusUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val structures = SectionLoader(viewModelScope, "fee structures") { getFeeStructuresUseCase() }
    private val assignments = SectionLoader(viewModelScope, "fee dues") { getMyFeeAssignmentsUseCase() }
    private val concessions = SectionLoader(viewModelScope, "fee concessions") { getMyFeeConcessionsUseCase() }
    private val invoices = SectionLoader(viewModelScope, "fee invoices") { getMyFeeInvoicesUseCase() }
    private val payments = SectionLoader(viewModelScope, "fee payments") { getMyFeePaymentsUseCase() }
    private val refunds = SectionLoader(viewModelScope, "fee refunds") { getMyFeeRefundsUseCase() }
    private val penalties = SectionLoader(viewModelScope, "fee penalties") { getFeePenaltiesUseCase() }

    val structuresState: StateFlow<UiState<List<FeeStructure>>> = structures.state
    val assignmentsState: StateFlow<UiState<List<FeeAssignment>>> = assignments.state
    val concessionsState: StateFlow<UiState<List<FeeConcession>>> = concessions.state
    val invoicesState: StateFlow<UiState<List<FeeInvoice>>> = invoices.state
    val paymentsState: StateFlow<UiState<List<FeePayment>>> = payments.state
    val refundsState: StateFlow<UiState<List<FeeRefund>>> = refunds.state
    val penaltiesState: StateFlow<UiState<List<FeePenalty>>> = penalties.state

    /** First real (non defense-in-depth) UI consumer of [SessionManager.hasPermission] -
     * see [FeePermissions]'s own kdoc. Re-derived on every permissions change (e.g. a
     * token refresh), not computed once at init. */
    val permissions: StateFlow<FeePermissions> = sessionManager.currentPermissions
        .map {
            FeePermissions(
                canAddFeeStructure = sessionManager.hasPermission(feePermission(FeeEndpoint.SubModules.FEE_STRUCTURE, FeeEndpoint.Actions.FEE_STRUCTURE, FeeEndpoint.ActionTypes.ADD)),
                canUpdateFeeStructure = sessionManager.hasPermission(feePermission(FeeEndpoint.SubModules.FEE_STRUCTURE, FeeEndpoint.Actions.FEE_STRUCTURE, FeeEndpoint.ActionTypes.UPDATE)),
                canDeleteFeeStructure = sessionManager.hasPermission(feePermission(FeeEndpoint.SubModules.FEE_STRUCTURE, FeeEndpoint.Actions.FEE_STRUCTURE, FeeEndpoint.ActionTypes.DELETE)),
                canAssignFee = sessionManager.hasPermission(feePermission(FeeEndpoint.SubModules.FEE_ASSIGNMENT, FeeEndpoint.Actions.FEE_ASSIGNMENT, FeeEndpoint.ActionTypes.ADD)),
                canDeleteFeeAssignment = sessionManager.hasPermission(feePermission(FeeEndpoint.SubModules.FEE_ASSIGNMENT, FeeEndpoint.Actions.FEE_ASSIGNMENT, FeeEndpoint.ActionTypes.DELETE)),
                canGrantConcession = sessionManager.hasPermission(feePermission(FeeEndpoint.SubModules.CONCESSION, FeeEndpoint.Actions.FEE_CONCESSION, FeeEndpoint.ActionTypes.ADD)),
                canApproveRefund = sessionManager.hasPermission(feePermission(FeeEndpoint.SubModules.REFUND, FeeEndpoint.Actions.FEE_REFUND, FeeEndpoint.ActionTypes.UPDATE))
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, FeePermissions())

    private fun feePermission(submodule: String, action: String, actionType: String) =
        requiredPermissionFor(FeeEndpoint.MODULE, submodule, action, actionType)

    private val _effect = MutableSharedFlow<FeeEffect>()
    val effect: SharedFlow<FeeEffect> = _effect.asSharedFlow()

    /**
     * Loads the selected tab's data if it hasn't been fetched yet. Indices match
     * FeeScreen's TAB_TITLES order.
     */
    fun onTabSelected(index: Int) {
        when (index) {
            0 -> assignments.loadOnce()
            1 -> invoices.loadOnce()
            2 -> payments.loadOnce()
            3 -> concessions.loadOnce()
            4 -> refunds.loadOnce()
            5 -> penalties.loadOnce()
            6 -> structures.loadOnce()
        }
    }

    // Explicit user-driven refresh, wired to each tab's retry button. Names unchanged.
    fun loadStructures() = structures.reload()
    fun loadAssignments() = assignments.reload()
    fun loadConcessions() = concessions.reload()
    fun loadInvoices() = invoices.reload()
    fun loadPayments() = payments.reload()
    fun loadRefunds() = refunds.reload()
    fun loadPenalties() = penalties.reload()

    fun createFeeStructure(
        code: String,
        name: String,
        academicYearId: String,
        programmeId: String,
        components: List<FeeStructureComponent>,
        effectiveFrom: String,
        effectiveTo: String?
    ) = submit("Fee structure created") {
        createFeeStructureUseCase(code, name, academicYearId, programmeId, components, effectiveFrom, effectiveTo)
        structures.reload()
    }

    fun updateFeeStructureStatus(structureId: String, status: String) = submit("Fee structure updated") {
        updateFeeStructureStatusUseCase(structureId, status)
        structures.reload()
    }

    fun deleteFeeStructure(structureId: String) = submit("Fee structure deactivated") {
        deleteFeeStructureUseCase(structureId)
        structures.reload()
    }

    fun assignFee(feeStructureId: String, termId: String, dueDate: String, studentIds: List<String>) =
        submit("Fee assigned") {
            assignFeeUseCase(feeStructureId, termId, dueDate, studentIds)
            assignments.reload()
        }

    fun deleteFeeAssignment(assignmentId: String) = submit("Fee assignment deactivated") {
        deleteFeeAssignmentUseCase(assignmentId)
        assignments.reload()
    }

    fun grantConcession(studentId: String, assignmentId: String, concessionType: String, amountOrPercent: Double, reason: String) =
        submit("Concession granted") {
            grantConcessionUseCase(studentId, assignmentId, concessionType, amountOrPercent, reason)
            concessions.reload()
        }

    fun requestRefund(invoiceId: String, amount: Double, reason: String) = submit("Refund requested") {
        requestRefundUseCase(invoiceId, amount, reason)
        refunds.reload()
    }

    fun updateRefundStatus(refundId: String, status: String, remarks: String?) =
        submit(if (status == "APPROVED") "Refund approved" else "Refund rejected") {
            updateRefundStatusUseCase(refundId, status, remarks)
            refunds.reload()
        }

    /** Called from the payment screen's own coroutine scope rather than fired via
     * [submit] - the caller needs the [RazorpayOrder] back to actually open checkout,
     * not just a toast, so this is a plain suspend function the caller wraps in its
     * own try/catch. */
    suspend fun initiateOnlinePayment(invoiceId: String, amount: Double): RazorpayOrder =
        initiateOnlinePaymentUseCase(invoiceId, amount, paidBy = "Student Self")

    /** Called once Razorpay's checkout SDK reports success - never a client-side
     * "payment complete" claim (feePayment/update has no confirmed contract yet), just
     * a re-fetch so the screen reflects whatever the backend has actually posted. */
    fun onOnlinePaymentSubmitted() {
        viewModelScope.launch {
            _effect.emit(FeeEffect.ShowToast("Payment submitted - your balance will update shortly"))
        }
        invoices.reload()
        payments.reload()
    }

    /** Every write action here is a one-shot toast, not a screen-replacing error
     * state - a failed submit shouldn't blank out data the student can already see. */
    private fun submit(successMessage: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _effect.emit(FeeEffect.ShowToast(successMessage))
            } catch (e: APIError) {
                _effect.emit(FeeEffect.ShowToast(e.message ?: "Something went wrong"))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _effect.emit(FeeEffect.ShowToast("Something went wrong"))
            }
        }
    }
}
