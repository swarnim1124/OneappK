package com.xsc.oneapp.feature.fee.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.result.SectionLoader
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeePenaltiesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeeStructuresUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeAssignmentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeConcessionsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeInvoicesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeePaymentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeRefundsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
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
    getFeePenaltiesUseCase: GetFeePenaltiesUseCase
) : ViewModel() {

    private val structures = SectionLoader(viewModelScope) { getFeeStructuresUseCase() }
    private val assignments = SectionLoader(viewModelScope) { getMyFeeAssignmentsUseCase() }
    private val concessions = SectionLoader(viewModelScope) { getMyFeeConcessionsUseCase() }
    private val invoices = SectionLoader(viewModelScope) { getMyFeeInvoicesUseCase() }
    private val payments = SectionLoader(viewModelScope) { getMyFeePaymentsUseCase() }
    private val refunds = SectionLoader(viewModelScope) { getMyFeeRefundsUseCase() }
    private val penalties = SectionLoader(viewModelScope) { getFeePenaltiesUseCase() }

    val structuresState: StateFlow<UiState<List<FeeStructure>>> = structures.state
    val assignmentsState: StateFlow<UiState<List<FeeAssignment>>> = assignments.state
    val concessionsState: StateFlow<UiState<List<FeeConcession>>> = concessions.state
    val invoicesState: StateFlow<UiState<List<FeeInvoice>>> = invoices.state
    val paymentsState: StateFlow<UiState<List<FeePayment>>> = payments.state
    val refundsState: StateFlow<UiState<List<FeeRefund>>> = refunds.state
    val penaltiesState: StateFlow<UiState<List<FeePenalty>>> = penalties.state

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
}
