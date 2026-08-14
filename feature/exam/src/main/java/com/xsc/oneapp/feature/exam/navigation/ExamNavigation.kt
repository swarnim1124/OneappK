package com.xsc.oneapp.feature.exam.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.deniesRoute
import com.xsc.oneapp.feature.exam.ui.screen.EvaluationScreen
import com.xsc.oneapp.feature.exam.ui.screen.ExamAdminScreen
import com.xsc.oneapp.feature.exam.ui.screen.ExamCentreScreen
import com.xsc.oneapp.feature.exam.ui.screen.ExamScreen
import com.xsc.oneapp.feature.exam.ui.screen.ExternalExaminerScreen
import com.xsc.oneapp.feature.exam.ui.screen.HallTicketAdminScreen
import com.xsc.oneapp.feature.exam.ui.screen.HallTicketScreen
import com.xsc.oneapp.feature.exam.ui.screen.InternalExamScreen
import com.xsc.oneapp.feature.exam.ui.screen.InvigilationScreen
import com.xsc.oneapp.feature.exam.ui.screen.MalpracticeScreen
import com.xsc.oneapp.feature.exam.ui.screen.MarksGradingScreen
import com.xsc.oneapp.feature.exam.ui.screen.QuestionPaperScreen
import com.xsc.oneapp.feature.exam.ui.screen.ReExamScreen
import com.xsc.oneapp.feature.exam.ui.screen.ResultAdminScreen
import com.xsc.oneapp.feature.exam.ui.screen.ResultsScreen
import com.xsc.oneapp.feature.exam.ui.screen.RevaluationScreen
import com.xsc.oneapp.feature.exam.ui.screen.SeatingPlanScreen
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamAdminViewModel
import com.xsc.oneapp.feature.exam.ui.viewmodel.ExamViewModel

/**
 * The Exam feature owns its own graph; `:app` only mounts it - same split as
 * [com.xsc.oneapp.feature.attendance.navigation.attendanceGraph]. Grew past the
 * student-facing subset once the m_exam contract's examination-administration routes
 * got a real repository/use-case/ViewModel layer - see the approved plan
 * ("m_exam contract completion: the full examination-administration surface").
 */
object ExamDestinations {
    /** Entry point for the whole feature. Callers navigate here and nothing else. */
    const val GRAPH_ROUTE = "exam_graph"

    internal const val SCHEDULES = "exam_schedules"
    internal const val HALL_TICKET = "exam_hall_ticket/{scheduleId}"
    internal const val RESULTS = "exam_results"
    internal const val REVALUATION = "exam_revaluation"
    internal const val RE_EXAMS = "exam_re_exams"
    internal const val MALPRACTICE = "exam_malpractice"

    // Admin surface - reached only from ADMIN_HUB, which is itself only reachable
    // when ExamAdminViewModel.hasAnyExamAdminAccess is true (checked inside ExamScreen).
    internal const val ADMIN_HUB = "exam_admin_hub"
    internal const val ADMIN_EXAM_CENTRES = "exam_admin_centres"
    internal const val ADMIN_SEATING = "exam_admin_seating"
    internal const val ADMIN_INVIGILATION = "exam_admin_invigilation"
    internal const val ADMIN_HALL_TICKET = "exam_admin_hall_ticket"
    internal const val ADMIN_QUESTION_PAPER = "exam_admin_question_paper"
    internal const val ADMIN_EXTERNAL_EXAM = "exam_admin_external_exam"
    internal const val ADMIN_INTERNAL_EXAM = "exam_admin_internal_exam"
    internal const val ADMIN_MARKS_GRADING = "exam_admin_marks_grading"
    internal const val ADMIN_EVALUATION = "exam_admin_evaluation"
    internal const val ADMIN_RESULTS = "exam_admin_results"

    internal fun hallTicket(scheduleId: String) = "exam_hall_ticket/$scheduleId"
}

/**
 * [accessibleRoutes] and [fallbackRoute] gate only [ExamDestinations.SCHEDULES] - the
 * graph's own start destination and only entry point from outside this module. Every
 * other destination is only reachable from within Schedules, same precedent as
 * Attendance's Records/Requests/Policy. The admin destinations add a second,
 * per-action gate on top of this: [ExamAdminViewModel.permissions] decides what each
 * admin screen lets a signed-in user actually do, not whether the route is reachable
 * at all - a student session simply never sees the entry point (see ExamScreen).
 */
fun NavGraphBuilder.examGraph(
    navController: NavHostController,
    accessibleRoutes: UiState<Set<String>>,
    fallbackRoute: String
) {
    navigation(
        startDestination = ExamDestinations.SCHEDULES,
        route = ExamDestinations.GRAPH_ROUTE
    ) {
        composable(ExamDestinations.SCHEDULES) { entry ->
            if (accessibleRoutes.deniesRoute(ExamDestinations.GRAPH_ROUTE)) {
                LaunchedEffect(Unit) {
                    navController.navigate(fallbackRoute) {
                        popUpTo(fallbackRoute) { inclusive = true }
                    }
                }
            } else {
                ExamScreen(
                    onBack = { navController.popBackStack(fallbackRoute, false) },
                    onScheduleClick = { scheduleId ->
                        navController.navigate(ExamDestinations.hallTicket(scheduleId))
                    },
                    onOpenResults = { navController.navigate(ExamDestinations.RESULTS) },
                    onOpenRevaluation = { navController.navigate(ExamDestinations.REVALUATION) },
                    onOpenReExams = { navController.navigate(ExamDestinations.RE_EXAMS) },
                    onOpenMalpractice = { navController.navigate(ExamDestinations.MALPRACTICE) },
                    onOpenAdmin = { navController.navigate(ExamDestinations.ADMIN_HUB) },
                    viewModel = entry.sharedExamViewModel(navController),
                    adminViewModel = entry.sharedExamAdminViewModel(navController)
                )
            }
        }

        // onBack pops straight to fallbackRoute (Dashboard), not navController.popBackStack() -
        // these destinations sit one hop below Schedules inside this nested graph, so a
        // plain pop would only return to Schedules instead of Dashboard.
        composable(
            ExamDestinations.HALL_TICKET,
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
        ) {
            HallTicketScreen(onBack = { navController.popBackStack(fallbackRoute, false) })
        }

        composable(ExamDestinations.RESULTS) { entry ->
            ResultsScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
                viewModel = entry.sharedExamViewModel(navController)
            )
        }

        composable(ExamDestinations.REVALUATION) { entry ->
            RevaluationScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
                viewModel = entry.sharedExamViewModel(navController),
                adminViewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.RE_EXAMS) { entry ->
            ReExamScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
                viewModel = entry.sharedExamViewModel(navController),
                adminViewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.MALPRACTICE) { entry ->
            MalpracticeScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
                viewModel = entry.sharedExamViewModel(navController),
                adminViewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_HUB) { entry ->
            ExamAdminScreen(
                onBack = { navController.popBackStack(fallbackRoute, false) },
                onOpenExamCentres = { navController.navigate(ExamDestinations.ADMIN_EXAM_CENTRES) },
                onOpenSeating = { navController.navigate(ExamDestinations.ADMIN_SEATING) },
                onOpenInvigilation = { navController.navigate(ExamDestinations.ADMIN_INVIGILATION) },
                onOpenHallTicketAdmin = { navController.navigate(ExamDestinations.ADMIN_HALL_TICKET) },
                onOpenQuestionPaper = { navController.navigate(ExamDestinations.ADMIN_QUESTION_PAPER) },
                onOpenExternalExam = { navController.navigate(ExamDestinations.ADMIN_EXTERNAL_EXAM) },
                onOpenInternalExam = { navController.navigate(ExamDestinations.ADMIN_INTERNAL_EXAM) },
                onOpenMarksGrading = { navController.navigate(ExamDestinations.ADMIN_MARKS_GRADING) },
                onOpenEvaluation = { navController.navigate(ExamDestinations.ADMIN_EVALUATION) },
                onOpenResultAdmin = { navController.navigate(ExamDestinations.ADMIN_RESULTS) },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_EXAM_CENTRES) { entry ->
            ExamCentreScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_SEATING) { entry ->
            SeatingPlanScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_INVIGILATION) { entry ->
            InvigilationScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_HALL_TICKET) { entry ->
            HallTicketAdminScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_QUESTION_PAPER) { entry ->
            QuestionPaperScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_EXTERNAL_EXAM) { entry ->
            ExternalExaminerScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_INTERNAL_EXAM) { entry ->
            InternalExamScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_MARKS_GRADING) { entry ->
            MarksGradingScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_EVALUATION) { entry ->
            EvaluationScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }

        composable(ExamDestinations.ADMIN_RESULTS) { entry ->
            ResultAdminScreen(
                onBack = { navController.popBackStack() },
                viewModel = entry.sharedExamAdminViewModel(navController)
            )
        }
    }
}

/**
 * Scopes one [ExamViewModel] to the graph rather than to each destination, so walking
 * Schedules -> Results -> back -> Results doesn't refetch results twice. [HallTicketScreen]
 * is deliberately excluded - it takes its own `scheduleId` nav arg via
 * [com.xsc.oneapp.feature.exam.ui.viewmodel.HallTicketViewModel] and has no shared
 * graph state to gain from this.
 */
@Composable
private fun NavBackStackEntry.sharedExamViewModel(navController: NavHostController): ExamViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(ExamDestinations.GRAPH_ROUTE)
    }
    return hiltViewModel(parentEntry)
}

/** Same graph-scoping as [sharedExamViewModel], kept as its own ViewModel rather than
 * folded into [ExamViewModel] - see [ExamAdminViewModel]'s own kdoc. */
@Composable
private fun NavBackStackEntry.sharedExamAdminViewModel(navController: NavHostController): ExamAdminViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry(ExamDestinations.GRAPH_ROUTE)
    }
    return hiltViewModel(parentEntry)
}
