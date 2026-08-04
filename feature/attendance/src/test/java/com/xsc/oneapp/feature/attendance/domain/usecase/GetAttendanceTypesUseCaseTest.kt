package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAttendanceTypesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<AttendanceRepository>()
        val types = listOf(AttendanceType("701", "PRESENT", "Present", "true", "true"))
        coEvery { repository.getAttendanceTypes() } returns types

        val result = GetAttendanceTypesUseCase(repository).invoke()

        assertEquals(types, result)
    }
}
