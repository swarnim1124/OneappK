package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.MalpracticeCase
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetMyMalpracticeCasesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val cases = listOf(
            MalpracticeCase("1", "101", "10", "Unauthorized material", "Invigilator A", null, null, null, "pending", "2026-08-01")
        )
        coEvery { repository.getMyMalpracticeCases() } returns cases

        val result = GetMyMalpracticeCasesUseCase(repository).invoke()

        assertEquals(cases, result)
    }
}
