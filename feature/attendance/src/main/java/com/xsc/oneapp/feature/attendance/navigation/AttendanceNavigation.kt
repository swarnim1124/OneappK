package com.xsc.oneapp.feature.attendance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.xsc.oneapp.feature.attendance.ui.screen.AttendanceOverviewScreen
import com.xsc.oneapp.feature.attendance.ui.screen.AttendancePolicyScreen
import com.xsc.oneapp.feature.attendance.ui.screen.AttendanceRecordsScreen
import com.xsc.oneapp.feature.attendance.ui.screen.AttendanceRequestsScreen
import com.xsc.oneapp.feature.attendance.ui.viewmodel.AttendanceViewModel

object AttendanceDestinations {
    /** Entry point for the whole feature. Callers navigate here and nothing else. */
    const val GRAPH_ROUTE = "attendance_graph"

    internal const val OVERVIEW = "attendance_overview"
    internal const val RECORDS = "attendance_records"
    internal const val REQUESTS = "attendance_requests"
    internal const val POLICY = "attendance_policy"
}

/**
 * The Attendance feature owns its own graph; `:app` only mounts it.
 *
 * Previously `:app` imported `AttendanceScreen` directly and declared a single
 * `composable(Routes.ATTENDANCE)`, so adding any destination inside this feature meant
 * editing the app module. Now the internal routes are `internal` to this module and
 * only [AttendanceDestinations.GRAPH_ROUTE] is public API.
 */
fun NavGraphBuilder.attendanceGraph(navController: NavHostController) {
    navigation(
        startDestination = AttendanceDestinations.OVERVIEW,
        route = AttendanceDestinations.GRAPH_ROUTE
    ) {
        composable(AttendanceDestinations.OVERVIEW) { entry ->
            AttendanceOverviewScreen(
                onBack = { navController.popBackStack() },
                onOpenRecords = { navController.navigate(AttendanceDestinations.RECORDS) },
                onOpenRequests = { navController.navigate(AttendanceDestinations.REQUESTS) },
                onOpenPolicy = { navController.navigate(AttendanceDestinations.POLICY) },
                viewModel = entry.sharedAttendanceViewModel(navController)
            )
        }

        composable(AttendanceDestinations.RECORDS) { entry ->
            AttendanceRecordsScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedAttendanceViewModel(navController)
            )
        }

        composable(AttendanceDestinations.REQUESTS) { entry ->
            AttendanceRequestsScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedAttendanceViewModel(navController)
            )
        }

        composable(AttendanceDestinations.POLICY) { entry ->
            AttendancePolicyScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedAttendanceViewModel(navController)
            )
        }
    }
}

/**
 * Scopes one [AttendanceViewModel] to the graph rather than to each destination.
 *
 * With a per-destination ViewModel every hop would construct a fresh instance and
 * refetch, so walking Overview -> Records -> back -> Records would hit the network four
 * times for the same rows. Tying it to the parent entry means each section loads once
 * and survives navigation within the feature, while still being cleared when the user
 * leaves Attendance entirely.
 */
@Composable
private fun NavBackStackEntry.sharedAttendanceViewModel(
    navController: NavHostController
): AttendanceViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(AttendanceDestinations.GRAPH_ROUTE)
    }
    return hiltViewModel(parentEntry)
}
