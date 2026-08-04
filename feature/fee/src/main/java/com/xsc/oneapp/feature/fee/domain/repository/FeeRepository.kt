package com.xsc.oneapp.feature.fee.domain.repository

import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure

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
}
