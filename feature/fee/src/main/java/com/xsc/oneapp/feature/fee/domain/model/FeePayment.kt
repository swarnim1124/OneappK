package com.xsc.oneapp.feature.fee.domain.model

data class FeePayment(
    val id: String?,
    val studentId: String?,
    val paymentMethodId: String?,
    val paymentMode: String?,
    val amount: String?,
    val paymentDate: String?,
    val transactionReference: String?,
    val statusId: String?
)
