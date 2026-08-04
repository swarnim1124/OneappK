package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAttendanceConfigurationsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val configs = listOf(
            AttendanceConfiguration("1", "101", "Fall 2026 Strict Policy", "75.0", "true", "15", "24", "true", "false", "2026-08-01", "2026-12-31", "true")
        )
        coEvery { repository.getAttendanceConfigurations() } returns configs

        val result = GetAttendanceConfigurationsUseCase(repository).invoke()

        assertEquals(configs, result)
    }
}
