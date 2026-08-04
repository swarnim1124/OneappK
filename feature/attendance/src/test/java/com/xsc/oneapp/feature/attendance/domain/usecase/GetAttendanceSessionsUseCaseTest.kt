package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAttendanceSessionsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val sessions = listOf(
            AttendanceSession("10001", "201001", "2026-08-01", "501", "1", "2026-08-01T09:00:00Z", null, null, "Lecture 1: Introduction to Calculus")
        )
        coEvery { repository.getAttendanceSessions() } returns sessions

        val result = GetAttendanceSessionsUseCase(repository).invoke()

        assertEquals(sessions, result)
    }
}
