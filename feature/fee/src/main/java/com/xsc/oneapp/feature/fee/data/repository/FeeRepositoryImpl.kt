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
import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.APIError
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

    private suspend fun add(submodule: String, action: String, payload: Map<String, Any>): JsonElement? =
        apiClient.request(
            module = FeeEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = FeeEndpoint.ActionTypes.ADD,
            payload = payload
        )

    private suspend fun update(submodule: String, action: String, payload: Map<String, Any>) {
        apiClient.request<JsonElement?>(
            module = FeeEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = FeeEndpoint.ActionTypes.UPDATE,
            payload = payload
        )
    }

    private suspend fun delete(submodule: String, action: String, payload: Map<String, Any>) {
        apiClient.request<JsonElement?>(
            module = FeeEndpoint.MODULE,
            submodule = submodule,
            action = action,
            actionType = FeeEndpoint.ActionTypes.DELETE,
            payload = payload
        )
    }

    /** Contract payloads show ids as bare JSON numbers, not strings. */
    private fun String.asIdPayload(): Any = toLongOrNull() ?: this

    private fun requireStudentId(): Any {
        val id = sessionManager.getUserId()
            ?: throw APIError.BusinessError("NO_ID", "No signed-in student for this request")
        return id.toLongOrNull() ?: id
    }

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

    /** m_fees contract §3.1: feeStructure/add - inst_id defaulted from the session
     * the same defensive-optional way every other inst_id payload in this module is.
     * Returns the new structure's id when the response includes one. */
    override suspend fun createFeeStructure(
        code: String,
        name: String,
        academicYearId: String,
        programmeId: String,
        components: List<FeeStructureComponent>,
        effectiveFrom: String,
        effectiveTo: String?
    ): String? {
        val payload = mutableMapOf<String, Any>(
            "feeStructureCode" to code,
            "feeStructureName" to name,
            "academicYearId" to academicYearId.asIdPayload(),
            "programmeId" to programmeId.asIdPayload(),
            "components" to components.map { component ->
                mapOf(
                    "headCode" to component.headCode,
                    "headName" to component.headName,
                    "amount" to component.amount,
                    "isOptional" to component.isOptional
                )
            },
            "effectiveFrom" to effectiveFrom
        )
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        effectiveTo?.takeIf { it.isNotBlank() }?.let { payload["effectiveTo"] = it }

        val data = add(FeeEndpoint.SubModules.FEE_STRUCTURE, FeeEndpoint.Actions.FEE_STRUCTURE, payload)
        return JsonRowUtils.asRows(data).firstOrNull()
            ?.let { JsonRowUtils.firstString(it, "fee_structure_id", "feeStructureId") }
    }

    /** m_fees contract §3.1: feeStructure/update. The contract's documented example
     * only shows a status transition (e.g. "ACTIVE") - scope/component edits are not
     * built since no example payload for them exists to build against. */
    override suspend fun updateFeeStructureStatus(structureId: String, status: String) {
        update(
            FeeEndpoint.SubModules.FEE_STRUCTURE,
            FeeEndpoint.Actions.FEE_STRUCTURE,
            mapOf("feeStructureId" to structureId.asIdPayload(), "status" to status)
        )
    }

    /** m_fees contract §3.1: feeStructure/delete - deactivates, doesn't hard-delete
     * (see the contract's own "Deactivated successfully" response message). */
    override suspend fun deleteFeeStructure(structureId: String) {
        delete(
            FeeEndpoint.SubModules.FEE_STRUCTURE,
            FeeEndpoint.Actions.FEE_STRUCTURE,
            mapOf("feeStructureId" to structureId.asIdPayload())
        )
    }

    /** m_fees contract §3.2: feeAssignment/add - sends studentId for a single
     * target and studentIds for more than one, matching the contract's "either/or"
     * schema rather than always sending an array. */
    override suspend fun assignFee(
        feeStructureId: String,
        termId: String,
        dueDate: String,
        studentIds: List<String>
    ) {
        val payload = mutableMapOf<String, Any>(
            "feeStructureId" to feeStructureId.asIdPayload(),
            "termId" to termId.asIdPayload(),
            "dueDate" to dueDate
        )
        if (studentIds.size == 1) {
            payload["studentId"] = studentIds.first().asIdPayload()
        } else {
            payload["studentIds"] = studentIds.map { it.asIdPayload() }
        }
        add(FeeEndpoint.SubModules.FEE_ASSIGNMENT, FeeEndpoint.Actions.FEE_ASSIGNMENT, payload)
    }

    /** m_fees contract §3.2: feeAssignment/delete - deactivates the assessment. */
    override suspend fun deleteFeeAssignment(assignmentId: String) {
        delete(
            FeeEndpoint.SubModules.FEE_ASSIGNMENT,
            FeeEndpoint.Actions.FEE_ASSIGNMENT,
            mapOf("assignmentId" to assignmentId.asIdPayload())
        )
    }

    /** m_fees contract §3.3: feeConcession/add. */
    override suspend fun grantConcession(
        studentId: String,
        assignmentId: String,
        concessionType: String,
        amountOrPercent: Double,
        reason: String
    ) {
        add(
            FeeEndpoint.SubModules.CONCESSION,
            FeeEndpoint.Actions.FEE_CONCESSION,
            mapOf(
                "studentId" to studentId.asIdPayload(),
                "assignmentId" to assignmentId.asIdPayload(),
                "concessionType" to concessionType,
                "amountOrPercent" to amountOrPercent,
                "reason" to reason
            )
        )
    }

    /** m_fees contract §3.5: feePayment/add with method=ONLINE - returns a
     * Razorpay order object rather than the plain success message the CASH/CHQ
     * path gets, so this parses `data` directly instead of reusing [add]'s
     * discard-the-body callers. */
    override suspend fun initiateOnlinePayment(invoiceId: String, amount: Double, paidBy: String): RazorpayOrder {
        val data = apiClient.request<JsonElement?>(
            module = FeeEndpoint.MODULE,
            submodule = FeeEndpoint.SubModules.PAYMENT,
            action = FeeEndpoint.Actions.FEE_PAYMENT,
            actionType = FeeEndpoint.ActionTypes.ADD,
            payload = mapOf(
                "invoiceId" to invoiceId.asIdPayload(),
                "amount" to amount,
                "method" to "ONLINE",
                "paidBy" to paidBy
            )
        )
        val order = JsonRowUtils.asRows(data).firstOrNull()
            ?: throw APIError.BusinessError("NO_ORDER", "Payment gateway did not return an order")
        val id = JsonRowUtils.firstString(order, "id")
            ?: throw APIError.BusinessError("NO_ORDER", "Payment gateway order is missing an id")
        val orderAmount = JsonRowUtils.firstString(order, "amount")?.toLongOrNull()
            ?: throw APIError.BusinessError("NO_ORDER", "Payment gateway order is missing an amount")
        val currency = JsonRowUtils.firstString(order, "currency") ?: "INR"
        return RazorpayOrder(id = id, amount = orderAmount, currency = currency)
    }

    /** m_fees contract §3.6: feeRefund/add - studentId resolved from the session. */
    override suspend fun requestRefund(invoiceId: String, amount: Double, reason: String) {
        add(
            FeeEndpoint.SubModules.REFUND,
            FeeEndpoint.Actions.FEE_REFUND,
            mapOf(
                "studentId" to requireStudentId(),
                "invoiceId" to invoiceId.asIdPayload(),
                "amount" to amount,
                "reason" to reason
            )
        )
    }

    /** m_fees contract §3.6: feeRefund/update - admin approve/reject. */
    override suspend fun updateRefundStatus(refundId: String, status: String, remarks: String?) {
        val payload = mutableMapOf<String, Any>(
            "refundId" to refundId.asIdPayload(),
            "status" to status
        )
        remarks?.takeIf { it.isNotBlank() }?.let { payload["remarks"] = it }
        update(FeeEndpoint.SubModules.REFUND, FeeEndpoint.Actions.FEE_REFUND, payload)
    }

    private fun studentPayload(): Map<String, Any> {
        val studentId = sessionManager.getUserId() ?: return emptyMap()
        return mapOf("studentId" to (studentId.toLongOrNull() ?: studentId))
    }
}
