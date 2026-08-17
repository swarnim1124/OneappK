package com.xsc.oneapp.feature.fee.domain.model

/**
 * One line of a student's fee statement.
 *
 * m_fees v1.3 `feeInvoice:view` returns an aggregate statement object rather than a
 * line array (see [FeeStatement]), so in practice this list is usually empty and the
 * Invoices tab renders the aggregate. It is still mapped because the same action
 * returns rows when the backend is asked for a specific `invoiceId`, and because the
 * previous build assumed rows unconditionally - which is exactly why the Invoices tab
 * showed a single blank card instead of anything readable.
 */
data class FeeInvoice(
    val id: String?,
    val studentId: String?,
    val transactionTypeId: String?,
    val transactionDate: String?,
    val amount: String?,
    val referenceId: String?,
    val description: String?,
    val dueDate: String? = null,
    val statusId: String? = null
)
