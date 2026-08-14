package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.SeatingPlan
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin CRUD for m_exam contract §3.4 (sm_seating). Admin-only - no student view exists. */

class GetSeatingPlansUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String? = null): List<SeatingPlan> =
        repository.getSeatingPlans(scheduleId)
}

class AddSeatingPlanUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        scheduleId: String,
        venueId: String,
        courseId: String,
        studentId: String,
        seatNumber: String,
        allocationMethod: String? = null
    ) = repository.addSeatingPlan(scheduleId, venueId, courseId, studentId, seatNumber, allocationMethod)
}

class UpdateSeatingPlanUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, seatNumber: String? = null, venueId: String? = null) =
        repository.updateSeatingPlan(id, seatNumber, venueId)
}

class DeleteSeatingPlanUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteSeatingPlan(id)
}
