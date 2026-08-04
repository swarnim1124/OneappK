package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSubmissionComplianceReportUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val report = listOf(
            AttendanceSession("10001", "201001", "2026-08-01", "501", "2", "2026-08-01T09:00:00Z", "2026-08-01T10:05:00Z", "2026-08-01T10:05:00Z", "Submitted on time.")
        )
        coEvery { repository.getSubmissionComplianceReport() } returns report

        val result = GetSubmissionComplianceReportUseCase(repository).invoke()

        assertEquals(report, result)
    }
}
