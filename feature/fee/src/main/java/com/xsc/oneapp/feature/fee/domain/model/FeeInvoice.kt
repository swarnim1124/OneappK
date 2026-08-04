package com.xsc.oneapp.feature.fee.domain.model

/** Note: per the contract, invoice queries read directly from the financial
 * ledger table - this is a transaction-log entry, not a line-itemized bill. */
data class FeeInvoice(
    val id: String?,
    val studentId: String?,
    val transactionTypeId: String?,
    val transactionDate: String?,
    val amount: String?,
    val referenceId: String?,
    val description: String?
)
