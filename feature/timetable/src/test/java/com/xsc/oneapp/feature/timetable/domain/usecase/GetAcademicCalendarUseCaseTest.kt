package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.AcademicCalendar
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAcademicCalendarUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val calendar = AcademicCalendar("1", "2026", "1", "proxied term data")
        coEvery { repository.getAcademicCalendar() } returns calendar

        val result = GetAcademicCalendarUseCase(repository).invoke()

        assertEquals(calendar, result)
    }
}
