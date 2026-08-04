package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.AcademicCalendar
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import javax.inject.Inject

class GetAcademicCalendarUseCase @Inject constructor(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): AcademicCalendar? = repository.getAcademicCalendar()
}
