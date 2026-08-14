package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExamBlockUseCasesTest {

    @Test
    fun `GetMyExamBlocksUseCase delegates the schedule id to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val blocks = listOf(
            StudentExamBlock("1", "101", "10", "Fee arrears", "Accounts office", "active", "2026-08-01")
        )
        coEvery { repository.getMyExamBlocks("10") } returns blocks

        val result = GetMyExamBlocksUseCase(repository).invoke("10")

        assertEquals(blocks, result)
    }

    @Test
    fun `GetMyExamBlocksUseCase defaults to no schedule filter`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.getMyExamBlocks(null) } returns emptyList()

        GetMyExamBlocksUseCase(repository).invoke()

        coVerify(exactly = 1) { repository.getMyExamBlocks(null) }
    }
}
