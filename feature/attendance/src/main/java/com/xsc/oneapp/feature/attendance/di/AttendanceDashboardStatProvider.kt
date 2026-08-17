package com.xsc.oneapp.feature.attendance.di

import com.xsc.oneapp.core.dashboard.DashboardStatContribution
import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceShortageUseCase
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Feeds the Dashboard's "attendance" stat card (Home > Day at a Glance) from the same
 * shortage report the Attendance module's own screens already call -
 * ATTENDANCE_API_CONTRACT.md sm_shortage/attendanceShortage/view via
 * [GetAttendanceShortageUseCase] (see AttendanceRepositoryImpl.getMyShortageReport).
 * No new endpoint, no hardcoded percentage.
 *
 * The report is a row per session/course rather than a single overall number, so the
 * percentage here is presentSessions/totalSessions summed across every row - the same
 * "how much of my required attendance have I attended" figure the shortage screen is
 * built around, just collapsed to one number for the dashboard tile.
 */
class AttendanceDashboardStatProvider @Inject constructor(
    private val getAttendanceShortageUseCase: GetAttendanceShortageUseCase
) : DashboardStatProvider {

    override val statId: String = "attendance"

    override suspend fun provideStat(): DashboardStatContribution? {
        val rows = try {
            getAttendanceShortageUseCase()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // No session, request failed, etc. - fall back to the Dashboard's own
            // placeholder for this stat rather than showing a wrong number.
            return null
        }
        if (rows.isEmpty()) return null

        var totalSessions = 0
        var presentSessions = 0
        var isAtRisk = false
        for (row in rows) {
            totalSessions += row.totalSessions?.toIntOrNull() ?: 0
            presentSessions += row.presentSessions?.toIntOrNull() ?: 0
            val flaggedShortage = row.isShortage?.equals("true", ignoreCase = true) == true ||
                row.isShortage == "1"
            val highRisk = row.riskLevel?.let {
                it.equals("high", ignoreCase = true) || it.equals("critical", ignoreCase = true)
            } == true
            if (flaggedShortage || highRisk) isAtRisk = true
        }
        if (totalSessions <= 0) return null

        val percentage = (presentSessions * 100.0 / totalSessions)
            .coerceIn(0.0, 100.0)
            .roundToInt()

        return DashboardStatContribution(
            id = statId,
            title = "Overall Attendance",
            value = "$percentage%",
            tag = if (isAtRisk) "At Risk" else "On Track"
        )
    }
}
