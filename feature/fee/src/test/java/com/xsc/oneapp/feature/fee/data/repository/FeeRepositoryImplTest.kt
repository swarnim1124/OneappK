package com.xsc.oneapp.feature.fee.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.oneapp.feature.fee.data.network.FeeEndpoint
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import com.xsc.sdk.network.internal.DispatcherApi
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

/**
 * m_fees rows are raw SQLAlchemy-model dictionaries (same pattern as
 * m_attendance/m_curriculum) - see FeeMapper.kt. Field names below are the real
 * confirmed response shapes (2026-07-30), not guessed.
 */
class FeeRepositoryImplTest {

    private val gson = Gson()

    private fun repository(
        dispatcherApi: DispatcherApi,
        sessionManager: SessionManager = mockk<SessionManager>().also {
            every { it.getUserId() } returns "12045"
            every { it.getInstitutionId() } returns null
        }
    ) = FeeRepositoryImpl(APIClient(dispatcherApi, gson), sessionManager)

    @Test
    fun `getFeeStructures dispatches to m_fees feeStructure view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":1,"fee_structure_code":"FS_2026_BTECH","fee_structure_name":"B.Tech Fall 2026 Tuition","description":"Standard tuition","institution_id":101,"acad_year_id":5,"effective_from":"2026-08-01","effective_to":null,"status_id":1,"is_active":true}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getFeeStructures()

        assertEquals(1, result.size)
        val structure = result.first()
        assertEquals("FS_2026_BTECH", structure.code)
        assertEquals("B.Tech Fall 2026 Tuition", structure.name)
        assertEquals("101", structure.institutionId)
        assertEquals("2026-08-01", structure.effectiveFrom)
        assertEquals(FeeEndpoint.MODULE, requestSlot.captured.mod)
        assertEquals(FeeEndpoint.SubModules.FEE_STRUCTURE, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_STRUCTURE, requestSlot.captured.action)
        assertEquals(FeeEndpoint.ActionTypes.VIEW, requestSlot.captured.actionType)
    }

    @Test
    fun `getFeeStructures includes inst_id when the session has one`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "12045"
        every { sessionManager.getInstitutionId() } returns 101

        repository(dispatcherApi, sessionManager).getFeeStructures()

        assertEquals(101, requestSlot.captured.payload["inst_id"])
    }

    @Test
    fun `getMyFeeAssignments sends the signed-in student's id and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":5001,"stud_id":12045,"fee_structure_id":1,"term_id":10,"total_amount":15000.00,"due_date":"2026-09-01","status_id":1,"is_active":true}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getMyFeeAssignments()

        assertEquals(1, result.size)
        val assignment = result.first()
        assertEquals("15000.00", assignment.totalAmount)
        assertEquals("2026-09-01", assignment.dueDate)
        assertEquals(12045L, requestSlot.captured.payload["studentId"])
        assertEquals(FeeEndpoint.SubModules.FEE_ASSIGNMENT, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_ASSIGNMENT, requestSlot.captured.action)
    }

    @Test
    fun `getMyFeeConcessions dispatches to concession feeConcession view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":302,"stud_id":12045,"concession_type_id":2,"amount":2000.00,"percentage":null,"reason":"Top 10% Entrance Exam Rank","status_id":1,"approved_by":992,"approved_on":"2026-08-15T14:30:00Z"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getMyFeeConcessions()

        assertEquals(1, result.size)
        assertEquals("2000.00", result.first().amount)
        assertEquals("Top 10% Entrance Exam Rank", result.first().reason)
        assertEquals(FeeEndpoint.SubModules.CONCESSION, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_CONCESSION, requestSlot.captured.action)
    }

    /**
     * The regression this whole change exists for: `feeInvoice:view` answers with a
     * single statement object, not a ledger array. The previous implementation mapped
     * it as rows keyed on stud_id/transaction_date/reference_id - none of which are in
     * the response - so the Invoices tab rendered one card of dashes.
     */
    @Test
    fun `getMyFeeStatement maps the aggregate statement object`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val body = JsonParser.parseString(
            """{"studentId":12045,"totalDebits":50000.0,"totalCredits":10000.0,"outstandingBalance":40000.0}"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = body))

        val result = repository(dispatcherApi).getMyFeeStatement()

        assertEquals("12045", result.studentId)
        assertEquals(50000.0, result.totalDebits)
        assertEquals(10000.0, result.totalCredits)
        assertEquals(40000.0, result.outstandingBalance)
        assertEquals(40000.0, result.payableAmount)
        assertEquals(FeeEndpoint.SubModules.INVOICE, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_INVOICE, requestSlot.captured.action)
        assertEquals(12045L, requestSlot.captured.payload["studentId"])
    }

    @Test
    fun `getMyFeeStatement falls back to summing line rows when given an array`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val rows = JsonParser.parseString(
            """[{"invoiceId":1,"amount":15000.00,"description":"Tuition"},{"invoiceId":2,"amount":5000.00,"description":"Hostel"}]"""
        )
        coEvery { dispatcherApi.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getMyFeeStatement()

        assertEquals(2, result.lines.size)
        assertEquals(20000.0, result.totalDebits)
        assertEquals("Tuition", result.lines.first().description)
    }

    @Test
    fun `getMyFeeStatement returns an empty statement rather than throwing on no data`() =
        runTest {
            val dispatcherApi = mockk<DispatcherApi>()
            coEvery { dispatcherApi.dispatch(any()) } returns
                Response.success(DispatchResponse(status = "success", data = null))

            assertTrue(repository(dispatcherApi).getMyFeeStatement().isEmpty)
        }

    @Test
    fun `createOnlinePaymentOrder posts method ONLINE and maps the gateway order`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val body = JsonParser.parseString(
            """{"id":"order_MOCK12345678","entity":"order","amount":4000000,"currency":"INR"}"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = body))

        val order = repository(dispatcherApi).createOnlinePaymentOrder("1", 40000.0, "Asha")

        assertEquals("order_MOCK12345678", order.orderId)
        assertEquals(4_000_000L, order.amountInPaise)
        assertEquals("INR", order.currency)
        // The backend is still returning a placeholder order, so Checkout must not be
        // handed this id.
        assertFalse(order.isRealGatewayOrder)
        assertEquals(FeeEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
        assertEquals("ONLINE", requestSlot.captured.payload["method"])
        assertEquals(1L, requestSlot.captured.payload["invoiceId"])
    }

    @Test
    fun `createOnlinePaymentOrder falls back to the requested amount when the order has none`() =
        runTest {
            val dispatcherApi = mockk<DispatcherApi>()
            coEvery { dispatcherApi.dispatch(any()) } returns Response.success(
                DispatchResponse(
                    status = "success",
                    data = JsonParser.parseString("""{"id":"order_X","currency":"INR"}""")
                )
            )

            val order = repository(dispatcherApi).createOnlinePaymentOrder(null, 250.5, "Asha")

            assertEquals(25050L, order.amountInPaise)
        }

    /**
     * The card has already been charged by the time this runs, so a rejected ledger
     * update must degrade to `false`, never to an exception the UI would render as
     * "payment failed".
     */
    @Test
    fun `confirmOnlinePayment returns false instead of throwing when the backend rejects it`() =
        runTest {
            val dispatcherApi = mockk<DispatcherApi>()
            coEvery { dispatcherApi.dispatch(any()) } returns Response.success(
                DispatchResponse(status = "error", message = "Unknown payment")
            )

            assertFalse(repository(dispatcherApi).confirmOnlinePayment("pay_ABC", true))
        }

    @Test
    fun `confirmOnlinePayment posts COMPLETED on success`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = null))

        assertTrue(repository(dispatcherApi).confirmOnlinePayment("pay_ABC", true))

        assertEquals(FeeEndpoint.ActionTypes.UPDATE, requestSlot.captured.actionType)
        assertEquals("COMPLETED", requestSlot.captured.payload["status"])
        assertEquals("pay_ABC", requestSlot.captured.payload["paymentId"])
    }

    @Test
    fun `getMyFeePayments dispatches to payment feePayment view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":4110,"stud_id":12045,"payment_method_id":5,"payment_mode":"ONLINE","amount":13000.00,"payment_date":"2026-08-20T10:15:00Z","transaction_reference":"pay_XYZ123ABC","status_id":1}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getMyFeePayments()

        assertEquals(1, result.size)
        assertEquals("ONLINE", result.first().paymentMode)
        assertEquals("pay_XYZ123ABC", result.first().transactionReference)
        assertEquals(FeeEndpoint.SubModules.PAYMENT, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_PAYMENT, requestSlot.captured.action)
    }

    @Test
    fun `getMyFeeRefunds dispatches to refund feeRefund view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":105,"payment_id":4110,"amount":1000.00,"reason":"Accidental double payment via gateway","refund_date":"2026-08-22T09:00:00Z","status_id":1,"approved_by":441}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getMyFeeRefunds()

        assertEquals(1, result.size)
        assertEquals("4110", result.first().paymentId)
        assertEquals(FeeEndpoint.SubModules.REFUND, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_REFUND, requestSlot.captured.action)
    }

    @Test
    fun `getFeePenalties scopes the query to the signed-in student and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"penaltyId":88,"studentId":12045,"fee_asmt_id":5001,"penaltyType":"FIXED","penaltyValue":500.00,"reason":"Late payment beyond due date (09-01)","penaltyDate":"2026-09-05"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getFeePenalties()

        assertEquals(1, result.size)
        assertEquals("5001", result.first().feeAssignmentId)
        // v1.3 renamed the money field penaltyValue and the date penaltyDate; both
        // used to map to null, so a penalty rendered with no amount at all.
        assertEquals("500.0", result.first().amount)
        assertEquals("2026-09-05", result.first().appliedDate)
        // v1.3 §3.7 documents studentId as this action's filter. Sending only
        // inst_id (the write side's filter) returned every student's penalties.
        assertEquals(12045L, requestSlot.captured.payload["studentId"])
        assertEquals(FeeEndpoint.SubModules.PENALTY, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_PENALTY, requestSlot.captured.action)
    }
}
