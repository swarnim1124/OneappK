package com.xsc.oneapp.feature.attendance.ui.viewmodel

import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceShortage
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceConfigurationsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceExceptionsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceRecordsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceSessionsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceShortageUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceTypesUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetCondonationsUseCase
import com.xsc.oneapp.feature.attendance.domain.usecase.GetSubmissionComplianceReportUseCase
import com.xsc.sdk.network.APIError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    private lateinit var getAttendanceShortageUseCase: GetAttendanceShortageUseCase
    private lateinit var getAttendanceConfigurationsUseCase: GetAttendanceConfigurationsUseCase
    private lateinit var getAttendanceTypesUseCase: GetAttendanceTypesUseCase
    private lateinit var getAttendanceSessionsUseCase: GetAttendanceSessionsUseCase
    private lateinit var getSubmissionComplianceReportUseCase: GetSubmissionComplianceReportUseCase
    private lateinit var getAttendanceRecordsUseCase: GetAttendanceRecordsUseCase
    private lateinit var getAttendanceExceptionsUseCase: GetAttendanceExceptionsUseCase
    private lateinit var getCondonationsUseCase: GetCondonationsUseCase

    private val shortageRow = AttendanceShortage(
        studentId = "101",
        totalSessions = "40",
        presentSessions = "29",
        attendancePercentage = "72",
        minRequiredPercentage = "75",
        shortagePercentage = "3",
        riskLevel = "WARNING",
        isShortage = "true"
    )

    private fun session(id: String, date: String, submittedAt: String?) = AttendanceSession(
        id = id,
        classSessionId = "2010$id",
        sessionDate = date,
        markedByFacultyId = "501",
        statusId = "1",
        startedAt = "${date}T09:00:00Z",
        submittedAt = submittedAt,
        lockedAt = null,
        remarks = null
    )

    private fun correction(id: String, approvedAt: String?) = AttendanceCorrectionRequest(
        id = id,
        attendanceRecordId = "5000$id",
        oldStatusId = "702",
        newStatusId = "704",
        requestedByUserId = "1001",
        approvedByUserId = null,
        statusId = "708",
        correctionReasonId = "801",
        proofDocId = null,
        requestedAt = "2026-08-02T14:00:00Z",
        approvedAt = approvedAt,
        remarks = null
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getAttendanceShortageUseCase = mockk()
        getAttendanceConfigurationsUseCase = mockk()
        getAttendanceTypesUseCase = mockk()
        getAttendanceSessionsUseCase = mockk()
        getSubmissionComplianceReportUseCase = mockk()
        getAttendanceRecordsUseCase = mockk()
        getAttendanceExceptionsUseCase = mockk()
        getCondonationsUseCase = mockk()
        stubAllEmpty()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): AttendanceViewModel = AttendanceViewModel(
        getAttendanceShortageUseCase,
        getAttendanceConfigurationsUseCase,
        getAttendanceTypesUseCase,
        getAttendanceSessionsUseCase,
        getSubmissionComplianceReportUseCase,
        getAttendanceRecordsUseCase,
        getAttendanceExceptionsUseCase,
        getCondonationsUseCase
    )

    // --- Lazy loading -------------------------------------------------------

    @Test
    fun `constructing the ViewModel dispatches no requests at all`() = runTest {
        viewModel()

        // The regression this guards: the previous version fired all eight use cases
        // from init, hitting the single dispatcher endpoint eight times for tabs the
        // user had not opened.
        coVerify(exactly = 0) { getAttendanceShortageUseCase() }
        coVerify(exactly = 0) { getAttendanceSessionsUseCase() }
        coVerify(exactly = 0) { getAttendanceConfigurationsUseCase() }
        coVerify(exactly = 0) { getCondonationsUseCase() }
    }

    @Test
    fun `loadOverview fetches only the three sections the overview renders`() = runTest {
        val vm = viewModel()

        vm.loadOverview()

        coVerify(exactly = 1) { getAttendanceShortageUseCase() }
        coVerify(exactly = 1) { getSubmissionComplianceReportUseCase() }
        coVerify(exactly = 1) { getAttendanceExceptionsUseCase() }
        coVerify(exactly = 0) { getAttendanceRecordsUseCase() }
        coVerify(exactly = 0) { getAttendanceConfigurationsUseCase() }
        coVerify(exactly = 0) { getAttendanceTypesUseCase() }
        coVerify(exactly = 0) { getCondonationsUseCase() }
    }

    @Test
    fun `revisiting a section does not refetch it`() = runTest {
        val vm = viewModel()

        vm.loadRequests()
        vm.loadRequests()
        vm.loadRequests()

        coVerify(exactly = 1) { getAttendanceExceptionsUseCase() }
        coVerify(exactly = 1) { getCondonationsUseCase() }
    }

    @Test
    fun `an explicit reload refetches even after a successful load`() = runTest {
        val vm = viewModel()

        vm.loadPolicy()
        vm.configurations.reload()

        coVerify(exactly = 2) { getAttendanceConfigurationsUseCase() }
    }

    @Test
    fun `sections shared across screens are fetched once, not once per screen`() = runTest {
        val vm = viewModel()

        // Submissions back both the overview's pending count and the Records screen.
        vm.loadOverview()
        vm.loadRecords()

        coVerify(exactly = 1) { getSubmissionComplianceReportUseCase() }
    }

    // --- Section state ------------------------------------------------------

    @Test
    fun `shortage rows surface a Success state`() = runTest {
        coEvery { getAttendanceShortageUseCase() } returns listOf(shortageRow)
        val vm = viewModel()

        vm.loadOverview()

        assertEquals(listOf(shortageRow), (vm.shortage.state.value as UiState.Success).data)
    }

    @Test
    fun `no shortage rows is still a Success state with an empty list`() = runTest {
        val vm = viewModel()

        vm.loadOverview()

        assertTrue((vm.shortage.state.value as UiState.Success).data.isEmpty())
    }

    @Test
    fun `a business error - such as no signed-in student - surfaces its message`() = runTest {
        coEvery { getAttendanceShortageUseCase() } throws
            APIError.BusinessError("NO_ID", "No signed-in student to look up attendance for")
        val vm = viewModel()

        vm.loadOverview()

        val state = vm.shortage.state.value as UiState.BusinessError
        assertEquals("No signed-in student to look up attendance for", state.message)
    }

    @Test
    fun `all eight sub-modules surface a Success state once their section is loaded`() = runTest {
        val config = AttendanceConfiguration("1", "101", "Fall 2026 Strict Policy", "75.0", "true", "15", "24", "true", "false", "2026-08-01", "2026-12-31", "true")
        val type = AttendanceType("701", "PRESENT", "Present", "true", "true")
        val sessionRow = session("10001", "2026-08-01", null)
        val submissionRow = session("10002", "2026-08-02", "2026-08-02T10:05:00Z")
        val record = AttendanceRecord("50001", "10001", "1001", "701", "2026-08-01T09:15:00Z", "Arrived on time")
        val exception = correction("1", approvedAt = null)
        val condonation = correction("2", approvedAt = "2026-11-21T09:30:00Z")

        coEvery { getAttendanceShortageUseCase() } returns listOf(shortageRow)
        coEvery { getAttendanceConfigurationsUseCase() } returns listOf(config)
        coEvery { getAttendanceTypesUseCase() } returns listOf(type)
        coEvery { getAttendanceSessionsUseCase() } returns listOf(sessionRow)
        coEvery { getSubmissionComplianceReportUseCase() } returns listOf(submissionRow)
        coEvery { getAttendanceRecordsUseCase() } returns listOf(record)
        coEvery { getAttendanceExceptionsUseCase() } returns listOf(exception)
        coEvery { getCondonationsUseCase() } returns listOf(condonation)

        val vm = viewModel()
        vm.loadOverview()
        vm.loadRecords()
        vm.loadRequests()
        vm.loadPolicy()

        assertEquals(listOf(shortageRow), (vm.shortage.state.value as UiState.Success).data)
        assertEquals(listOf(config), (vm.configurations.state.value as UiState.Success).data)
        assertEquals(listOf(type), (vm.types.state.value as UiState.Success).data)
        assertEquals(listOf(sessionRow), (vm.sessions.state.value as UiState.Success).data)
        assertEquals(listOf(submissionRow), (vm.submissions.state.value as UiState.Success).data)
        assertEquals(listOf(record), (vm.records.state.value as UiState.Success).data)
        assertEquals(listOf(exception), (vm.exceptions.state.value as UiState.Success).data)
        assertEquals(listOf(condonation), (vm.condonations.state.value as UiState.Success).data)
    }

    // --- Derived overview ---------------------------------------------------

    @Test
    fun `overview surfaces the students shortage row and derived counts`() = runTest {
        coEvery { getAttendanceShortageUseCase() } returns listOf(shortageRow)
        coEvery { getSubmissionComplianceReportUseCase() } returns listOf(
            session("1", "2026-08-01", submittedAt = null),
            session("2", "2026-08-02", submittedAt = "2026-08-02T10:00:00Z"),
            session("3", "2026-08-03", submittedAt = null)
        )
        coEvery { getAttendanceExceptionsUseCase() } returns listOf(
            correction("1", approvedAt = null),
            correction("2", approvedAt = "2026-08-04T10:00:00Z")
        )

        val vm = viewModel()
        vm.loadOverview()

        val overview = vm.overview.value
        assertEquals(shortageRow, overview.shortage)
        assertEquals(2, overview.pendingSubmissions)
        assertEquals(1, overview.openRequests)
        assertFalse(overview.isLoading)
        assertNull(overview.errorMessage)
    }

    @Test
    fun `overview lists the most recent sessions first and caps them at three`() = runTest {
        coEvery { getSubmissionComplianceReportUseCase() } returns listOf(
            session("1", "2026-08-01", null),
            session("2", "2026-08-05", null),
            session("3", "2026-08-03", null),
            session("4", "2026-08-04", null)
        )

        val vm = viewModel()
        vm.loadOverview()

        val dates = vm.overview.value.recentSessions.map { it.sessionDate }
        assertEquals(listOf("2026-08-05", "2026-08-04", "2026-08-03"), dates)
    }

    @Test
    fun `a failing secondary section degrades its tile instead of erroring the screen`() = runTest {
        coEvery { getAttendanceShortageUseCase() } returns listOf(shortageRow)
        coEvery { getSubmissionComplianceReportUseCase() } throws
            APIError.NetworkError("Unable to reach the server")

        val vm = viewModel()
        vm.loadOverview()

        val overview = vm.overview.value
        // The headline still renders; only the pending-submissions tile reads zero.
        assertEquals(shortageRow, overview.shortage)
        assertEquals(0, overview.pendingSubmissions)
        assertNull(overview.errorMessage)
    }

    @Test
    fun `a failing headline section does surface an error for the whole overview`() = runTest {
        // uiStateCatching maps a NetworkError to a fixed, reason-based message now
        // (see core.result.AppError.Network) rather than passing the exception's own
        // text through - the assertion checks that fixed copy, not the thrown message.
        coEvery { getAttendanceShortageUseCase() } throws
            APIError.NetworkError("Unable to reach the server")

        val vm = viewModel()
        vm.loadOverview()

        assertEquals("Server is unreachable. Try again in a few minutes.", vm.overview.value.errorMessage)
    }

    private fun stubAllEmpty() {
        coEvery { getAttendanceShortageUseCase() } returns emptyList()
        coEvery { getAttendanceConfigurationsUseCase() } returns emptyList()
        coEvery { getAttendanceTypesUseCase() } returns emptyList()
        coEvery { getAttendanceSessionsUseCase() } returns emptyList()
        coEvery { getSubmissionComplianceReportUseCase() } returns emptyList()
        coEvery { getAttendanceRecordsUseCase() } returns emptyList()
        coEvery { getAttendanceExceptionsUseCase() } returns emptyList()
        coEvery { getCondonationsUseCase() } returns emptyList()
    }
}
