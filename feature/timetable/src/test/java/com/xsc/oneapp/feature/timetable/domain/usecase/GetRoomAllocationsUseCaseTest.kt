package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.RoomAllocation
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRoomAllocationsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val allocations = listOf(RoomAllocation("1", "5", "50", "MONDAY", "2"))
        coEvery { repository.getRoomAllocations() } returns allocations

        val result = GetRoomAllocationsUseCase(repository).invoke()

        assertEquals(allocations, result)
    }
}
