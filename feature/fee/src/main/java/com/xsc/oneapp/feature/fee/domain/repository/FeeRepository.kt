package com.xsc.oneapp.feature.fee.domain.repository

import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder

interface FeeRepository {
    suspend fun getFeeStructures(): List<FeeStructure>
    suspend fun getMyFeeAssignments(): List<FeeAssignment>
    suspend fun getMyFeeConcessions(): List<FeeConcession>
    suspend fun getMyFeeInvoices(): List<FeeInvoice>
    suspend fun getMyFeePayments(): List<FeePayment>
    suspend fun getMyFeeRefunds(): List<FeeRefund>

    /** No studentId filter is documented for this action's view payload (only
     * inst_id) - see FeeNotes.kt on the feePenalty policy-vs-record ambiguity. */
    suspend fun getFeePenalties(): List<FeePenalty>

    // --- Admin: Fee Structure (m_fees contract §3.1) ---

    suspend fun createFeeStructure(
        code: String,
        name: String,
        academicYearId: String,
        programmeId: String,
        components: List<FeeStructureComponent>,
        effectiveFrom: String,
        effectiveTo: String?
    ): String?

    suspend fun updateFeeStructureStatus(structureId: String, status: String)
    suspend fun deleteFeeStructure(structureId: String)

    // --- Admin: Fee Assignment (m_fees contract §3.2) ---

    /** [studentIds] carries every target - the contract accepts a bare `studentId`
     * or a `studentIds[]` array; this always sends whichever shape matches the
     * caller-supplied count rather than exposing two overloads. */
    suspend fun assignFee(feeStructureId: String, termId: String, dueDate: String, studentIds: List<String>)

    suspend fun deleteFeeAssignment(assignmentId: String)

    // --- Admin: Fee Concession (m_fees contract §3.3) ---

    suspend fun grantConcession(
        studentId: String,
        assignmentId: String,
        concessionType: String,
        amountOrPercent: Double,
        reason: String
    )

    // --- Fee Payment (m_fees contract §3.5) ---

    /** Online-only by design - CASH/CHQ collection is an in-person accounts-office
     * counter action, not something this student-facing app records on someone
     * else's behalf. */
    suspend fun initiateOnlinePayment(invoiceId: String, amount: Double, paidBy: String): RazorpayOrder

    // --- Fee Refund (m_fees contract §3.6) ---

    /** studentId is resolved from the signed-in session, not caller-supplied - a
     * student requests a refund for themselves, never types their own id in. */
    suspend fun requestRefund(invoiceId: String, amount: Double, reason: String)

    /** Admin approve/reject - [status] must be "APPROVED" or "REJECTED" per contract. */
    suspend fun updateRefundStatus(refundId: String, status: String, remarks: String?)
}
