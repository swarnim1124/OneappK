package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceShortage
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAttendanceShortageUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val rows = listOf(
            AttendanceShortage(
                studentId = "101",
                totalSessions = "40",
                presentSessions = "29",
                attendancePercentage = "72",
                minRequiredPercentage = "75",
                shortagePercentage = "3",
                riskLevel = "WARNING",
                isShortage = "true"
            )
        )
        coEvery { repository.getMyShortageReport() } returns rows

        val result = GetAttendanceShortageUseCase(repository).invoke()

        assertEquals(rows, result)
    }
}
