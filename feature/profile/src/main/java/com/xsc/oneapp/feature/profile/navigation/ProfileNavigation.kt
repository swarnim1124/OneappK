package com.xsc.oneapp.feature.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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
    const val SECURITY = "security"
}

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    navigation(
        startDestination = ProfileDestinations.PROFILE_DASHBOARD,
        route = ProfileDestinations.PROFILE_ROUTE
    ) {
        composable(route = ProfileDestinations.PROFILE_DASHBOARD) {
            ProfileDashboardScreen(
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }
        composable(route = ProfileDestinations.PERSONAL_DETAIL) {
            PersonalDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = ProfileDestinations.ACADEMIC_DETAIL) {
            AcademicDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = ProfileDestinations.FAMILY_DETAIL) {
            FamilyDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = ProfileDestinations.EMERGENCY_CONTACT) {
            EmergencyContactScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = ProfileDestinations.MEDICAL_DETAIL) {
            MedicalDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = ProfileDestinations.USER_PREFERENCE) {
            UserPreferenceScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(route = ProfileDestinations.SECURITY) {
            SecurityScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
