package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.WorkingDay
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import javax.inject.Inject

class GetWorkingDaysUseCase @Inject constructor(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): List<WorkingDay> = repository.getWorkingDays()
}
