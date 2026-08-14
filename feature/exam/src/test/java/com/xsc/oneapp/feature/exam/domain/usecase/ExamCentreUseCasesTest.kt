package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.ExamCentre
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExamCentreUseCasesTest {

    @Test
    fun `GetExamCentresUseCase returns centres from the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val centres = listOf(ExamCentre("1", "Main Block", "200", "Campus Road", listOf("101"), "ACTIVE"))
        coEvery { repository.getExamCentres() } returns centres

        val result = GetExamCentresUseCase(repository)()

        assertEquals(centres, result)
    }

    @Test
    fun `AddExamCentreUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.addExamCentre("Main Block", "200", "Campus Road", listOf("101")) } just Runs

        AddExamCentreUseCase(repository)("Main Block", "200", "Campus Road", listOf("101"))

        coVerify(exactly = 1) { repository.addExamCentre("Main Block", "200", "Campus Road", listOf("101")) }
    }

    @Test
    fun `UpdateExamCentreUseCase forwards every field to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery {
            repository.updateExamCentre("1", "North Block", "250", "New Road", listOf("102"), "INACTIVE")
        } just Runs

        UpdateExamCentreUseCase(repository)("1", "North Block", "250", "New Road", listOf("102"), "INACTIVE")

        coVerify(exactly = 1) {
            repository.updateExamCentre("1", "North Block", "250", "New Road", listOf("102"), "INACTIVE")
        }
    }

    @Test
    fun `DeleteExamCentreUseCase forwards centreId`() = runTest {
        val repository = mockk<ExamRepository>()
        coEvery { repository.deleteExamCentre("1") } just Runs

        DeleteExamCentreUseCase(repository)("1")

        coVerify(exactly = 1) { repository.deleteExamCentre("1") }
    }
}
