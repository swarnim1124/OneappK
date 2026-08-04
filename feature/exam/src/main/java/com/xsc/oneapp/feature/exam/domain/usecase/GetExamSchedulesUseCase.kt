package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

class GetExamSchedulesUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(): List<ExamSchedule> = repository.getExamSchedules()
}
