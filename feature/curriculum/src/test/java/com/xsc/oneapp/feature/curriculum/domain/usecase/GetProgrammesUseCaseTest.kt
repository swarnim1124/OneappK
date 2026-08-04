package com.xsc.oneapp.feature.curriculum.domain.usecase

import com.xsc.oneapp.feature.curriculum.domain.model.Programme
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProgrammesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<CurriculumRepository>()
        val programmes = listOf(Programme("B.Tech CSE", "CSE", "8", "160"))
        coEvery { repository.getProgrammes() } returns programmes

        val result = GetProgrammesUseCase(repository).invoke()

        assertEquals(programmes, result)
    }
}
