package com.xsc.oneapp.feature.fee.ui.viewmodel

import app.cash.turbine.test
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeInvoice
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.oneapp.feature.fee.domain.model.RazorpayOrder
import com.xsc.oneapp.feature.fee.domain.usecase.AssignFeeUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.CreateFeeStructureUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.DeleteFeeAssignmentUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.DeleteFeeStructureUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeePenaltiesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeeStructuresUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeAssignmentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeConcessionsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeInvoicesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeePaymentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeRefundsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GrantConcessionUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.InitiateOnlinePaymentUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.RequestRefundUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.UpdateFeeStructureStatusUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.UpdateRefundStatusUseCase
import com.xsc.oneapp.feature.fee.ui.state.FeeEffect
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIError
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    private lateinit var createFeeStructureUseCase: CreateFeeStructureUseCase
    private lateinit var updateFeeStructureStatusUseCase: UpdateFeeStructureStatusUseCase
    private lateinit var deleteFeeStructureUseCase: DeleteFeeStructureUseCase
    private lateinit var assignFeeUseCase: AssignFeeUseCase
    private lateinit var deleteFeeAssignmentUseCase: DeleteFeeAssignmentUseCase
    private lateinit var grantConcessionUseCase: GrantConcessionUseCase
    private lateinit var initiateOnlinePaymentUseCase: InitiateOnlinePaymentUseCase
    private lateinit var requestRefundUseCase: RequestRefundUseCase
    private lateinit var updateRefundStatusUseCase: UpdateRefundStatusUseCase
    private lateinit var sessionManager: SessionManager

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
        createFeeStructureUseCase = mockk()
        updateFeeStructureStatusUseCase = mockk()
        deleteFeeStructureUseCase = mockk()
        assignFeeUseCase = mockk()
        deleteFeeAssignmentUseCase = mockk()
        grantConcessionUseCase = mockk()
        initiateOnlinePaymentUseCase = mockk()
        requestRefundUseCase = mockk()
        updateRefundStatusUseCase = mockk()
        sessionManager = mockk()
        // Fully denied by default (matches FeePermissions' own fail-closed default) -
        // tests that care about a granted permission override this explicitly.
        every { sessionManager.currentPermissions } returns MutableStateFlow(emptyList())
        every { sessionManager.hasPermission(any()) } returns false
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
        getFeePenaltiesUseCase,
        createFeeStructureUseCase,
        updateFeeStructureStatusUseCase,
        deleteFeeStructureUseCase,
        assignFeeUseCase,
        deleteFeeAssignmentUseCase,
        grantConcessionUseCase,
        initiateOnlinePaymentUseCase,
        requestRefundUseCase,
        updateRefundStatusUseCase,
        sessionManager
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

    // --- FeePermissions derivation ---

    @Test
    fun `permissions default to fully denied when the session has none`() = runTest {
        val vm = viewModel()

        val permissions = vm.permissions.value
        assertFalse(permissions.canAddFeeStructure)
        assertFalse(permissions.canUpdateFeeStructure)
        assertFalse(permissions.canDeleteFeeStructure)
        assertFalse(permissions.canAssignFee)
        assertFalse(permissions.canDeleteFeeAssignment)
        assertFalse(permissions.canGrantConcession)
        assertFalse(permissions.canApproveRefund)
    }

    @Test
    fun `permissions reflect exactly the granted strings, not a blanket admin flag`() = runTest {
        every { sessionManager.currentPermissions } returns MutableStateFlow(
            listOf("m_fees.feeStructure.feeStructure.add", "m_fees.refund.feeRefund.update")
        )
        every { sessionManager.hasPermission("m_fees.feeStructure.feeStructure.add") } returns true
        every { sessionManager.hasPermission("m_fees.refund.feeRefund.update") } returns true
        every { sessionManager.hasPermission(match { it != "m_fees.feeStructure.feeStructure.add" && it != "m_fees.refund.feeRefund.update" }) } returns false

        val vm = viewModel()

        val permissions = vm.permissions.value
        assertTrue(permissions.canAddFeeStructure)
        assertTrue(permissions.canApproveRefund)
        assertFalse(permissions.canUpdateFeeStructure)
        assertFalse(permissions.canDeleteFeeStructure)
        assertFalse(permissions.canAssignFee)
        assertFalse(permissions.canDeleteFeeAssignment)
        assertFalse(permissions.canGrantConcession)
    }

    // --- Submit-then-reload write actions ---

    @Test
    fun `createFeeStructure reloads structures and emits a toast on success`() = runTest {
        coEvery { getFeeStructuresUseCase() } returns emptyList()
        val component = FeeStructureComponent("TUIT", "Tuition Fee", 50000.0, false)
        coEvery {
            createFeeStructureUseCase("FS-2026-ENG", "B.Tech Tuition", "1", "101", listOf(component), "2026-06-01", null)
        } returns "1"

        val vm = viewModel()
        vm.loadStructures()

        vm.effect.test {
            vm.createFeeStructure("FS-2026-ENG", "B.Tech Tuition", "1", "101", listOf(component), "2026-06-01", null)
            assertEquals(FeeEffect.ShowToast("Fee structure created"), awaitItem())
        }
        coVerify(exactly = 2) { getFeeStructuresUseCase() } // initial load + post-create reload
    }

    @Test
    fun `deleteFeeStructure reloads structures`() = runTest {
        coEvery { getFeeStructuresUseCase() } returns emptyList()
        coEvery { deleteFeeStructureUseCase("1") } just Runs

        val vm = viewModel()
        vm.loadStructures()
        vm.deleteFeeStructure("1")

        coVerify(exactly = 1) { deleteFeeStructureUseCase("1") }
        coVerify(exactly = 2) { getFeeStructuresUseCase() }
    }

    @Test
    fun `assignFee reloads assignments`() = runTest {
        coEvery { getMyFeeAssignmentsUseCase() } returns emptyList()
        coEvery { assignFeeUseCase("1", "10", "2026-07-15", listOf("10")) } just Runs

        val vm = viewModel()
        vm.loadAssignments()
        vm.assignFee("1", "10", "2026-07-15", listOf("10"))

        coVerify(exactly = 1) { assignFeeUseCase("1", "10", "2026-07-15", listOf("10")) }
        coVerify(exactly = 2) { getMyFeeAssignmentsUseCase() }
    }

    @Test
    fun `grantConcession reloads concessions`() = runTest {
        coEvery { getMyFeeConcessionsUseCase() } returns emptyList()
        coEvery { grantConcessionUseCase("10", "1", "MERIT_SCHOLARSHIP", 5000.0, "Top rank") } just Runs

        val vm = viewModel()
        vm.loadConcessions()
        vm.grantConcession("10", "1", "MERIT_SCHOLARSHIP", 5000.0, "Top rank")

        coVerify(exactly = 1) { grantConcessionUseCase("10", "1", "MERIT_SCHOLARSHIP", 5000.0, "Top rank") }
        coVerify(exactly = 2) { getMyFeeConcessionsUseCase() }
    }

    @Test
    fun `requestRefund reloads refunds`() = runTest {
        coEvery { getMyFeeRefundsUseCase() } returns emptyList()
        coEvery { requestRefundUseCase("1", 2000.0, "Overpayment") } just Runs

        val vm = viewModel()
        vm.loadRefunds()
        vm.requestRefund("1", 2000.0, "Overpayment")

        coVerify(exactly = 1) { requestRefundUseCase("1", 2000.0, "Overpayment") }
        coVerify(exactly = 2) { getMyFeeRefundsUseCase() }
    }

    @Test
    fun `updateRefundStatus approves and reloads refunds`() = runTest {
        coEvery { getMyFeeRefundsUseCase() } returns emptyList()
        coEvery { updateRefundStatusUseCase("1", "APPROVED", null) } just Runs

        val vm = viewModel()
        vm.loadRefunds()

        vm.effect.test {
            vm.updateRefundStatus("1", "APPROVED", null)
            assertEquals(FeeEffect.ShowToast("Refund approved"), awaitItem())
        }
        coVerify(exactly = 2) { getMyFeeRefundsUseCase() }
    }

    @Test
    fun `a failed submit emits the business error message instead of a generic one`() = runTest {
        coEvery { getMyFeeRefundsUseCase() } returns emptyList()
        coEvery { requestRefundUseCase("1", 2000.0, "Overpayment") } throws
            APIError.BusinessError("BAD_INVOICE", "Invoice does not belong to this student")

        val vm = viewModel()
        vm.loadRefunds()

        vm.effect.test {
            vm.requestRefund("1", 2000.0, "Overpayment")
            assertEquals(FeeEffect.ShowToast("Invoice does not belong to this student"), awaitItem())
        }
    }

    // --- Online payment ---

    @Test
    fun `initiateOnlinePayment delegates to the use case and returns the order`() = runTest {
        val order = RazorpayOrder("order_MOCK12345678", 1000000, "INR")
        coEvery { initiateOnlinePaymentUseCase("1", 10000.0, "Student Self") } returns order

        val vm = viewModel()
        val result = vm.initiateOnlinePayment("1", 10000.0)

        assertEquals(order, result)
    }

    @Test
    fun `onOnlinePaymentSubmitted emits a toast and reloads invoices and payments, never claiming success itself`() = runTest {
        coEvery { getMyFeeInvoicesUseCase() } returns emptyList()
        coEvery { getMyFeePaymentsUseCase() } returns emptyList()

        val vm = viewModel()
        vm.effect.test {
            vm.onOnlinePaymentSubmitted()
            assertEquals(FeeEffect.ShowToast("Payment submitted - your balance will update shortly"), awaitItem())
        }
        coVerify(exactly = 1) { getMyFeeInvoicesUseCase() }
        coVerify(exactly = 1) { getMyFeePaymentsUseCase() }
    }
}
