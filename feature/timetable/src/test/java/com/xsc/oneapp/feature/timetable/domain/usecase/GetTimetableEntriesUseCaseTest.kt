package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTimetableEntriesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val entries = listOf(
            TimetableEntry(
                "1", "5", "1", "2026", "1", "10", "2", "3", "CS101", "1", "201", "201",
                "1", "MONDAY", "2", "50", "LECTURE", "2026-01-01", "2026-06-30", "true",
                "TT_SEC3_TERM1", "PUBLISHED"
            )
        )
        coEvery { repository.getTimetableEntries() } returns entries

        val result = GetTimetableEntriesUseCase(repository).invoke()

        assertEquals(entries, result)
    }
}
