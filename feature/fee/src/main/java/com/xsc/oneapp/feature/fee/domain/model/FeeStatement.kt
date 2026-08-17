package com.xsc.oneapp.feature.fee.domain.model

/**
 * What `feeInvoice:view` actually returns (m_fees contract v1.3 §3.4): a single
 * computed statement for one student, not a ledger row list.
 *
 * ```json
 * { "studentId": 10, "totalDebits": 50000.0, "totalCredits": 10000.0,
 *   "outstandingBalance": 40000.0 }
 * ```
 *
 * The previous client modelled this action as a list of transaction rows keyed on
 * `stud_id` / `transaction_date` / `reference_id` - none of which exist in that
 * response - so every field mapped to null and the Invoices tab rendered one empty
 * card. That is the "I can't see invoices" symptom.
 *
 * [lines] is populated only when the backend returns line items (either as a nested
 * array or as a row array for a specific invoiceId); it is not synthesised.
 */
data class FeeStatement(
    val studentId: String?,
    val totalDebits: Double?,
    val totalCredits: Double?,
    val outstandingBalance: Double?,
    val lines: List<FeeInvoice> = emptyList()
) {
    /** True when the backend returned nothing usable - lets the tab show a real empty
     * state instead of a card full of dashes. */
    val isEmpty: Boolean
        get() = totalDebits == null && totalCredits == null &&
            outstandingBalance == null && lines.isEmpty()

    /** Amount the student still owes, or null when the backend did not say. Drives
     * whether "Pay now" is offered at all. */
    val payableAmount: Double?
        get() = outstandingBalance?.takeIf { it > 0.0 }

    companion object {
        val EMPTY = FeeStatement(null, null, null, null, emptyList())
    }
}
