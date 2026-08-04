package com.xsc.oneapp.feature.fee.data.mapper

import com.google.gson.JsonObject
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure

/** m_fees rows are raw SQLAlchemy-model dictionaries, schema-less like
 * m_attendance/m_curriculum - see JsonRowUtils. Field names verified against
 * real response examples (2026-07-30). */
fun JsonObject.toFeeStructure(): FeeStructure = FeeStructure(
    id = JsonRowUtils.firstString(this, "id"),
    code = JsonRowUtils.firstString(this, "fee_structure_code", "feeStructureCode"),
    name = JsonRowUtils.firstString(this, "fee_structure_name", "feeStructureName"),
    description = JsonRowUtils.firstString(this, "description"),
    institutionId = JsonRowUtils.firstString(this, "institution_id", "institutionId"),
    academicYearId = JsonRowUtils.firstString(this, "acad_year_id", "academicYearId"),
    effectiveFrom = JsonRowUtils.firstString(this, "effective_from", "effectiveFrom"),
    effectiveTo = JsonRowUtils.firstString(this, "effective_to", "effectiveTo"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId"),
    isActive = JsonRowUtils.firstString(this, "is_active", "isActive")
)

fun JsonObject.toFeeAssignment(): FeeAssignment = FeeAssignment(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    feeStructureId = JsonRowUtils.firstString(this, "fee_structure_id", "feeStructureId"),
    termId = JsonRowUtils.firstString(this, "term_id", "termId"),
    totalAmount = JsonRowUtils.firstString(this, "total_amount", "totalAmount"),
    dueDate = JsonRowUtils.firstString(this, "due_date", "dueDate"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId"),
    isActive = JsonRowUtils.firstString(this, "is_active", "isActive")
)

fun JsonObject.toFeeConcession(): FeeConcession = FeeConcession(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    concessionTypeId = JsonRowUtils.firstString(this, "concession_type_id", "concessionTypeId"),
    amount = JsonRowUtils.firstString(this, "amount"),
    percentage = JsonRowUtils.firstString(this, "percentage"),
    reason = JsonRowUtils.firstString(this, "reason"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId"),
    approvedBy = JsonRowUtils.firstString(this, "approved_by", "approvedBy"),
    approvedOn = JsonRowUtils.firstString(this, "approved_on", "approvedOn")
)

fun JsonObject.toFeeInvoice(): FeeInvoice = FeeInvoice(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    transactionTypeId = JsonRowUtils.firstString(this, "transaction_type_id", "transactionTypeId"),
    transactionDate = JsonRowUtils.firstString(this, "transaction_date", "transactionDate"),
    amount = JsonRowUtils.firstString(this, "amount"),
    referenceId = JsonRowUtils.firstString(this, "reference_id", "referenceId"),
    description = JsonRowUtils.firstString(this, "description")
)

fun JsonObject.toFeePayment(): FeePayment = FeePayment(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    paymentMethodId = JsonRowUtils.firstString(this, "payment_method_id", "paymentMethodId"),
    paymentMode = JsonRowUtils.firstString(this, "payment_mode", "paymentMode"),
    amount = JsonRowUtils.firstString(this, "amount"),
    paymentDate = JsonRowUtils.firstString(this, "payment_date", "paymentDate"),
    transactionReference = JsonRowUtils.firstString(this, "transaction_reference", "transactionReference"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId")
)

fun JsonObject.toFeeRefund(): FeeRefund = FeeRefund(
    id = JsonRowUtils.firstString(this, "id"),
    paymentId = JsonRowUtils.firstString(this, "payment_id", "paymentId"),
    amount = JsonRowUtils.firstString(this, "amount"),
    reason = JsonRowUtils.firstString(this, "reason"),
    refundDate = JsonRowUtils.firstString(this, "refund_date", "refundDate"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId"),
    approvedBy = JsonRowUtils.firstString(this, "approved_by", "approvedBy")
)

fun JsonObject.toFeePenalty(): FeePenalty = FeePenalty(
    id = JsonRowUtils.firstString(this, "id"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    feeAssignmentId = JsonRowUtils.firstString(this, "fee_asmt_id", "feeAssignmentId"),
    amount = JsonRowUtils.firstString(this, "amount"),
    reason = JsonRowUtils.firstString(this, "reason"),
    appliedDate = JsonRowUtils.firstString(this, "applied_date", "appliedDate"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId")
)
