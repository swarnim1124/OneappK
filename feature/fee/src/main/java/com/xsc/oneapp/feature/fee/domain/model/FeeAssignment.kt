package com.xsc.oneapp.feature.fee.domain.model

data class FeeAssignment(
    val id: String?,
    val studentId: String?,
    val feeStructureId: String?,
    val termId: String?,
    val totalAmount: String?,
    val dueDate: String?,
    val statusId: String?,
    val isActive: String?
)
