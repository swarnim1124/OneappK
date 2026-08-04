package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCondonationsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val condonations = listOf(
            AttendanceCorrectionRequest(
                "2", "50002", "702", "704", "1002", "405", "709", "802", "455",
                "2026-11-20T10:00:00Z", "2026-11-21T09:30:00Z",
                "[CONDONATION] Severe medical emergency during exam preparation week"
            )
        )
        coEvery { repository.getCondonations() } returns condonations

        val result = GetCondonationsUseCase(repository).invoke()

        assertEquals(condonations, result)
    }
}
