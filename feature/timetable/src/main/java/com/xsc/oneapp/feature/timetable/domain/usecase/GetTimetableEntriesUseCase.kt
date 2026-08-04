package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import javax.inject.Inject

class GetTimetableEntriesUseCase @Inject constructor(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): List<TimetableEntry> = repository.getTimetableEntries()
}
