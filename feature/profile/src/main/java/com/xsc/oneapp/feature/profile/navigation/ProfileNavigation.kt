package com.xsc.oneapp.feature.profile.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.xsc.oneapp.core.result.UiState
import com.xsc.oneapp.core.result.deniesRoute
import com.xsc.oneapp.feature.profile.ui.screen.*

object ProfileDestinations {
    const val PROFILE_ROUTE = "profile_route"
    const val PROFILE_DASHBOARD = "profile_dashboard"
    const val PERSONAL_DETAIL = "personal_detail"
    const val ACADEMIC_DETAIL = "academic_detail"
    const val FAMILY_DETAIL = "family_detail"
    const val EMERGENCY_CONTACT = "emergency_contact"
    const val MEDICAL_DETAIL = "medical_detail"
    const val USER_PREFERENCE = "user_preference"
}

/**
 * [accessibleRoutes] and [fallbackRoute] gate only [ProfileDestinations.PROFILE_DASHBOARD] -
 * this graph's start destination and only entry point from outside this module. Every
 * other screen here is reached via onNavigateTo from a user already inside the
 * dashboard, so it doesn't need its own independent gate - same precedent as
 * HALL_TICKET / AttendanceNavigation's Records/Requests/Policy.
 */
fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    accessibleRoutes: UiState<Set<String>>,
    fallbackRoute: String
) {
    navigation(
        startDestination = ProfileDestinations.PROFILE_DASHBOARD,
        route = ProfileDestinations.PROFILE_ROUTE
    ) {
        composable(route = ProfileDestinations.PROFILE_DASHBOARD) {
            if (accessibleRoutes.deniesRoute(ProfileDestinations.PROFILE_ROUTE)) {
                LaunchedEffect(Unit) {
                    navController.navigate(fallbackRoute) {
                        popUpTo(fallbackRoute) { inclusive = true }
                    }
                }
            } else {
                ProfileDashboardScreen(
                    onNavigateTo = { route -> navController.navigate(route) },
                    onNavigateBack = { navController.popBackStack(fallbackRoute, false) }
                )
            }
        }
        // Each sub-screen pops straight to fallbackRoute (Dashboard), not
        // navController.popBackStack() - a plain pop would only return to
        // ProfileDashboardScreen, one hop up inside this nested graph, not Dashboard.
        composable(route = ProfileDestinations.PERSONAL_DETAIL) {
            PersonalDetailScreen(onNavigateBack = { navController.popBackStack(fallbackRoute, false) })
        }
        composable(route = ProfileDestinations.ACADEMIC_DETAIL) {
            AcademicDetailScreen(onNavigateBack = { navController.popBackStack(fallbackRoute, false) })
        }
        composable(route = ProfileDestinations.FAMILY_DETAIL) {
            FamilyDetailScreen(onNavigateBack = { navController.popBackStack(fallbackRoute, false) })
        }
        composable(route = ProfileDestinations.EMERGENCY_CONTACT) {
            EmergencyContactScreen(onNavigateBack = { navController.popBackStack(fallbackRoute, false) })
        }
        composable(route = ProfileDestinations.MEDICAL_DETAIL) {
            MedicalDetailScreen(onNavigateBack = { navController.popBackStack(fallbackRoute, false) })
        }
        composable(route = ProfileDestinations.USER_PREFERENCE) {
            UserPreferenceScreen(onNavigateBack = { navController.popBackStack(fallbackRoute, false) })
        }
    }
}
