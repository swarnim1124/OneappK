package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetExamSchedulesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val schedules = listOf(ExamSchedule("45", "Midterm", "2026-01-01", "2026-01-05", "Published", "Written"))
        coEvery { repository.getExamSchedules() } returns schedules

        val result = GetExamSchedulesUseCase(repository).invoke()

        assertEquals(schedules, result)
    }
}
