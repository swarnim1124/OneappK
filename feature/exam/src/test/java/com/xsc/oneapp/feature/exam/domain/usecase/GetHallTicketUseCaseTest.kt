package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetHallTicketUseCaseTest {

    @Test
    fun `invoke delegates the scheduleId to the repository`() = runTest {
        val repository = mockk<ExamRepository>()
        val tickets = listOf(HallTicket("12", "45", "1002", "Main Campus Hall - Block A, Room 102", "S-042", "generated"))
        coEvery { repository.getHallTicket("45") } returns tickets

        val result = GetHallTicketUseCase(repository).invoke("45")

        assertEquals(tickets, result)
    }
}
