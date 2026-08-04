package com.xsc.oneapp.feature.fee.data.repository

import com.google.gson.JsonElement
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.fee.data.mapper.toFeeAssignment
import com.xsc.oneapp.feature.fee.data.mapper.toFeeConcession
import com.xsc.oneapp.feature.fee.data.mapper.toFeeInvoice
import com.xsc.oneapp.feature.fee.data.mapper.toFeePayment
import com.xsc.oneapp.feature.fee.data.mapper.toFeePenalty
import com.xsc.oneapp.feature.fee.data.mapper.toFeeRefund
import com.xsc.oneapp.feature.fee.data.mapper.toFeeStructure
import com.xsc.oneapp.feature.fee.data.network.FeeEndpoint
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import javax.inject.Inject

class FeeRepositoryImpl @Inject constructor(
    private val apiClient: APIClient,
    private val sessionManager: SessionManager
) : FeeRepository {

    private suspend fun view(submodule: String, action: String, payload: Map<String, Any>): JsonElement? =
        apiClient.request(
            module = FeeEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = FeeEndpoint.ActionTypes.VIEW,
            payload = payload
        )

    /** m_fees contract: feeStructure/view - instId included when the session has
     * one (defensive-optional, same pattern used across every other module). */
    override suspend fun getFeeStructures(): List<FeeStructure> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        val data = view(FeeEndpoint.SubModules.FEE_STRUCTURE, FeeEndpoint.Actions.FEE_STRUCTURE, payload)
        return JsonRowUtils.asRows(data).map { it.toFeeStructure() }
    }

    /** m_fees contract: feeAssignment/view. */
    override suspend fun getMyFeeAssignments(): List<FeeAssignment> {
        val data = view(FeeEndpoint.SubModules.FEE_ASSIGNMENT, FeeEndpoint.Actions.FEE_ASSIGNMENT, studentPayload())
        return JsonRowUtils.asRows(data).map { it.toFeeAssignment() }
    }

    /** m_fees contract: feeConcession/view. */
    override suspend fun getMyFeeConcessions(): List<FeeConcession> {
        val data = view(FeeEndpoint.SubModules.CONCESSION, FeeEndpoint.Actions.FEE_CONCESSION, studentPayload())
        return JsonRowUtils.asRows(data).map { it.toFeeConcession() }
    }

    /** m_fees contract: feeInvoice/view (reads the financial ledger). */
    override suspend fun getMyFeeInvoices(): List<FeeInvoice> {
        val data = view(FeeEndpoint.SubModules.INVOICE, FeeEndpoint.Actions.FEE_INVOICE, studentPayload())
        return JsonRowUtils.asRows(data).map { it.toFeeInvoice() }
    }

    /** m_fees contract: feePayment/view. */
    override suspend fun getMyFeePayments(): List<FeePayment> {
        val data = view(FeeEndpoint.SubModules.PAYMENT, FeeEndpoint.Actions.FEE_PAYMENT, studentPayload())
        return JsonRowUtils.asRows(data).map { it.toFeePayment() }
    }

    /** m_fees contract: feeRefund/view. */
    override suspend fun getMyFeeRefunds(): List<FeeRefund> {
        val data = view(FeeEndpoint.SubModules.REFUND, FeeEndpoint.Actions.FEE_REFUND, studentPayload())
        return JsonRowUtils.asRows(data).map { it.toFeeRefund() }
    }

    /** m_fees contract: feePenalty/view - only inst_id is a documented filter for
     * this one (no studentId), unlike every other view in this module. */
    override suspend fun getFeePenalties(): List<FeePenalty> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        val data = view(FeeEndpoint.SubModules.PENALTY, FeeEndpoint.Actions.FEE_PENALTY, payload)
        return JsonRowUtils.asRows(data).map { it.toFeePenalty() }
    }

    private fun studentPayload(): Map<String, Any> {
        val studentId = sessionManager.getUserId() ?: return emptyMap()
        return mapOf("studentId" to (studentId.toLongOrNull() ?: studentId))
    }
}
