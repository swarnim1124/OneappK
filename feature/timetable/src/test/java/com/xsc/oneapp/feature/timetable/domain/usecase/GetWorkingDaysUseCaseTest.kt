package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.WorkingDay
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetWorkingDaysUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val days = listOf(WorkingDay("1", "1", "2026", "Standard Week", "2026-01-01", "2026-12-31", "1", "MONDAY", "true", "true"))
        coEvery { repository.getWorkingDays() } returns days

        val result = GetWorkingDaysUseCase(repository).invoke()

        assertEquals(days, result)
    }
}
