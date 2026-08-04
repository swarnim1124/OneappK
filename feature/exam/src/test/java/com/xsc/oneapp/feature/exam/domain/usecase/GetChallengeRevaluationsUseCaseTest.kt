package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetChallengeRevaluationsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val challenges = listOf(
            ChallengeRevaluation("1", "101", "CS101", "10", "5", "Challenge regular reval outcome", "submitted", "2026-07-31 12:00:00")
        )
        coEvery { repository.getMyChallengeRevaluations() } returns challenges

        val result = GetChallengeRevaluationsUseCase(repository).invoke()

        assertEquals(challenges, result)
    }
}
