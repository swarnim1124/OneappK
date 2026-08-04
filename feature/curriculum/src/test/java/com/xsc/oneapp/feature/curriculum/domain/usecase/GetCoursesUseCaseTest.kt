package com.xsc.oneapp.feature.curriculum.domain.usecase

import com.xsc.oneapp.feature.curriculum.domain.model.Course
import com.xsc.oneapp.feature.curriculum.domain.repository.CurriculumRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetCoursesUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<CurriculumRepository>()
        val courses = listOf(Course("402", "CS101", "Introduction to Programming", "4.0", "3.0", "1.0", "0.0"))
        coEvery { repository.getCourses() } returns courses

        val result = GetCoursesUseCase(repository).invoke()

        assertEquals(courses, result)
    }
}
