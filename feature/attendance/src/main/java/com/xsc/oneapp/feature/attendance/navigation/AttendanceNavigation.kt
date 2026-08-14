package com.xsc.oneapp.feature.attendance.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.deniesRoute
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
 *
 * [accessibleRoutes] and [fallbackRoute] gate only [AttendanceDestinations.OVERVIEW] -
 * the graph's own start destination and only entry point from outside this module.
 * Records/Requests/Policy stay ungated: they're only reachable via the onOpen* calls
 * below, from a user already inside Overview, same as HALL_TICKET's precedent in
 * RootNavHost. `:app` has no visibility into this graph's own composable() entries,
 * so it can't wrap them itself - the check has to live here instead.
 */
fun NavGraphBuilder.attendanceGraph(
    navController: NavHostController,
    accessibleRoutes: UiState<Set<String>>,
    fallbackRoute: String
) {
    navigation(
        startDestination = AttendanceDestinations.OVERVIEW,
        route = AttendanceDestinations.GRAPH_ROUTE
    ) {
        composable(AttendanceDestinations.OVERVIEW) { entry ->
            if (accessibleRoutes.deniesRoute(AttendanceDestinations.GRAPH_ROUTE)) {
                LaunchedEffect(Unit) {
                    navController.navigate(fallbackRoute) {
                        popUpTo(fallbackRoute) { inclusive = true }
                    }
                }
            } else {
                AttendanceOverviewScreen(
                    onBack = { navController.popBackStack(fallbackRoute, false) },
                    onOpenRecords = { navController.navigate(AttendanceDestinations.RECORDS) },
                    onOpenRequests = { navController.navigate(AttendanceDestinations.REQUESTS) },
                    onOpenPolicy = { navController.navigate(AttendanceDestinations.POLICY) },
                    viewModel = entry.sharedAttendanceViewModel(navController)
                )
            }
        }

        // onBack pops straight to fallbackRoute (Dashboard), not navController.popBackStack() -
        // these destinations sit one hop below Overview inside this nested graph, so a plain
        // pop would only return to Overview instead of Dashboard.
        composable(AttendanceDestinations.RECORDS) { entry ->
            AttendanceRecordsScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
                viewModel = entry.sharedAttendanceViewModel(navController)
            )
        }

        composable(AttendanceDestinations.REQUESTS) { entry ->
            AttendanceRequestsScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
                viewModel = entry.sharedAttendanceViewModel(navController)
            )
        }

        composable(AttendanceDestinations.POLICY) { entry ->
            AttendancePolicyScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
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
