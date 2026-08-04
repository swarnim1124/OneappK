package com.xsc.oneapp.feature.curriculum.domain.usecase

import com.xsc.oneapp.feature.curriculum.domain.model.Syllabus
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSyllabusUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<CurriculumRepository>()
        val syllabus = listOf(Syllabus("55", "1", "B.Tech CS 2026 Syllabus"))
        coEvery { repository.getSyllabus() } returns syllabus

        val result = GetSyllabusUseCase(repository).invoke()

        assertEquals(syllabus, result)
    }
}
