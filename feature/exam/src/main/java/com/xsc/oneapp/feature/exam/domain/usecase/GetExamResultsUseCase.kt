package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

class GetExamResultsUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ExamResult> = repository.getMyResults()
}
