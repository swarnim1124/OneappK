package com.xsc.oneapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.xsc.oneapp.feature.dashboard.ui.screen.DashboardScreen
import com.xsc.oneapp.feature.login.ui.effect.ForgotPasswordEffect
import com.xsc.oneapp.feature.login.ui.screen.LoginScreen
import com.xsc.oneapp.feature.login.ui.screen.ResetPasswordScreen
import com.xsc.oneapp.feature.login.ui.screen.VerifyOtpScreen
import com.xsc.sdk.auth.SessionManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.xsc.oneapp.feature.profile.navigation.profileGraph
import com.xsc.oneapp.feature.exam.ui.screen.ExamScreen
import com.xsc.oneapp.feature.exam.ui.screen.HallTicketScreen
import com.xsc.oneapp.feature.attendance.navigation.attendanceGraph
import com.xsc.oneapp.feature.curriculum.ui.screen.CurriculumScreen
import com.xsc.oneapp.feature.fee.ui.screen.FeeScreen
import com.xsc.oneapp.feature.timetable.ui.screen.TimetableScreen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RootNavHost(
    navController: NavHostController,
    sessionManager: SessionManager = hiltViewModel<com.xsc.oneapp.MainViewModel>().sessionManager
) {
    // Read auth state ONCE to pick the cold-start destination only. NavHost keys its
    // graph off `startDestination` - if this were reactive (collectAsState), flipping
    // isAuthenticated right after login would rebuild the graph and re-enter
    // "dashboard" a second time, on top of the explicit navigate("dashboard") call
    // LoginScreen already issues. That's what was spawning a second DashboardViewModel
    // (and firing accessibleModules twice) immediately after every login. All
    // post-launch transitions go through the explicit navigate() calls below instead.
    val startDestination = remember { if (sessionManager.isAuthenticated.value) Routes.DASHBOARD else Routes.LOGIN }

    // Reactive auth state: the only place in the app that navigates to Login in
    // response to the session actually ending, rather than every screen that has a
    // sign-out button doing it themselves. Previously `isAuthenticated` was read once
    // here for the cold-start destination and never observed again, so a session that
    // ended *during* use - a forced logout from TokenAuthenticator when refresh fails,
    // for instance - left the user stranded on whatever screen they were on with every
    // subsequent call failing, instead of being routed back to Login (see
    // PRODUCTION_READINESS_FINAL.md risk #4 / Day 6-8 plan: "reactive auth state").
    //
    // Only reacts to a true -> false transition, not to "currently unauthenticated",
    // so it doesn't fire while the user is still working through the pre-auth screens
    // (forgot password / OTP / reset), which are unauthenticated the whole time.
    LaunchedEffect(sessionManager) {
        var wasAuthenticated = sessionManager.isAuthenticated.value
        sessionManager.isAuthenticated.collect { authenticated ->
            if (wasAuthenticated && !authenticated) {
                // popUpTo the root graph's own id (not a specific destination) is
                // the documented way to clear the entire back stack regardless of
                // where the user was when the session ended - popUpTo(0) looks
                // similar but 0 isn't a real destination id in Compose Navigation's
                // hash-based ids, so it would silently pop nothing.
                navController.navigate(Routes.LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            }
            wasAuthenticated = authenticated
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            val viewModel: com.xsc.oneapp.feature.login.ui.viewmodel.ForgotPasswordViewModel = hiltViewModel()
            val state by viewModel.state.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.effect.collectLatest { effect ->
                    when (effect) {
                        is ForgotPasswordEffect.NavigateToVerifyOtp ->
                            navController.navigate(Routes.verifyOtp(effect.resetToken))
                    }
                }
            }

            com.xsc.oneapp.feature.login.ui.screen.ForgotPasswordScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.VERIFY_OTP,
            arguments = listOf(navArgument("resetToken") { type = NavType.StringType })
        ) {
            VerifyOtpScreen(
                onNavigateToResetPassword = { resetToken ->
                    navController.navigate(Routes.resetPassword(resetToken))
                }
            )
        }

        composable(
            Routes.RESET_PASSWORD,
            arguments = listOf(navArgument("resetToken") { type = NavType.StringType })
        ) {
            ResetPasswordScreen(
                onBackToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToModule = { route ->
                    navController.navigate(Routes.destinationFor(route))
                },
                // Navigation itself is handled by the reactive isAuthenticated
                // effect above, once DashboardViewModel.logout()'s
                // sessionManager.clearSession() flips it to false - this callback
                // firing an explicit navigate() too would race the reactive one and
                // risk a duplicate back-stack entry.
                onLogout = {},
                onChangePassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.EXAMS) {
            ExamScreen(
                onBack = { navController.popBackStack() },
                onScheduleClick = { scheduleId -> navController.navigate(Routes.hallTicket(scheduleId)) }
            )
        }

        composable(
            Routes.HALL_TICKET,
            arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
        ) {
            HallTicketScreen(onBack = { navController.popBackStack() })
        }

        // The Attendance feature owns its internal destinations; :app only mounts the
        // graph. Routes.ATTENDANCE is an alias for its public entry point.
        attendanceGraph(navController)

        composable(Routes.CURRICULUM) {
            CurriculumScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.FEES) {
            FeeScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.TIMETABLE) {
            TimetableScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.MODULE_PATTERN) { backStackEntry ->
            val moduleName = backStackEntry.arguments?.getString("moduleName") ?: "Unknown"
            com.xsc.oneapp.feature.dashboard.ui.screen.DummyModuleScreen(
                moduleName = moduleName,
                onBack = { navController.popBackStack() }
            )
        }
        
        profileGraph(navController)
    }
}
