package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.FacultyAllocation
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFacultyAllocationsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val allocations = listOf(FacultyAllocation("1", "12", "201", "PRIMARY", "100", "true", "Assigned HOD approved"))
        coEvery { repository.getFacultyAllocations() } returns allocations

        val result = GetFacultyAllocationsUseCase(repository).invoke()

        assertEquals(allocations, result)
    }
}
