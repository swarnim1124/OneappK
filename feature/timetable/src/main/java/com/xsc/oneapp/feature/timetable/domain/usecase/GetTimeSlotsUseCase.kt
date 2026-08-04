package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import javax.inject.Inject

class GetTimeSlotsUseCase @Inject constructor(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): List<TimeSlot> = repository.getTimeSlots()
}
