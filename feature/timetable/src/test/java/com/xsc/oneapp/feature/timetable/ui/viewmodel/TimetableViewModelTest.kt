package com.xsc.oneapp.feature.timetable.ui.viewmodel

import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.timetable.domain.model.AcademicCalendar
import com.xsc.oneapp.feature.timetable.domain.model.FacultyAllocation
import com.xsc.oneapp.feature.timetable.domain.model.RoomAllocation
import com.xsc.oneapp.feature.timetable.domain.model.Substitution
import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.model.TimetableApproval
import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.model.WorkingDay
import com.xsc.oneapp.feature.timetable.domain.usecase.GetAcademicCalendarUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetFacultyAllocationsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetRoomAllocationsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetSubstitutionsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetTimeSlotsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetTimetableApprovalsUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetTimetableEntriesUseCase
import com.xsc.oneapp.feature.timetable.domain.usecase.GetWorkingDaysUseCase
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
class TimetableViewModelTest {

    private lateinit var getTimetableEntriesUseCase: GetTimetableEntriesUseCase
    private lateinit var getWorkingDaysUseCase: GetWorkingDaysUseCase
    private lateinit var getTimeSlotsUseCase: GetTimeSlotsUseCase
    private lateinit var getAcademicCalendarUseCase: GetAcademicCalendarUseCase
    private lateinit var getFacultyAllocationsUseCase: GetFacultyAllocationsUseCase
    private lateinit var getRoomAllocationsUseCase: GetRoomAllocationsUseCase
    private lateinit var getSubstitutionsUseCase: GetSubstitutionsUseCase
    private lateinit var getTimetableApprovalsUseCase: GetTimetableApprovalsUseCase

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        getTimetableEntriesUseCase = mockk()
        getWorkingDaysUseCase = mockk()
        getTimeSlotsUseCase = mockk()
        getAcademicCalendarUseCase = mockk()
        getFacultyAllocationsUseCase = mockk()
        getRoomAllocationsUseCase = mockk()
        getSubstitutionsUseCase = mockk()
        getTimetableApprovalsUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): TimetableViewModel = TimetableViewModel(
        getTimetableEntriesUseCase,
        getWorkingDaysUseCase,
        getTimeSlotsUseCase,
        getAcademicCalendarUseCase,
        getFacultyAllocationsUseCase,
        getRoomAllocationsUseCase,
        getSubstitutionsUseCase,
        getTimetableApprovalsUseCase
    )

    @Test
    fun `all eight sub-modules surface a Success state on load`() = runTest {
        val entry = TimetableEntry(
            "1", "5", "1", "2026", "1", "10", "2", "3", "CS101", "1", "201", "201",
            "1", "MONDAY", "2", "50", "LECTURE", "2026-01-01", "2026-06-30", "true",
            "TT_SEC3_TERM1", "PUBLISHED"
        )
        val workingDay = WorkingDay("1", "1", "2026", "Standard Week", "2026-01-01", "2026-12-31", "1", "MONDAY", "true", "true")
        val timeSlot = TimeSlot("1", "1", "Period 1", "09:00:00", "09:50:00", "1", "false", "true")
        val calendar = AcademicCalendar("1", "2026", "1", "proxied term data")
        val facultyAllocation = FacultyAllocation("1", "12", "201", "PRIMARY", "100", "true", "Assigned HOD approved")
        val roomAllocation = RoomAllocation("1", "5", "50", "MONDAY", "2")
        val substitution = Substitution("1", "501", "1", "FAC_CHANGE", "201", "205", "50", "50", "2026-02-15", "2026-02-15", "Faculty on sick leave", "ACTIVE")
        val approval = TimetableApproval("5", "1", "2026", "1", "3", "TT_SEC3_TERM1", "PENDING_APPROVAL", "Timetable draft submitted for Dean review")

        coEvery { getTimetableEntriesUseCase() } returns listOf(entry)
        coEvery { getWorkingDaysUseCase() } returns listOf(workingDay)
        coEvery { getTimeSlotsUseCase() } returns listOf(timeSlot)
        coEvery { getAcademicCalendarUseCase() } returns calendar
        coEvery { getFacultyAllocationsUseCase() } returns listOf(facultyAllocation)
        coEvery { getRoomAllocationsUseCase() } returns listOf(roomAllocation)
        coEvery { getSubstitutionsUseCase() } returns listOf(substitution)
        coEvery { getTimetableApprovalsUseCase() } returns listOf(approval)

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)
        vm.onTabSelected(7)

        assertEquals(listOf(entry), (vm.entriesState.value as UiState.Success).data)
        assertEquals(listOf(workingDay), (vm.workingDaysState.value as UiState.Success).data)
        assertEquals(listOf(timeSlot), (vm.timeSlotsState.value as UiState.Success).data)
        assertEquals(calendar, (vm.academicCalendarState.value as UiState.Success).data)
        assertEquals(listOf(facultyAllocation), (vm.facultyAllocationsState.value as UiState.Success).data)
        assertEquals(listOf(roomAllocation), (vm.roomAllocationsState.value as UiState.Success).data)
        assertEquals(listOf(substitution), (vm.substitutionsState.value as UiState.Success).data)
        assertEquals(listOf(approval), (vm.approvalsState.value as UiState.Success).data)
    }

    @Test
    fun `a null academic calendar is still a Success state, not an error`() = runTest {
        coEvery { getTimetableEntriesUseCase() } returns emptyList()
        coEvery { getWorkingDaysUseCase() } returns emptyList()
        coEvery { getTimeSlotsUseCase() } returns emptyList()
        coEvery { getAcademicCalendarUseCase() } returns null
        coEvery { getFacultyAllocationsUseCase() } returns emptyList()
        coEvery { getRoomAllocationsUseCase() } returns emptyList()
        coEvery { getSubstitutionsUseCase() } returns emptyList()
        coEvery { getTimetableApprovalsUseCase() } returns emptyList()

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)
        vm.onTabSelected(7)

        val state = vm.academicCalendarState.value as UiState.Success
        assertEquals(null, state.data)
    }

    @Test
    fun `a failing sub-module surfaces its error without blocking the others`() = runTest {
        coEvery { getTimetableEntriesUseCase() } returns emptyList()
        coEvery { getWorkingDaysUseCase() } returns emptyList()
        coEvery { getTimeSlotsUseCase() } returns emptyList()
        coEvery { getAcademicCalendarUseCase() } returns null
        coEvery { getFacultyAllocationsUseCase() } returns emptyList()
        coEvery { getRoomAllocationsUseCase() } returns emptyList()
        coEvery { getSubstitutionsUseCase() } throws APIError.HttpError(500, "boom")
        coEvery { getTimetableApprovalsUseCase() } returns emptyList()

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)
        vm.onTabSelected(7)

        val substitutionsState = vm.substitutionsState.value as UiState.UnexpectedError
        assertTrue(substitutionsState.message.contains("500"))
        assertTrue((vm.entriesState.value as UiState.Success).data.isEmpty())
    }

    @Test
    fun `loadEntries retries only the schedule sub-module`() = runTest {
        coEvery { getWorkingDaysUseCase() } returns emptyList()
        coEvery { getTimeSlotsUseCase() } returns emptyList()
        coEvery { getAcademicCalendarUseCase() } returns null
        coEvery { getFacultyAllocationsUseCase() } returns emptyList()
        coEvery { getRoomAllocationsUseCase() } returns emptyList()
        coEvery { getSubstitutionsUseCase() } returns emptyList()
        coEvery { getTimetableApprovalsUseCase() } returns emptyList()
        coEvery { getTimetableEntriesUseCase() } throws APIError.NetworkError("offline") andThen listOf(
            TimetableEntry(
                "1", "5", "1", "2026", "1", "10", "2", "3", "CS101", "1", "201", "201",
                "1", "MONDAY", "2", "50", "LECTURE", "2026-01-01", "2026-06-30", "true",
                "TT_SEC3_TERM1", "PUBLISHED"
            )
        )

        val vm = viewModel()
        vm.onTabSelected(0)
        vm.onTabSelected(1)
        vm.onTabSelected(2)
        vm.onTabSelected(3)
        vm.onTabSelected(4)
        vm.onTabSelected(5)
        vm.onTabSelected(6)
        vm.onTabSelected(7)
        assertTrue(vm.entriesState.value is UiState.NetworkError)

        vm.loadEntries()

        assertEquals(1, (vm.entriesState.value as UiState.Success).data.size)
    }
}
