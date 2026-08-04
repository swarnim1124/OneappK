package com.xsc.oneapp.feature.fee.domain.model

data class FeeConcession(
    val id: String?,
    val studentId: String?,
    val concessionTypeId: String?,
    val amount: String?,
    val percentage: String?,
    val reason: String?,
    val statusId: String?,
    val approvedBy: String?,
    val approvedOn: String?
)
