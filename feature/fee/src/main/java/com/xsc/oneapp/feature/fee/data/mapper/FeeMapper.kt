package com.xsc.oneapp.feature.fee.data.mapper

import com.google.gson.JsonObject
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.model.PaymentOrder

/**
 * m_fees rows are schema-less dictionaries (same pattern as m_attendance/m_curriculum),
 * so every field is read through a candidate-key list rather than a fixed DTO.
 *
 * Corrected 2026-08-14 against m_fees_api_contract.md v1.3, whose documented `view`
 * responses use camelCase business keys (`paymentId`, `concessionId`, `penaltyValue`,
 * `sanctionDate`, ...) rather than the raw ORM column names (`id`, `stud_id`,
 * `applied_date`, ...) this mapper had been written against. Every entity here was
 * mapping most of its fields to null as a result - Payments showed no reference,
 * Concessions no type or date, Penalties no amount at all.
 *
 * The old snake_case candidates are deliberately kept ahead of / alongside the new
 * ones: which set a given deployment returns depends on whether the action goes
 * through a Pydantic response model or dumps the SQLAlchemy row, and both are in
 * flight. Trying both costs one map lookup and removes a whole class of silent
 * blank-screen bug.
 */
fun JsonObject.toFeeStructure(): FeeStructure = FeeStructure(
    id = JsonRowUtils.firstString(this, "id", "fee_structure_id", "feeStructureId"),
    code = JsonRowUtils.firstString(this, "fee_structure_code", "feeStructureCode", "code"),
    name = JsonRowUtils.firstString(this, "fee_structure_name", "feeStructureName", "name"),
    description = JsonRowUtils.firstString(this, "description"),
    institutionId = JsonRowUtils.firstString(this, "inst_id", "institution_id", "institutionId"),
    academicYearId = JsonRowUtils.firstString(this, "acad_year_id", "academicYearId"),
    effectiveFrom = JsonRowUtils.firstString(this, "effective_from", "effectiveFrom"),
    effectiveTo = JsonRowUtils.firstString(this, "effective_to", "effectiveTo"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId", "status"),
    isActive = JsonRowUtils.firstString(this, "is_active", "isActive")
)

fun JsonObject.toFeeAssignment(): FeeAssignment = FeeAssignment(
    id = JsonRowUtils.firstString(this, "id", "stud_asmt_id", "studAsmtId", "assignmentId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    feeStructureId = JsonRowUtils.firstString(this, "fee_structure_id", "feeStructureId"),
    termId = JsonRowUtils.firstString(this, "term_id", "termId"),
    totalAmount = JsonRowUtils.firstString(this, "total_amount", "totalAmount", "amount"),
    dueDate = JsonRowUtils.firstString(this, "due_date", "dueDate"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId", "status"),
    isActive = JsonRowUtils.firstString(this, "is_active", "isActive")
)

fun JsonObject.toFeeConcession(): FeeConcession = FeeConcession(
    id = JsonRowUtils.firstString(this, "id", "concession_id", "concessionId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    concessionTypeId = JsonRowUtils.firstString(
        this, "concession_type_id", "concessionTypeId", "concession_type", "concessionType"
    ),
    amount = JsonRowUtils.firstString(this, "amount", "amountOrPercent"),
    percentage = JsonRowUtils.firstString(this, "percentage", "percent"),
    reason = JsonRowUtils.firstString(this, "reason"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId", "status"),
    approvedBy = JsonRowUtils.firstString(this, "approved_by", "approvedBy", "sanctionedBy"),
    approvedOn = JsonRowUtils.firstString(
        this, "approved_on", "approvedOn", "sanction_date", "sanctionDate"
    )
)

/** A statement line, when one is returned. See [toFeeStatement] for the usual case. */
fun JsonObject.toFeeInvoice(): FeeInvoice = FeeInvoice(
    id = JsonRowUtils.firstString(this, "id", "invoice_id", "invoiceId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    transactionTypeId = JsonRowUtils.firstString(
        this, "transaction_type_id", "transactionTypeId", "transactionType", "headCode"
    ),
    transactionDate = JsonRowUtils.firstString(
        this, "transaction_date", "transactionDate", "invoiceDate", "createdOn"
    ),
    amount = JsonRowUtils.firstString(this, "amount", "totalAmount"),
    referenceId = JsonRowUtils.firstString(this, "reference_id", "referenceId", "reference"),
    description = JsonRowUtils.firstString(this, "description", "headName", "narration"),
    dueDate = JsonRowUtils.firstString(this, "due_date", "dueDate"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId", "status")
)

/**
 * The real shape of `feeInvoice:view` - one aggregate object per student.
 *
 * Line items are picked up if the backend nests them (`lines` / `items` /
 * `transactions` / `invoices`), but none are invented when it does not: an empty
 * [FeeStatement.lines] with real totals renders correctly, a fabricated line does not.
 */
fun JsonObject.toFeeStatement(): FeeStatement = FeeStatement(
    studentId = JsonRowUtils.firstString(this, "studentId", "stud_id", "student_id"),
    totalDebits = JsonRowUtils.firstDouble(this, "totalDebits", "total_debits", "totalBilled"),
    totalCredits = JsonRowUtils.firstDouble(this, "totalCredits", "total_credits", "totalPaid"),
    outstandingBalance = JsonRowUtils.firstDouble(
        this, "outstandingBalance", "outstanding_balance", "balance", "dueAmount"
    ),
    lines = JsonRowUtils
        .firstRowArray(this, "lines", "items", "transactions", "invoices", "data")
        .map { it.toFeeInvoice() }
)

fun JsonObject.toFeePayment(): FeePayment = FeePayment(
    id = JsonRowUtils.firstString(this, "id", "payment_id", "paymentId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    paymentMethodId = JsonRowUtils.firstString(this, "payment_method_id", "paymentMethodId"),
    paymentMode = JsonRowUtils.firstString(
        this, "payment_mode", "paymentMode", "method", "paymentMethod"
    ),
    amount = JsonRowUtils.firstString(this, "amount"),
    paymentDate = JsonRowUtils.firstString(this, "payment_date", "paymentDate"),
    transactionReference = JsonRowUtils.firstString(
        this, "transaction_reference", "transactionReference", "transactionRef",
        "receipt_number", "receiptNumber"
    ),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId", "status")
)

fun JsonObject.toFeeRefund(): FeeRefund = FeeRefund(
    id = JsonRowUtils.firstString(this, "id", "refund_id", "refundId"),
    paymentId = JsonRowUtils.firstString(this, "payment_id", "paymentId"),
    amount = JsonRowUtils.firstString(this, "amount"),
    reason = JsonRowUtils.firstString(this, "reason"),
    refundDate = JsonRowUtils.firstString(this, "refund_date", "refundDate"),
    statusId = JsonRowUtils.firstString(this, "status_id", "statusId", "status"),
    approvedBy = JsonRowUtils.firstString(this, "approved_by", "approvedBy"),
    feeType = JsonRowUtils.firstString(
        this, "fee_type", "feeType", "type", "category", "refund_type", "refundType",
        "head_code", "headCode", "transaction_type_id", "transactionTypeId"
    )
)

fun JsonObject.toFeePenalty(): FeePenalty = FeePenalty(
    id = JsonRowUtils.firstString(this, "id", "penalty_id", "penaltyId"),
    studentId = JsonRowUtils.firstString(this, "stud_id", "studentId"),
    feeAssignmentId = JsonRowUtils.firstString(this, "fee_asmt_id", "feeAssignmentId", "assignmentId"),
    // v1.3 calls the money field penaltyValue; the ORM row called it amount.
    amount = JsonRowUtils.firstString(this, "amount", "penalty_value", "penaltyValue"),
    reason = JsonRowUtils.firstString(this, "reason"),
    appliedDate = JsonRowUtils.firstString(
        this, "applied_date", "appliedDate", "penalty_date", "penaltyDate"
    ),
    statusId = JsonRowUtils.firstString(
        this, "status_id", "statusId", "status", "penalty_type", "penaltyType"
    )
)

/** `feePayment:add` (`method: "ONLINE"`) response - a gateway order, not a payment. */
fun JsonObject.toPaymentOrder(): PaymentOrder = PaymentOrder(
    orderId = JsonRowUtils.firstString(this, "id", "order_id", "orderId"),
    amountInPaise = JsonRowUtils.firstDouble(this, "amount")?.toLong() ?: 0L,
    currency = JsonRowUtils.firstString(this, "currency") ?: "INR",
    backendPaymentId = JsonRowUtils.firstString(this, "paymentId", "payment_id")
)
