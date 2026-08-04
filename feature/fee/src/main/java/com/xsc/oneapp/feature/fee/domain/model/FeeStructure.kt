package com.xsc.oneapp.feature.fee.domain.model

data class FeeStructure(
    val id: String?,
    val code: String?,
    val name: String?,
    val description: String?,
    val institutionId: String?,
    val academicYearId: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val statusId: String?,
    val isActive: String?
)
