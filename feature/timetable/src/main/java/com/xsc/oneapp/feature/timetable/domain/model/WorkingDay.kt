package com.xsc.oneapp.feature.timetable.domain.model

data class WorkingDay(
    val id: String?,
    val institutionId: String?,
    val academicYearId: String?,
    val name: String?,
    val effectiveFrom: String?,
    val effectiveTo: String?,
    val dayOfWeekId: String?,
    val dayName: String?,
    val isWorkingDay: String?,
    val isActive: String?
)
