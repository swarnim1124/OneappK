package com.xsc.oneapp.feature.exam.domain.model

data class HallTicket(
    val id: String?,
    val scheduleId: String?,
    val studentId: String?,
    val venueDetails: String?,
    val seatNumber: String?,
    val status: String?
)
