package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

class GetChallengeRevaluationsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ChallengeRevaluation> = repository.getMyChallengeRevaluations()
}
