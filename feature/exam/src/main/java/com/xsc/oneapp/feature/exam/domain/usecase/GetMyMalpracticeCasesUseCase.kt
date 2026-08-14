package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.MalpracticeCase
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

class GetMyMalpracticeCasesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<MalpracticeCase> = repository.getMyMalpracticeCases()
}
