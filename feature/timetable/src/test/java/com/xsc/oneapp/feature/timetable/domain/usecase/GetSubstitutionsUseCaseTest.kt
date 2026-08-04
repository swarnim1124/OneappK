package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.Substitution
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSubstitutionsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val substitutions = listOf(
            Substitution("1", "501", "1", "FAC_CHANGE", "201", "205", "50", "50", "2026-02-15", "2026-02-15", "Faculty on sick leave", "ACTIVE")
        )
        coEvery { repository.getSubstitutions() } returns substitutions

        val result = GetSubstitutionsUseCase(repository).invoke()

        assertEquals(substitutions, result)
    }
}
