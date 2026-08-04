package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetRevaluationRequestsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val requests = listOf(RevaluationRequest("1", "101", "CS101", "10", "Mark discrepancy", "submitted", "2026-07-31 12:00:00"))
        coEvery { repository.getMyRevaluationRequests() } returns requests

        val result = GetRevaluationRequestsUseCase(repository).invoke()

        assertEquals(requests, result)
    }
}
