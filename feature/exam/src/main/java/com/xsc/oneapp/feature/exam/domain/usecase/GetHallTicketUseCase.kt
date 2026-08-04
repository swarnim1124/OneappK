package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

class GetHallTicketUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String): List<HallTicket> = repository.getHallTicket(scheduleId)
}
