package com.xsc.oneapp.feature.fee.dashboard

import com.xsc.oneapp.core.dashboard.DashboardStatContribution
import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.feature.fee.domain.usecase.GetMyFeeAssignmentsUseCase
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Feeds the Dashboard's "Pending Fees" stat card with a real total instead of the
 * permanent "Coming Soon" placeholder. Sums every assignment's totalAmount - there's
 * no separate "paid"/"balance" field on FeeAssignment to net off already-paid amounts
 * against (see FeeAssignment.kt), so this is the total assigned, not strictly the
 * outstanding balance. Same number the Dues tab's cards already show individually,
 * just totalled.
 */
class FeeDashboardStatProvider @Inject constructor(
    private val getMyFeeAssignmentsUseCase: GetMyFeeAssignmentsUseCase
) : DashboardStatProvider {

    override val statId: String = "fees"

    override suspend fun provideStat(): DashboardStatContribution? {
        // provideStat()'s own contract (see DashboardStatProvider) is to return null on
        // a failed request, not to let it propagate - same crash class fixed in
        // AttendanceDashboardStatProvider (an uncaught failure here previously took
        // down the whole Dashboard right after login).
        val assignments = try {
            getMyFeeAssignmentsUseCase()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return null
        }
        if (assignments.isEmpty()) return null
        val total = assignments.sumOf { it.totalAmount?.toDoubleOrNull() ?: 0.0 }
        return DashboardStatContribution(
            id = statId,
            title = "PENDING FEES",
            value = "₹${"%,.0f".format(total)}",
            tag = "Due"
        )
    }
}
