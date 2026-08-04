package com.xsc.oneapp.feature.fee.domain.model

data class FeeRefund(
    val id: String?,
    val paymentId: String?,
    val amount: String?,
    val reason: String?,
    val refundDate: String?,
    val statusId: String?,
    val approvedBy: String?
)
