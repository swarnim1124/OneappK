package com.xsc.oneapp.feature.fee.ui.viewmodel

import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.fee.domain.model.FeeAssignment
import com.xsc.oneapp.feature.fee.domain.model.FeeConcession
import com.xsc.oneapp.feature.fee.domain.model.FeeStatement
import com.xsc.oneapp.feature.fee.domain.model.FeePayment
import com.xsc.oneapp.feature.fee.domain.model.FeePenalty
import com.xsc.oneapp.feature.fee.domain.model.FeeRefund
import com.xsc.oneapp.feature.fee.domain.model.FeeStructure
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeePenaltiesUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetFeeStructuresUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeAssignmentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeConcessionsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.ConfirmOnlinePaymentUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.CreateOnlinePaymentOrderUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeStatementUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeePaymentsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeRefundsUseCase
import com.xsc.oneapp.feature.fee.domain.usecase.RequestFeeRefundUseCase
import com.xsc.oneapp.feature.fee.domain.model.PaymentOrder
import com.xsc.oneapp.feature.fee.payment.RazorpayCheckoutResult
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import app.cash.turbine.test
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
    private lateinit var getMyFeeStatementUseCase: GetMyFeeStatementUseCase
    private lateinit var getMyFeePaymentsUseCase: GetMyFeePaymentsUseCase
    private lateinit var getMyFeeRefundsUseCase: GetMyFeeRefundsUseCase
    private lateinit var getFeePenaltiesUseCase: GetFeePenaltiesUseCase
    private lateinit var createOnlinePaymentOrderUseCase: CreateOnlinePaymentOrderUseCase
    private lateinit var confirmOnlinePaymentUseCase: ConfirmOnlinePaymentUseCase
    private lateinit var requestFeeRefundUseCase: RequestFeeRefundUseCase
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getFeeStructuresUseCase = mockk()
        getMyFeeAssignmentsUseCase = mockk()
        getMyFeeConcessionsUseCase = mockk()
        getMyFeeStatementUseCase = mockk()
        getMyFeePaymentsUseCase = mockk()
        getMyFeeRefundsUseCase = mockk()
        getFeePenaltiesUseCase = mockk()
        createOnlinePaymentOrderUseCase = mockk()
        confirmOnlinePaymentUseCase = mockk()
        requestFeeRefundUseCase = mockk()
        sessionManager = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): FeeViewModel = FeeViewModel(
        getFeeStructuresUseCase,
        getMyFeeAssignmentsUseCase,
        getMyFeeConcessionsUseCase,
        getMyFeeStatementUseCase,
        getMyFeePaymentsUseCase,
        getMyFeeRefundsUseCase,
        getFeePenaltiesUseCase,
        createOnlinePaymentOrderUseCase,
        confirmOnlinePaymentUseCase,
        requestFeeRefundUseCase,
        sessionManager
    )

    @Test
    fun `all seven sub-modules surface a Success state on load`() = runTest {
        val structure = FeeStructure("1", "FS_2026_BTECH", "B.Tech Fall 2026 Tuition", null, "101", "5", "2026-08-01", null, "1", "true")
        val assignment = FeeAssignment("5001", "12045", "1", "10", "15000.00", "2026-09-01", "1", "true")
        val concession = FeeConcession("302", "12045", "2", "2000.00", null, "Top 10% Entrance Exam Rank", "1", "992", "2026-08-15T14:30:00Z")
        val statement = FeeStatement("12045", 50000.0, 10000.0, 40000.0)
        val payment = FeePayment("4110", "12045", "5", "ONLINE", "13000.00", "2026-08-20T10:15:00Z", "pay_XYZ123ABC", "1")
        val refund = FeeRefund("105", "4110", "1000.00", "Accidental double payment via gateway", "2026-08-22T09:00:00Z", "1", "441")
        val penalty = FeePenalty("88", "12045", "5001", "500.00", "Late payment beyond due date (09-01)", "2026-09-05", "1")

        coEvery { getFeeStructuresUseCase() } returns listOf(structure)
        coEvery { getMyFeeAssignmentsUseCase() } returns listOf(assignment)
        coEvery { getMyFeeConcessionsUseCase() } returns listOf(concession)
        coEvery { getMyFeeStatementUseCase() } returns statement
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
        assertEquals(statement, (vm.statementState.value as UiState.Success).data)
        assertEquals(listOf(payment), (vm.paymentsState.value as UiState.Success).data)
        assertEquals(listOf(refund), (vm.refundsState.value as UiState.Success).data)
        assertEquals(listOf(penalty), (vm.penaltiesState.value as UiState.Success).data)
    }

    @Test
    fun `an empty list from any use case is still a Success state`() = runTest {
        coEvery { getFeeStructuresUseCase() } returns emptyList()
        coEvery { getMyFeeAssignmentsUseCase() } returns emptyList()
        coEvery { getMyFeeConcessionsUseCase() } returns emptyList()
        coEvery { getMyFeeStatementUseCase() } returns FeeStatement.EMPTY
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
        coEvery { getMyFeeStatementUseCase() } returns FeeStatement.EMPTY
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
        coEvery { getMyFeeStatementUseCase() } returns FeeStatement.EMPTY
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

    // --- Online payment (Razorpay) -----------------------------------------

    private fun statementLoaded(vm: FeeViewModel) {
        coEvery { getMyFeeStatementUseCase() } returns FeeStatement("12045", 50000.0, 10000.0, 40000.0)
        vm.onTabSelected(1)
    }

    @Test
    fun `startOnlinePayment charges the outstanding balance, not a user-supplied figure`() =
        runTest {
            every { sessionManager.getDisplayName() } returns "Asha"
            coEvery {
                createOnlinePaymentOrderUseCase(any(), 40000.0, "Asha")
            } returns PaymentOrder("order_REAL123", 4_000_000L, "INR", null)

            val vm = viewModel()
            statementLoaded(vm)

            vm.effects.test {
                vm.startOnlinePayment()
                val effect = awaitItem() as FeeEffect.LaunchCheckout
                assertEquals(4_000_000L, effect.request.amountInPaise)
                assertEquals("order_REAL123", effect.request.orderId)
                cancelAndIgnoreRemainingEvents()
            }
        }

    /** The backend still returns `order_MOCK...`; forwarding it fails at the gateway. */
    @Test
    fun `a placeholder order id is dropped so Checkout opens on the amount alone`() = runTest {
        coEvery {
            createOnlinePaymentOrderUseCase(any(), any(), any())
        } returns PaymentOrder("order_MOCK12345678", 4_000_000L, "INR", null)

        val vm = viewModel()
        statementLoaded(vm)

        vm.effects.test {
            vm.startOnlinePayment()
            val effect = awaitItem() as FeeEffect.LaunchCheckout
            assertEquals(null, effect.request.orderId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nothing outstanding means no order is ever opened`() = runTest {
        coEvery { getMyFeeStatementUseCase() } returns FeeStatement("12045", 50000.0, 50000.0, 0.0)

        val vm = viewModel()
        vm.onTabSelected(1)

        vm.effects.test {
            vm.startOnlinePayment()
            assertTrue(awaitItem() is FeeEffect.Notify)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify(exactly = 0) { createOnlinePaymentOrderUseCase(any(), any(), any()) }
    }

    /**
     * The one rule that matters most here: the card has already been charged by the
     * time the result comes back, so a ledger update the backend rejects must not be
     * reported to the student as a failed payment.
     */
    @Test
    fun `a successful charge the backend cannot reconcile is still reported as paid`() = runTest {
        coEvery { getMyFeeStatementUseCase() } returns FeeStatement("12045", 50000.0, 10000.0, 40000.0)
        coEvery { getMyFeePaymentsUseCase() } returns emptyList()
        coEvery { confirmOnlinePaymentUseCase("pay_ABC", true) } returns false

        val vm = viewModel()
        vm.onTabSelected(1)

        vm.effects.test {
            vm.onCheckoutResult(RazorpayCheckoutResult.Success("pay_ABC", null, null))
            val notice = awaitItem() as FeeEffect.Notify
            assertTrue(notice.message.contains("successful"))
            assertTrue(!notice.isError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a cancelled checkout says so and reports no charge`() = runTest {
        coEvery { getMyFeeStatementUseCase() } returns FeeStatement.EMPTY

        val vm = viewModel()

        vm.effects.test {
            vm.onCheckoutResult(RazorpayCheckoutResult.Cancelled)
            val notice = awaitItem() as FeeEffect.Notify
            assertTrue(notice.message.contains("not been charged"))
            cancelAndIgnoreRemainingEvents()
        }
    }
}
