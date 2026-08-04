package com.xsc.oneapp.feature.timetable.domain.model

data class TimeSlot(
    val id: String?,
    val institutionId: String?,
    val slotName: String?,
    val startTime: String?,
    val endTime: String?,
    val slotSequence: String?,
    val isBreak: String?,
    val isActive: String?
)
