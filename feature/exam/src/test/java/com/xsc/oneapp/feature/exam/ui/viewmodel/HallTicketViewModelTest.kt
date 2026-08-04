package com.xsc.oneapp.feature.exam.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.usecase.GetHallTicketUseCase
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

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getHallTicketUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(scheduleId: String = "45") =
        HallTicketViewModel(SavedStateHandle(mapOf("scheduleId" to scheduleId)), getHallTicketUseCase)

    @Test
    fun `tickets from the use case surface a Success state`() = runTest {
        val ticket = HallTicket("12", "45", "1002", "Main Campus Hall - Block A, Room 102", "S-042", "generated")
        coEvery { getHallTicketUseCase("45") } returns listOf(ticket)

        val vm = viewModel()

        val state = vm.state.value as UiState.Success
        assertEquals(listOf(ticket), state.data)
    }

    @Test
    fun `no ticket yet is still a Success state with an empty list`() = runTest {
        coEvery { getHallTicketUseCase("45") } returns emptyList()

        val vm = viewModel()

        val state = vm.state.value as UiState.Success
        assertTrue(state.data.isEmpty())
    }

    @Test
    fun `a business error surfaces its message`() = runTest {
        coEvery { getHallTicketUseCase("45") } throws APIError.BusinessError("BLOCKED", "You are blocked from this exam")

        val vm = viewModel()

        val state = vm.state.value as UiState.BusinessError
        assertEquals("You are blocked from this exam", state.message)
    }
}
