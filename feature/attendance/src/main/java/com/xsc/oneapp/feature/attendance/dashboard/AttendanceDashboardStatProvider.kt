package com.xsc.oneapp.feature.attendance.dashboard

import com.xsc.oneapp.core.dashboard.DashboardStatContribution
import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.feature.attendance.domain.usecase.GetAttendanceShortageUseCase
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Feeds the Dashboard's "Attendance" stat card with a real percentage instead of the
 * permanent "Coming Soon" placeholder it showed before anything implemented this
 * extension point. Takes the first shortage row, same as AttendanceViewModel's own
 * overview headline (`shortageState.dataOrNull()?.firstOrNull()`), so the number here
 * always matches what the Attendance module itself shows - not a second, possibly
 * different aggregation.
 */
class AttendanceDashboardStatProvider @Inject constructor(
    private val getAttendanceShortageUseCase: GetAttendanceShortageUseCase
) : DashboardStatProvider {

    override val statId: String = "attendance"

    override suspend fun provideStat(): DashboardStatContribution? {
        // provideStat()'s own contract (see DashboardStatProvider) is to return null on
        // a failed request, not to let it propagate - an HTTP/business/network failure
        // here was previously uncaught and crashed the whole Dashboard right after
        // login, since this runs inside DashboardViewModel's viewModelScope.launch with
        // no exception handler.
        val shortage = try {
            getAttendanceShortageUseCase().firstOrNull() ?: return null
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        val percentage = shortage.attendancePercentage ?: return null
        val tag = when (shortage.riskLevel?.lowercase()) {
            "high", "critical" -> "At risk"
            "medium" -> "Watch"
            else -> "On track"
        }
        return DashboardStatContribution(
            id = statId,
            title = "ATTENDANCE",
            value = "$percentage%",
            tag = tag
        )
    }
}
