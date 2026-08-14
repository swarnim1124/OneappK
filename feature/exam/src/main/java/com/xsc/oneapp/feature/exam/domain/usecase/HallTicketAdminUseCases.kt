package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin actions for m_exam contract §4 (sm_hallTicket): bulk generation, the
 * publish/hold/unpublish workflow, and the exam-block roster - distinct from
 * [GetMyExamBlocksUseCase], which is always scoped to the signed-in student. */

class GenerateHallTicketUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        scheduleId: String,
        studentId: String,
        venueDetails: String? = null,
        seatNumber: String? = null
    ) = repository.generateHallTicket(scheduleId, studentId, venueDetails, seatNumber)
}

class UpdateHallTicketAdminUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, venueDetails: String? = null, seatNumber: String? = null) =
        repository.updateHallTicketAdmin(id, venueDetails, seatNumber)
}

class DeleteHallTicketAdminUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteHallTicketAdmin(id)
}

class PublishHallTicketsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.publishHallTickets(scheduleId)
}

class HoldHallTicketsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.holdHallTickets(scheduleId)
}

class UnpublishHallTicketsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.unpublishHallTickets(scheduleId)
}

class GetExamBlocksAdminUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String? = null): List<StudentExamBlock> =
        repository.getExamBlocksAdmin(scheduleId)
}

class AddStudentExamBlockUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(studentId: String, scheduleId: String, reason: String) =
        repository.addStudentExamBlock(studentId, scheduleId, reason)
}

class UpdateStudentExamBlockUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, reason: String? = null, status: String? = null) =
        repository.updateStudentExamBlock(id, reason, status)
}

class DeleteStudentExamBlockUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteStudentExamBlock(id)
}
