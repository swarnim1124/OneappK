package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.SeatingPlan
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SeatingPlanUseCasesTest {

    @Test
    fun `GetSeatingPlansUseCase returns plans for the given schedule`() = runTest {
        val repository = mockk<ExamRepository>()
        val plans = listOf(SeatingPlan("1", "1", "10", "101", "45", "A1", "RANDOM"))
        coEvery { repository.getSeatingPlans("1") } returns plans

        val result = GetSeatingPlansUseCase(repository)("1")

        assertEquals(plans, result)
    }

    @Test
    fun `AddSeatingPlanUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addSeatingPlan("1", "10", "101", "45", "A1", "RANDOM") } just Runs

        AddSeatingPlanUseCase(repository)("1", "10", "101", "45", "A1", "RANDOM")

        coVerify(exactly = 1) { repository.addSeatingPlan("1", "10", "101", "45", "A1", "RANDOM") }
    }

    @Test
    fun `UpdateSeatingPlanUseCase forwards id and updated fields`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.updateSeatingPlan("1", "A2", "11") } just Runs

        UpdateSeatingPlanUseCase(repository)("1", "A2", "11")

        coVerify(exactly = 1) { repository.updateSeatingPlan("1", "A2", "11") }
    }

    @Test
    fun `DeleteSeatingPlanUseCase forwards id`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteSeatingPlan("1") } just Runs

        DeleteSeatingPlanUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteSeatingPlan("1") }
    }
}
