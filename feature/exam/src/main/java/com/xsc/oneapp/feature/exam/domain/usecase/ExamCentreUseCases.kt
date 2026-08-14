package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamCentre
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin CRUD for m_exam contract §3.1 (sm_examCenter). */

class GetExamCentresUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ExamCentre> = repository.getExamCentres()
}

class AddExamCentreUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        centreName: String,
        capacity: String,
        address: String?,
        courses: List<String>
    ) = repository.addExamCentre(centreName, capacity, address, courses)
}

class UpdateExamCentreUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        centreId: String,
        centreName: String? = null,
        capacity: String? = null,
        address: String? = null,
        courses: List<String>? = null,
        status: String? = null
    ) = repository.updateExamCentre(centreId, centreName, capacity, address, courses, status)
}

class DeleteExamCentreUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(centreId: String) = repository.deleteExamCentre(centreId)
}
