package com.xsc.oneapp.feature.fee.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.oneapp.feature.fee.data.network.FeeEndpoint
import com.xsc.oneapp.feature.fee.domain.model.FeeStructureComponent
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import com.xsc.sdk.network.APIError
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
import org.junit.Assert.assertThrows
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

    @Test
    fun `getMyFeeInvoices dispatches to invoice feeInvoice view and maps rows`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":89912,"stud_id":12045,"transaction_type_id":4,"transaction_date":"2026-08-05","amount":15000.00,"reference_id":5001,"description":"Tuition Fee Assignment Generation"}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getMyFeeInvoices()

        assertEquals(1, result.size)
        assertEquals("5001", result.first().referenceId)
        assertEquals(FeeEndpoint.SubModules.INVOICE, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_INVOICE, requestSlot.captured.action)
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
    fun `getFeePenalties dispatches to penalty feePenalty view and maps rows, without a studentId filter`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        val rows = JsonParser.parseString(
            """[{"id":88,"stud_id":12045,"fee_asmt_id":5001,"amount":500.00,"reason":"Late payment beyond due date (09-01)","applied_date":"2026-09-05","status_id":1}]"""
        )
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = rows))

        val result = repository(dispatcherApi).getFeePenalties()

        assertEquals(1, result.size)
        assertEquals("5001", result.first().feeAssignmentId)
        assertFalse(requestSlot.captured.payload.containsKey("studentId"))
        assertEquals(FeeEndpoint.SubModules.PENALTY, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_PENALTY, requestSlot.captured.action)
    }

    @Test
    fun `createFeeStructure sends the full payload and returns the new id`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("""{"fee_structure_id":1}""")))
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns "12045"
        every { sessionManager.getInstitutionId() } returns 1

        val component = FeeStructureComponent("TUIT", "Tuition Fee", 50000.0, false)
        val result = repository(dispatcherApi, sessionManager).createFeeStructure(
            code = "FS-2026-ENG",
            name = "B.Tech Tuition Fee 2026",
            academicYearId = "1",
            programmeId = "101",
            components = listOf(component),
            effectiveFrom = "2026-06-01",
            effectiveTo = "2027-05-31"
        )

        assertEquals("1", result)
        assertEquals("FS-2026-ENG", requestSlot.captured.payload["feeStructureCode"])
        assertEquals("B.Tech Tuition Fee 2026", requestSlot.captured.payload["feeStructureName"])
        assertEquals(1L, requestSlot.captured.payload["academicYearId"])
        assertEquals(101L, requestSlot.captured.payload["programmeId"])
        assertEquals("2026-06-01", requestSlot.captured.payload["effectiveFrom"])
        assertEquals("2027-05-31", requestSlot.captured.payload["effectiveTo"])
        assertEquals(1, requestSlot.captured.payload["inst_id"])
        @Suppress("UNCHECKED_CAST")
        val components = requestSlot.captured.payload["components"] as List<Map<String, Any>>
        assertEquals("TUIT", components.first()["headCode"])
        assertEquals(50000.0, components.first()["amount"])
        assertEquals(FeeEndpoint.SubModules.FEE_STRUCTURE, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `createFeeStructure omits a blank effectiveTo`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).createFeeStructure(
            "FS-2026-ENG", "B.Tech Tuition", "1", "101",
            listOf(FeeStructureComponent("TUIT", "Tuition Fee", 50000.0, false)), "2026-06-01", null
        )

        assertFalse(requestSlot.captured.payload.containsKey("effectiveTo"))
    }

    @Test
    fun `updateFeeStructureStatus sends feeStructureId and status`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).updateFeeStructureStatus("1", "ACTIVE")

        assertEquals(1L, requestSlot.captured.payload["feeStructureId"])
        assertEquals("ACTIVE", requestSlot.captured.payload["status"])
        assertEquals(FeeEndpoint.ActionTypes.UPDATE, requestSlot.captured.actionType)
    }

    @Test
    fun `deleteFeeStructure sends feeStructureId`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).deleteFeeStructure("1")

        assertEquals(1L, requestSlot.captured.payload["feeStructureId"])
        assertEquals(FeeEndpoint.ActionTypes.DELETE, requestSlot.captured.actionType)
    }

    @Test
    fun `assignFee sends a single studentId when only one target is given`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).assignFee("1", "10", "2026-07-15", listOf("10"))

        assertEquals(10L, requestSlot.captured.payload["studentId"])
        assertFalse(requestSlot.captured.payload.containsKey("studentIds"))
        assertEquals(FeeEndpoint.SubModules.FEE_ASSIGNMENT, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `assignFee sends studentIds when multiple targets are given`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).assignFee("1", "10", "2026-07-15", listOf("10", "11"))

        assertEquals(listOf(10L, 11L), requestSlot.captured.payload["studentIds"])
        assertFalse(requestSlot.captured.payload.containsKey("studentId"))
    }

    @Test
    fun `deleteFeeAssignment sends assignmentId`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).deleteFeeAssignment("5001")

        assertEquals(5001L, requestSlot.captured.payload["assignmentId"])
        assertEquals(FeeEndpoint.SubModules.FEE_ASSIGNMENT, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.ActionTypes.DELETE, requestSlot.captured.actionType)
    }

    @Test
    fun `grantConcession sends the full payload`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).grantConcession("10", "1", "MERIT_SCHOLARSHIP", 5000.0, "Top 1% Academic Performer")

        assertEquals(10L, requestSlot.captured.payload["studentId"])
        assertEquals(1L, requestSlot.captured.payload["assignmentId"])
        assertEquals("MERIT_SCHOLARSHIP", requestSlot.captured.payload["concessionType"])
        assertEquals(5000.0, requestSlot.captured.payload["amountOrPercent"])
        assertEquals(FeeEndpoint.SubModules.CONCESSION, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_CONCESSION, requestSlot.captured.action)
        assertEquals(FeeEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `initiateOnlinePayment sends method ONLINE and parses the order response`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns Response.success(
            DispatchResponse(
                status = "success",
                data = JsonParser.parseString("""{"id":"order_MOCK12345678","entity":"order","amount":1000000,"currency":"INR"}""")
            )
        )

        val order = repository(dispatcherApi).initiateOnlinePayment("1", 10000.0, "Student Self")

        assertEquals("order_MOCK12345678", order.id)
        assertEquals(1000000L, order.amount)
        assertEquals("INR", order.currency)
        assertEquals(1L, requestSlot.captured.payload["invoiceId"])
        assertEquals(10000.0, requestSlot.captured.payload["amount"])
        assertEquals("ONLINE", requestSlot.captured.payload["method"])
        assertEquals("Student Self", requestSlot.captured.payload["paidBy"])
        assertEquals(FeeEndpoint.SubModules.PAYMENT, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.Actions.FEE_PAYMENT, requestSlot.captured.action)
        assertEquals(FeeEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `initiateOnlinePayment throws a business error when the gateway returns no order`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        coEvery { dispatcherApi.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("null")))

        assertThrows(APIError.BusinessError::class.java) {
            kotlinx.coroutines.runBlocking { repository(dispatcherApi).initiateOnlinePayment("1", 10000.0, "Student Self") }
        }
    }

    @Test
    fun `requestRefund resolves studentId from the session, not a caller-supplied value`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).requestRefund("1", 2000.0, "Overpayment during admission")

        assertEquals(12045L, requestSlot.captured.payload["studentId"])
        assertEquals(1L, requestSlot.captured.payload["invoiceId"])
        assertEquals(2000.0, requestSlot.captured.payload["amount"])
        assertEquals(FeeEndpoint.SubModules.REFUND, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.ActionTypes.ADD, requestSlot.captured.actionType)
    }

    @Test
    fun `requestRefund throws instead of silently defaulting when there's no signed-in student`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val sessionManager = mockk<SessionManager>()
        every { sessionManager.getUserId() } returns null

        assertThrows(APIError.BusinessError::class.java) {
            kotlinx.coroutines.runBlocking {
                repository(dispatcherApi, sessionManager).requestRefund("1", 2000.0, "Overpayment")
            }
        }
    }

    @Test
    fun `updateRefundStatus sends refundId, status and remarks when present`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).updateRefundStatus("1", "APPROVED", "Verified against payment gateway log")

        assertEquals(1L, requestSlot.captured.payload["refundId"])
        assertEquals("APPROVED", requestSlot.captured.payload["status"])
        assertEquals("Verified against payment gateway log", requestSlot.captured.payload["remarks"])
        assertEquals(FeeEndpoint.SubModules.REFUND, requestSlot.captured.subMod)
        assertEquals(FeeEndpoint.ActionTypes.UPDATE, requestSlot.captured.actionType)
    }

    @Test
    fun `updateRefundStatus omits a blank remarks`() = runTest {
        val dispatcherApi = mockk<DispatcherApi>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { dispatcherApi.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("{}")))

        repository(dispatcherApi).updateRefundStatus("1", "REJECTED", null)

        assertFalse(requestSlot.captured.payload.containsKey("remarks"))
    }
}
