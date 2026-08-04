package com.xsc.oneapp.feature.fee.ui.viewmodel

import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeePenaltiesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeeStructuresUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeAssignmentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeConcessionsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeInvoicesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeePaymentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeRefundsUseCase
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
class FeeViewModelTest {

    private lateinit var getFeeStructuresUseCase: GetFeeStructuresUseCase
    private lateinit var getMyFeeAssignmentsUseCase: GetMyFeeAssignmentsUseCase
    private lateinit var getMyFeeConcessionsUseCase: GetMyFeeConcessionsUseCase
    private lateinit var getMyFeeInvoicesUseCase: GetMyFeeInvoicesUseCase
    private lateinit var getMyFeePaymentsUseCase: GetMyFeePaymentsUseCase
    private lateinit var getMyFeeRefundsUseCase: GetMyFeeRefundsUseCase
    private lateinit var getFeePenaltiesUseCase: GetFeePenaltiesUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getFeeStructuresUseCase = mockk()
        getMyFeeAssignmentsUseCase = mockk()
        getMyFeeConcessionsUseCase = mockk()
        getMyFeeInvoicesUseCase = mockk()
        getMyFeePaymentsUseCase = mockk()
        getMyFeeRefundsUseCase = mockk()
        getFeePenaltiesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): FeeViewModel = FeeViewModel(
        getFeeStructuresUseCase,
        getMyFeeAssignmentsUseCase,
        getMyFeeConcessionsUseCase,
        getMyFeeInvoicesUseCase,
        getMyFeePaymentsUseCase,
        getMyFeeRefundsUseCase,
        getFeePenaltiesUseCase
    )

    @Test
    fun `all seven sub-modules surface a Success state on load`() = runTest {
        val structure = FeeStructure("1", "FS_2026_BTECH", "B.Tech Fall 2026 Tuition", null, "101", "5", "2026-08-01", null, "1", "true")
        val assignment = FeeAssignment("5001", "12045", "1", "10", "15000.00", "2026-09-01", "1", "true")
        val concession = FeeConcession("302", "12045", "2", "2000.00", null, "Top 10% Entrance Exam Rank", "1", "992", "2026-08-15T14:30:00Z")
        val invoice = FeeInvoice("89912", "12045", "4", "2026-08-05", "15000.00", "5001", "Tuition Fee Assignment Generation")
        val payment = FeePayment("4110", "12045", "5", "ONLINE", "13000.00", "2026-08-20T10:15:00Z", "pay_XYZ123ABC", "1")
        val refund = FeeRefund("105", "4110", "1000.00", "Accidental double payment via gateway", "2026-08-22T09:00:00Z", "1", "441")
        val penalty = FeePenalty("88", "12045", "5001", "500.00", "Late payment beyond due date (09-01)", "2026-09-05", "1")

        coEvery { getFeeStructuresUseCase() } returns listOf(structure)
        coEvery { getMyFeeAssignmentsUseCase() } returns listOf(assignment)
        coEvery { getMyFeeConcessionsUseCase() } returns listOf(concession)
        coEvery { getMyFeeInvoicesUseCase() } returns listOf(invoice)
        coEvery { getMyFeePaymentsUseCase() } returns listOf(payment)
        coEvery { getMyFeeRefundsUseCase() } returns listOf(refund)
        coEvery { getFeePenaltiesUseCase() } returns listOf(penalty)

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)

        assertEquals(listOf(structure), (vm.structuresState.value as UiState.Success).data)
        assertEquals(listOf(assignment), (vm.assignmentsState.value as UiState.Success).data)
        assertEquals(listOf(concession), (vm.concessionsState.value as UiState.Success).data)
        assertEquals(listOf(invoice), (vm.invoicesState.value as UiState.Success).data)
        assertEquals(listOf(payment), (vm.paymentsState.value as UiState.Success).data)
        assertEquals(listOf(refund), (vm.refundsState.value as UiState.Success).data)
        assertEquals(listOf(penalty), (vm.penaltiesState.value as UiState.Success).data)
    }

    @Test
    fun `an empty list from any use case is still a Success state`() = runTest {
        coEvery { getFeeStructuresUseCase() } returns emptyList()
        coEvery { getMyFeeAssignmentsUseCase() } returns emptyList()
        coEvery { getMyFeeConcessionsUseCase() } returns emptyList()
        coEvery { getMyFeeInvoicesUseCase() } returns emptyList()
        coEvery { getMyFeePaymentsUseCase() } returns emptyList()
        coEvery { getMyFeeRefundsUseCase() } returns emptyList()
        coEvery { getFeePenaltiesUseCase() } returns emptyList()

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)

        assertTrue((vm.assignmentsState.value as UiState.Success).data.isEmpty())
    }

    @Test
    fun `a failing sub-module surfaces its error without blocking the others`() = runTest {
        coEvery { getFeeStructuresUseCase() } returns emptyList()
        coEvery { getMyFeeAssignmentsUseCase() } returns emptyList()
        coEvery { getMyFeeConcessionsUseCase() } returns emptyList()
        coEvery { getMyFeeInvoicesUseCase() } returns emptyList()
        coEvery { getMyFeePaymentsUseCase() } returns emptyList()
        coEvery { getMyFeeRefundsUseCase() } returns emptyList()
        coEvery { getFeePenaltiesUseCase() } throws APIError.HttpError(500, "boom")

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)

        val penaltiesState = vm.penaltiesState.value as UiState.UnexpectedError
        assertTrue(penaltiesState.message.contains("500"))
        assertTrue((vm.assignmentsState.value as UiState.Success).data.isEmpty())
    }

    @Test
    fun `loadPayments retries only the payments sub-module`() = runTest {
        coEvery { getFeeStructuresUseCase() } returns emptyList()
        coEvery { getMyFeeAssignmentsUseCase() } returns emptyList()
        coEvery { getMyFeeConcessionsUseCase() } returns emptyList()
        coEvery { getMyFeeInvoicesUseCase() } returns emptyList()
        coEvery { getMyFeeRefundsUseCase() } returns emptyList()
        coEvery { getFeePenaltiesUseCase() } returns emptyList()
        coEvery { getMyFeePaymentsUseCase() } throws APIError.NetworkError("offline") andThen listOf(
            FeePayment("4110", "12045", "5", "ONLINE", "13000.00", "2026-08-20T10:15:00Z", "pay_XYZ123ABC", "1")
        )

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)
        assertTrue(vm.paymentsState.value is UiState.NetworkError)

        vm.loadPayments()

        assertEquals(1, (vm.paymentsState.value as UiState.Success).data.size)
    }
}
