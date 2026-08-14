package com.xsc.oneapp.navigation

import android.net.Uri
import com.xsc.oneapp.core.registry.ModuleRegistry
import com.xsc.oneapp.feature.attendance.navigation.AttendanceDestinations
import com.xsc.oneapp.feature.exam.navigation.ExamDestinations
import com.xsc.oneapp.feature.profile.navigation.ProfileDestinations

/** Navigation route constants for RootNavHost's top-level graph. */
object Routes {
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val VERIFY_OTP = "verify_otp/{resetToken}"
    const val RESET_PASSWORD = "reset_password/{resetToken}"
    const val DASHBOARD = "dashboard"

    /** Exams is a nested graph owned by :feature:exam - navigating here lands on its
     * start destination (the schedule list). Its internal routes (hall ticket,
     * results, revaluation, re-exams, malpractice) are not visible to :app. */
    const val EXAMS = ExamDestinations.GRAPH_ROUTE

    /** Attendance is a nested graph owned by :feature:attendance - navigating here
     * lands on its start destination (the overview). Its internal routes are not
     * visible to :app. */
    const val ATTENDANCE = AttendanceDestinations.GRAPH_ROUTE

    const val CURRICULUM = "curriculum"
    const val FEES = "fees"
    const val TIMETABLE = "timetable"
    const val MODULE_PATTERN = "module/{moduleName}"

    fun module(name: String) = "module/$name"

    /**
     * Resolves a backend-supplied module route (`"/fees"`, `"exams"`, `"/Academics"`)
     * to an in-app destination.
     *
     * Matching is done on a normalised key - lowercased, with surrounding slashes and
     * whitespace stripped - because a plain case-sensitive match on the raw string
     * would have meant a backend returning `"/Fees"` or `"fees/"` fell through to the
     * generic module template, producing a "Fees Module — Template Active"
     * placeholder that looked, to a user, exactly like the Fees feature being
     * missing.
     *
     * The alias-to-destination mapping itself now comes from [registry] -
     * [ModuleRegistry.findByRouteAlias] - each feature module's own `ModuleDefinition`
     * declares its accepted aliases (see AttendanceModule, ExamModule, etc.), rather
     * than being hardcoded here. `curriculum` is accepted alongside `academics` so
     * the backend can adopt the new name without a client release; the m_AAA contract
     * id remains `academics`. Anything the registry doesn't recognise still falls
     * through to the generic module template, exactly as before.
     */
    fun destinationFor(route: String, registry: ModuleRegistry): String {
        val key = route.trim().trim('/').lowercase()
        return registry.findByRouteAlias(key)?.navigationEntry ?: module(key)
    }

    fun verifyOtp(resetToken: String) = "verify_otp/${Uri.encode(resetToken)}"

    fun resetPassword(resetToken: String) = "reset_password/${Uri.encode(resetToken)}"
}
