package com.xsc.oneapp.feature.fee.data.repository

import com.google.gson.JsonElement
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.fee.data.mapper.toFeeAssignment
import com.xsc.oneapp.feature.fee.data.mapper.toFeeConcession
import com.xsc.oneapp.feature.fee.data.mapper.toFeeInvoice
import com.xsc.oneapp.feature.fee.data.mapper.toFeePayment
import com.xsc.oneapp.feature.fee.data.mapper.toFeePenalty
import com.xsc.oneapp.feature.fee.data.mapper.toFeeRefund
import com.xsc.oneapp.feature.fee.data.mapper.toFeeStatement
import com.xsc.oneapp.feature.fee.data.mapper.toFeeStructure
import com.xsc.oneapp.feature.fee.data.mapper.toPaymentOrder
import com.xsc.oneapp.feature.fee.data.network.FeeEndpoint
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.model.PaymentOrder
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.APIClient
import javax.inject.Inject

class FeeRepositoryImpl @Inject constructor(
    private val apiClient: APIClient,
    private val sessionManager: SessionManager
) : FeeRepository {

    private suspend fun view(
        submodule: String,
        action: String,
        payload: Map<String, Any>
    ): JsonElement? = apiClient.request(
        module = FeeEndpoint.MODULE,
        submodule = submodule,
        action = action,
        actionType = FeeEndpoint.ActionTypes.VIEW,
        payload = payload
    )

    /** m_fees contract §3.1: feeStructure/view - `inst_id` is the documented filter. */
    override suspend fun getFeeStructures(): List<FeeStructure> {
        val data = view(
            FeeEndpoint.SubModules.FEE_STRUCTURE,
            FeeEndpoint.Actions.FEE_STRUCTURE,
            instPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toFeeStructure() }
    }

    /** m_fees contract §3.2: feeAssignment/view. */
    override suspend fun getMyFeeAssignments(): List<FeeAssignment> {
        val data = view(
            FeeEndpoint.SubModules.FEE_ASSIGNMENT,
            FeeEndpoint.Actions.FEE_ASSIGNMENT,
            studentPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toFeeAssignment() }
    }

    /** m_fees contract §3.3: feeConcession/view. */
    override suspend fun getMyFeeConcessions(): List<FeeConcession> {
        val data = view(
            FeeEndpoint.SubModules.CONCESSION,
            FeeEndpoint.Actions.FEE_CONCESSION,
            studentPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toFeeConcession() }
    }

    /**
     * m_fees contract §3.4: feeInvoice/view.
     *
     * `data` is an object (totalDebits/totalCredits/outstandingBalance), not an array -
     * the array branch below only fires if a deployment returns line rows instead, in
     * which case the totals are summed from them rather than left blank.
     */
    override suspend fun getMyFeeStatement(): FeeStatement {
        val data = view(
            FeeEndpoint.SubModules.INVOICE,
            FeeEndpoint.Actions.FEE_INVOICE,
            studentPayload()
        )

        JsonRowUtils.asObject(data)?.let { return it.toFeeStatement() }

        val lines = JsonRowUtils.asRows(data).map { it.toFeeInvoice() }
        if (lines.isEmpty()) return FeeStatement.EMPTY

        val billed = lines.mapNotNull { it.amount?.toDoubleOrNull() }.sum()
        return FeeStatement(
            studentId = lines.firstNotNullOfOrNull { it.studentId },
            totalDebits = billed,
            totalCredits = null,
            outstandingBalance = null,
            lines = lines
        )
    }

    /** m_fees contract §3.5: feePayment/view. */
    override suspend fun getMyFeePayments(): List<FeePayment> {
        val data = view(
            FeeEndpoint.SubModules.PAYMENT,
            FeeEndpoint.Actions.FEE_PAYMENT,
            studentPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toFeePayment() }
    }

    /** m_fees contract §3.6: feeRefund/view. */
    override suspend fun getMyFeeRefunds(): List<FeeRefund> {
        val data = view(
            FeeEndpoint.SubModules.REFUND,
            FeeEndpoint.Actions.FEE_REFUND,
            studentPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toFeeRefund() }
    }

    /**
     * m_fees contract §3.7: feePenalty/view.
     *
     * v1.3's example payload for this action is `{"studentId": 10}` - the previous
     * build sent only `inst_id`, which is the *write* side's filter, so a student saw
     * either nothing or every student's penalties. Both are sent now: studentId scopes
     * it, inst_id is harmless if the handler ignores it.
     */
    override suspend fun getFeePenalties(): List<FeePenalty> {
        val payload = instPayload()
        payload.putAll(studentPayload())
        val data = view(FeeEndpoint.SubModules.PENALTY, FeeEndpoint.Actions.FEE_PENALTY, payload)
        return JsonRowUtils.asRows(data).map { it.toFeePenalty() }
    }

    /**
     * m_fees contract §3.5, online branch: `feePayment:add` with `method: "ONLINE"`
     * returns a gateway order rather than posting a payment to the ledger.
     *
     * `invoiceId` is documented as required. It is omitted when the caller has none
     * (the statement endpoint does not always return one) rather than sent as a
     * fabricated value - the backend's own validation error is more useful than a
     * wrong id silently paying down somebody else's invoice.
     */
    override suspend fun createOnlinePaymentOrder(
        invoiceId: String?,
        amount: Double,
        paidBy: String
    ): PaymentOrder {
        val payload = mutableMapOf<String, Any>(
            "amount" to amount,
            "method" to FeeEndpoint.PaymentMethods.ONLINE,
            "paidBy" to paidBy
        )
        invoiceId?.let { payload["invoiceId"] = it.toLongOrNull() ?: it }
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        studentId()?.let { payload["studentId"] = it }

        val data = apiClient.request<JsonElement?>(
            module = FeeEndpoint.MODULE,
            submodule = FeeEndpoint.SubModules.PAYMENT,
            action = FeeEndpoint.Actions.FEE_PAYMENT,
            actionType = FeeEndpoint.ActionTypes.ADD,
            payload = payload
        )

        val order = JsonRowUtils.asObject(data)?.toPaymentOrder()
            ?: JsonRowUtils.asRows(data).firstOrNull()?.toPaymentOrder()
            ?: throw APIError.BusinessError(
                "",
                "The server did not return a payment order. Please try again."
            )

        // A zero amount would open Checkout on nothing; fall back to what we asked for.
        return if (order.amountInPaise > 0L) {
            order
        } else {
            order.copy(amountInPaise = Math.round(amount * 100))
        }
    }

    /**
     * m_fees contract §3.5: `feePayment:update`.
     *
     * Deliberately swallows a business/HTTP failure into `false`. By the time this
     * runs the card has already been charged, so throwing here would surface as
     * "payment failed" to a user who has definitely paid. The caller shows a
     * "paid, reconciling" message instead, and the ledger is corrected server side
     * from the gateway webhook.
     */
    override suspend fun confirmOnlinePayment(paymentId: String, succeeded: Boolean): Boolean = try {
        apiClient.request<JsonElement?>(
            module = FeeEndpoint.MODULE,
            submodule = FeeEndpoint.SubModules.PAYMENT,
            action = FeeEndpoint.Actions.FEE_PAYMENT,
            actionType = FeeEndpoint.ActionTypes.UPDATE,
            payload = mapOf(
                "paymentId" to (paymentId.toLongOrNull() ?: paymentId),
                "status" to if (succeeded) {
                    FeeEndpoint.PaymentStatus.COMPLETED
                } else {
                    FeeEndpoint.PaymentStatus.FAILED
                },
                "transactionRef" to paymentId
            )
        )
        true
    } catch (e: APIError) {
        false
    }

    /**
     * `feeRefund:add`. Mirrors [createOnlinePaymentOrder]'s payload style: every
     * identifier the backend might filter/validate on is sent when we have it,
     * nothing is fabricated when we don't. [feeType] is free-form (Academic Fees,
     * Hostel Fees, ...) since no confirmed enum exists for it - see [FeeRefund.feeType].
     */
    override suspend fun requestRefund(
        paymentId: String?,
        amount: Double,
        reason: String,
        feeType: String?
    ) {
        val payload = mutableMapOf<String, Any>(
            "amount" to amount,
            "reason" to reason
        )
        paymentId?.takeIf { it.isNotBlank() }?.let { payload["paymentId"] = it.toLongOrNull() ?: it }
        feeType?.takeIf { it.isNotBlank() }?.let { payload["feeType"] = it }
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        studentId()?.let { payload["studentId"] = it }

        apiClient.request<JsonElement?>(
            module = FeeEndpoint.MODULE,
            submodule = FeeEndpoint.SubModules.REFUND,
            action = FeeEndpoint.Actions.FEE_REFUND,
            actionType = FeeEndpoint.ActionTypes.ADD,
            payload = payload
        )
    }

    private fun instPayload(): MutableMap<String, Any> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        return payload
    }

    /**
     * Every student-scoped `view` in this module takes `studentId` (contract §3.2-§3.7).
     *
     * Caveat worth keeping visible: this is the JWT's user id, and the backend's
     * `studentId` is `tb_student.id`. They coincide on the seeded dev data but are not
     * the same column, and nothing in m_fees resolves one to the other - if a student
     * sees an empty statement on a real tenant, this is the first thing to check.
     */
    private fun studentPayload(): MutableMap<String, Any> {
        val id = studentId() ?: return mutableMapOf()
        return mutableMapOf("studentId" to id)
    }

    private fun studentId(): Any? =
        sessionManager.getUserId()?.let { it.toLongOrNull() ?: it }
}
