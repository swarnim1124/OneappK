package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAttendanceRecordsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val records = listOf(AttendanceRecord("50001", "10001", "1001", "701", "2026-08-01T09:15:00Z", "Arrived on time"))
        coEvery { repository.getAttendanceRecords() } returns records

        val result = GetAttendanceRecordsUseCase(repository).invoke()

        assertEquals(records, result)
    }
}
