package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetExamResultsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val results = listOf(ExamResult("1", "10", "101", "8.50", "8.30", "generated", "2026-07-31 12:00:00"))
        coEvery { repository.getMyResults() } returns results

        val result = GetExamResultsUseCase(repository).invoke()

        assertEquals(results, result)
    }
}
