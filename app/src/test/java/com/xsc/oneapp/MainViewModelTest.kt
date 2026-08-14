package com.xsc.oneapp

import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.core.registry.ModuleRegistry
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.feature.attendance.navigation.AttendanceDestinations
import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.domain.usecase.GetAccessibleModulesUseCase
import com.xsc.oneapp.feature.profile.navigation.ProfileDestinations
import com.xsc.oneapp.navigation.Routes
import com.xsc.sdk.auth.SessionManager
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

/**
 * accessibleRoutes is what RootNavHost's GatedDestination guards navigation against -
 * see its doc comment for why "loaded and route absent" is the only state that
 * redirects, never "still loading" or "fetch failed".
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var sessionManager: SessionManager
    private lateinit var getAccessibleModulesUseCase: GetAccessibleModulesUseCase

    private fun module(id: String, route: String) =
        ModuleItem(id, id, "icon", route, ModuleItem.ModuleStatus.ACTIVE, "#4F46E5")

    /** Mirrors exactly what each feature's own DI module contributes in production
     * (see AttendanceModule, ProfileModule, ExamModule, FeeModule, CurriculumModule,
     * TimetableModule) so this test exercises the real alias/navigationEntry shape
     * rather than a contrived subset. */
    private fun productionEquivalentRegistry() = ModuleRegistry(
        setOf(
            ModuleDefinition("attendance", "Attendance", AttendanceDestinations.GRAPH_ROUTE, setOf("attendance")),
            ModuleDefinition("profile", "Profile", ProfileDestinations.PROFILE_ROUTE, setOf("profile")),
            ModuleDefinition("exams", "Exams", "exams", setOf("exams", "exam")),
            ModuleDefinition("fees", "Fees", "fees", setOf("fees", "fee")),
            ModuleDefinition("curriculum", "Curriculum", "curriculum", setOf("academics", "curriculum")),
            ModuleDefinition("timetable", "Timetable", "timetable", setOf("timetable"))
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        sessionManager = mockk()
        getAccessibleModulesUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() =
        MainViewModel(sessionManager, productionEquivalentRegistry(), getAccessibleModulesUseCase)

    @Test
    fun `accessible routes are derived from each module's route via Routes destinationFor`() = runTest {
        coEvery { getAccessibleModulesUseCase() } returns listOf(
            module("fees_module", "/fees"),
            module("attendance_module", "attendance")
        )

        val vm = viewModel()

        val state = vm.accessibleRoutes.value
        assertTrue(state is UiState.Success)
        assertEquals(
            setOf(Routes.FEES, Routes.ATTENDANCE),
            (state as UiState.Success).data
        )
    }

    @Test
    fun `a module absent from the accessible list never appears in the resolved route set`() = runTest {
        coEvery { getAccessibleModulesUseCase() } returns listOf(module("fees_module", "/fees"))

        val vm = viewModel()

        val data = (vm.accessibleRoutes.value as UiState.Success).data
        assertTrue(Routes.FEES in data)
        assertTrue(Routes.TIMETABLE !in data)
    }

    @Test
    fun `an empty accessible-modules response resolves to an empty, non-null route set`() = runTest {
        coEvery { getAccessibleModulesUseCase() } returns emptyList()

        val vm = viewModel()

        assertEquals(UiState.Success(emptySet<String>()), vm.accessibleRoutes.value)
    }

    @Test
    fun `a failed fetch surfaces as a UiState failure, not a crash`() = runTest {
        coEvery { getAccessibleModulesUseCase() } throws RuntimeException("offline")

        val vm = viewModel()

        assertTrue(vm.accessibleRoutes.value is UiState.UnexpectedError)
    }
}
