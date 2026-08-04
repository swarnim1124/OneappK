package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.TimetableApproval
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTimetableApprovalsUseCaseTest {

    @Test
    fun `invoke delegates to the repository`() = runTest {
        val repository = mockk<TimetableRepository>()
        val approvals = listOf(
            TimetableApproval("5", "1", "2026", "1", "3", "TT_SEC3_TERM1", "PENDING_APPROVAL", "Timetable draft submitted for Dean review")
        )
        coEvery { repository.getTimetableApprovals() } returns approvals

        val result = GetTimetableApprovalsUseCase(repository).invoke()

        assertEquals(approvals, result)
    }
}
