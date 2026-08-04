package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

class GetRevaluationRequestsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<RevaluationRequest> = repository.getMyRevaluationRequests()
}
