package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.InvigilatorAssignment
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin CRUD for m_exam contract §3.5 (sm_invigilation). */

class GetInvigilatorAssignmentsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String? = null): List<InvigilatorAssignment> =
        repository.getInvigilatorAssignments(scheduleId)
}

class AddInvigilatorAssignmentUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        scheduleId: String,
        venueId: String,
        facultyId: String,
        dutyType: String
    ) = repository.addInvigilatorAssignment(scheduleId, venueId, facultyId, dutyType)
}

class UpdateInvigilatorAssignmentUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, dutyType: String? = null, status: String? = null) =
        repository.updateInvigilatorAssignment(id, dutyType, status)
}

class DeleteInvigilatorAssignmentUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteInvigilatorAssignment(id)
}
