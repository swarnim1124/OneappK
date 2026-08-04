package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTimeSlotsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val slots = listOf(TimeSlot("1", "1", "Period 1", "09:00:00", "09:50:00", "1", "false", "true"))
        coEvery { repository.getTimeSlots() } returns slots

        val result = GetTimeSlotsUseCase(repository).invoke()

        assertEquals(slots, result)
    }
}
