package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ReappearEligibility
import com.xsc.oneapp.feature.exam.domain.model.ReappearExam
import com.xsc.oneapp.feature.exam.domain.model.SupplementaryExam
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** sm_supplementary and sm_reappear - the two re-examination routes a student can see
 * and register against. */

class GetSupplementaryExamsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<SupplementaryExam> = repository.getSupplementaryExams()
}

class RegisterForSupplementaryExamUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(supplementaryExamId: String, courseIds: List<String>) =
        repository.registerForSupplementaryExam(supplementaryExamId, courseIds)
}

class GetReappearExamsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ReappearExam> = repository.getReappearExams()
}

class RegisterForReappearExamUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(reappearExamId: String, courseIds: List<String>) =
        repository.registerForReappearExam(reappearExamId, courseIds)
}

class GetMyReappearEligibilityUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(reappearExamId: String? = null): List<ReappearEligibility> =
        repository.getMyReappearEligibility(reappearExamId)
}
