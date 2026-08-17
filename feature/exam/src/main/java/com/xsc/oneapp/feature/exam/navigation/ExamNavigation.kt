package com.xsc.oneapp.feature.exam.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.xsc.oneapp.feature.exam.ui.screen.ExamOverviewScreen
import com.xsc.oneapp.feature.exam.ui.screen.ExamResultScreen
import com.xsc.oneapp.feature.exam.ui.screen.ExamRevaluationScreen
import com.xsc.oneapp.feature.exam.ui.screen.ExamScreen
import com.xsc.oneapp.feature.exam.ui.screen.HallTicketScreen
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel

object ExamDestinations {
    /** Entry point for the whole feature. Callers navigate here and nothing else. */
    const val GRAPH_ROUTE = "exam_graph"

    internal const val OVERVIEW = "exam_overview"
    internal const val SCHEDULES = "exam_schedules"
    internal const val RESULTS = "exam_results"
    internal const val REVALUATION = "exam_revaluation"
    internal const val HALL_TICKET = "exam_hall_ticket/{scheduleId}"

    fun hallTicket(scheduleId: String) = "exam_hall_ticket/${Uri.encode(scheduleId)}"
}

/**
 * The Exam feature owns its own graph; `:app` only mounts it - same shape as
 * :feature:attendance's AttendanceNavigation.
 *
 * Overview, Schedules, Results and Revaluation share one [ExamViewModel] scoped to this
 * graph's own back-stack entry, so a section loads once and survives navigation within
 * the feature. Hall Ticket keeps its own per-destination ViewModel because it needs the
 * `scheduleId` route argument.
 */
fun NavGraphBuilder.examGraph(navController: NavHostController) {
    navigation(
        startDestination = ExamDestinations.OVERVIEW,
        route = ExamDestinations.GRAPH_ROUTE
    ) {
        composable(ExamDestinations.OVERVIEW) { entry ->
            ExamOverviewScreen(
                onBack = { navController.popBackStack() },
                onOpenSchedules = { navController.navigate(ExamDestinations.SCHEDULES) },
                onOpenResults = { navController.navigate(ExamDestinations.RESULTS) },
                onOpenRevaluation = { navController.navigate(ExamDestinations.REVALUATION) },
                viewModel = entry.sharedExamViewModel(navController)
            )
        }

        composable(ExamDestinations.SCHEDULES) { entry ->
            ExamScreen(
                onBack = { navController.popBackStack() },
                onScheduleClick = { scheduleId ->
                    navController.navigate(ExamDestinations.hallTicket(scheduleId))
                },
                viewModel = entry.sharedExamViewModel(navController)
            )
        }

        composable(ExamDestinations.RESULTS) { entry ->
            ExamResultScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamViewModel(navController)
            )
        }

        composable(ExamDestinations.REVALUATION) { entry ->
            ExamRevaluationScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamViewModel(navController)
            )
        }

        composable(
            ExamDestinations.HALL_TICKET,
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
        ) {
            // Its own hiltViewModel(): a per-schedule drill-in reading `scheduleId` from
            // SavedStateHandle, not a section of the shared graph ViewModel.
            HallTicketScreen(onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Scopes one [ExamViewModel] to the graph rather than to each destination - see
 * AttendanceNavigation's `sharedAttendanceViewModel` for the same pattern. Without it,
 * every hop between Overview, Schedules, Results and Revaluation would construct a
 * fresh instance and refetch already-loaded sections.
 */
@Composable
private fun NavBackStackEntry.sharedExamViewModel(navController: NavHostController): ExamViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(ExamDestinations.GRAPH_ROUTE)
    }
    return hiltViewModel(parentEntry)
}
