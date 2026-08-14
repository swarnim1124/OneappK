package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.StudentExamBlock
import com.xsc.oneapp.feature.exam.domain.usecase.GetHallTicketUseCase
import com.xsc.oneapp.feature.exam.domain.usecase.GetMyExamBlocksUseCase
import com.xsc.sdk.network.APIError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HallTicketViewModelTest {

    private lateinit var getHallTicketUseCase: GetHallTicketUseCase
    private lateinit var getMyExamBlocksUseCase: GetMyExamBlocksUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getHallTicketUseCase = mockk()
        getMyExamBlocksUseCase = mockk()
        coEvery { getMyExamBlocksUseCase("45") } returns emptyList()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(scheduleId: String = "45") = HallTicketViewModel(
        SavedStateHandle(mapOf("scheduleId" to scheduleId)),
        getHallTicketUseCase,
        getMyExamBlocksUseCase
    )

    @Test
    fun `tickets from the use case surface a Success state`() = runTest {
        val ticket = HallTicket("12", "45", "1002", "Main Campus Hall - Block A, Room 102", "S-042", "generated")
        coEvery { getHallTicketUseCase("45") } returns listOf(ticket)

        val vm = viewModel()

        val state = vm.state.value as UiState.Success
        assertEquals(listOf(ticket), state.data.tickets)
        assertTrue(state.data.blocks.isEmpty())
    }

    @Test
    fun `no ticket yet is still a Success state with an empty list`() = runTest {
        coEvery { getHallTicketUseCase("45") } returns emptyList()

        val vm = viewModel()

        val state = vm.state.value as UiState.Success
        assertTrue(state.data.tickets.isEmpty())
    }

    @Test
    fun `an active exam block is surfaced alongside the ticket list`() = runTest {
        coEvery { getHallTicketUseCase("45") } returns emptyList()
        val block = StudentExamBlock(
            id = "1", studentId = "1002", scheduleId = "45",
            reason = "Fee arrears", blockedBy = "Accounts office", status = "active", createdAt = "2026-08-01"
        )
        coEvery { getMyExamBlocksUseCase("45") } returns listOf(block)

        val vm = viewModel()

        val state = vm.state.value as UiState.Success
        assertEquals(listOf(block), state.data.blocks)
    }

    @Test
    fun `a failure to load exam blocks does not fail the whole screen`() = runTest {
        val ticket = HallTicket("12", "45", "1002", "Main Campus Hall - Block A, Room 102", "S-042", "generated")
        coEvery { getHallTicketUseCase("45") } returns listOf(ticket)
        coEvery { getMyExamBlocksUseCase("45") } throws APIError.BusinessError("FORBIDDEN", "No permission")

        val vm = viewModel()

        val state = vm.state.value as UiState.Success
        assertEquals(listOf(ticket), state.data.tickets)
        assertTrue(state.data.blocks.isEmpty())
    }

    @Test
    fun `a business error surfaces its message`() = runTest {
        coEvery { getHallTicketUseCase("45") } throws APIError.BusinessError("BLOCKED", "You are blocked from this exam")

        val vm = viewModel()

        val state = vm.state.value as UiState.BusinessError
        assertEquals("You are blocked from this exam", state.message)
    }
}
