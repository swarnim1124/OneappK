package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAttendanceExceptionsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val exceptions = listOf(
            AttendanceCorrectionRequest(
                "1", "50001", "702", "704", "1001", null, "708", "801", null,
                "2026-08-02T14:00:00Z", null,
                "[EXCEPTION] [RANGE: 2026-08-01 to 2026-08-05] Official Inter-University Athletics Championship [DOC: /uploads/sports.pdf]"
            )
        )
        coEvery { repository.getAttendanceExceptions() } returns exceptions

        val result = GetAttendanceExceptionsUseCase(repository).invoke()

        assertEquals(exceptions, result)
    }
}
