package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExternalEvaluation
import com.xsc.oneapp.feature.exam.domain.model.ExternalExaminer
import com.xsc.oneapp.feature.exam.domain.model.ExternalPaperSetting
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin CRUD for m_exam contract §5.2 (sm_externalExam): the examiner registry,
 * paper-setting assignments, and evaluation dispatch. */

class GetExternalExaminersUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ExternalExaminer> = repository.getExternalExaminers()
}

class AddExternalExaminerUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        examinerName: String,
        contactEmail: String? = null,
        contactPhone: String? = null,
        specialization: String? = null
    ) = repository.addExternalExaminer(examinerName, contactEmail, contactPhone, specialization)
}

class UpdateExternalExaminerUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        id: String,
        examinerName: String? = null,
        contactEmail: String? = null,
        contactPhone: String? = null,
        specialization: String? = null
    ) = repository.updateExternalExaminer(id, examinerName, contactEmail, contactPhone, specialization)
}

class DeleteExternalExaminerUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteExternalExaminer(id)
}

class GetExternalPaperSettingsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ExternalPaperSetting> = repository.getExternalPaperSettings()
}

class AddExternalPaperSettingUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(examinerId: String, courseId: String, scheduleId: String) =
        repository.addExternalPaperSetting(examinerId, courseId, scheduleId)
}

class DeleteExternalPaperSettingUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteExternalPaperSetting(id)
}

class GetExternalEvaluationsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ExternalEvaluation> = repository.getExternalEvaluations()
}

class AddExternalEvaluationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        examinerId: String,
        courseId: String,
        scheduleId: String,
        scriptBundleIds: List<String>
    ) = repository.addExternalEvaluation(examinerId, courseId, scheduleId, scriptBundleIds)
}

class UpdateExternalEvaluationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String, status: String? = null) =
        repository.updateExternalEvaluation(id, status)
}

class DeleteExternalEvaluationUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteExternalEvaluation(id)
}
