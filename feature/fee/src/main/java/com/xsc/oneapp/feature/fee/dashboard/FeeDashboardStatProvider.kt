package com.xsc.oneapp.feature.fee.dashboard

import com.xsc.oneapp.core.dashboard.DashboardStatContribution
import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.feature.fee.domain.repository.FeeRepository
import com.xsc.sdk.network.APIError
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

/**
 * Feeds the Dashboard's "PENDING FEES" stat card from the same statement
 * (`feeInvoice:view` -> [com.xsc.oneapp.feature.fee.domain.model.FeeStatement]) the
 * Fees module itself reads, so the two never disagree - previously the Dashboard's
 * pending-fees card was a static "$4,250" with no data source at all (see
 * HomeGlanceComponents.PendingFeesCard).
 *
 * Registered via [com.xsc.oneapp.feature.fee.di.FeeModule]'s `@Binds @IntoSet`, which
 * is the extension point [DashboardStatProvider] documents - :feature:dashboard never
 * depends on :feature:fee, only on :core where the interface lives.
 *
 * Returns null (leaving the Dashboard's own "Coming Soon" placeholder in place) when
 * there is no session, the call fails, or the backend hasn't returned anything usable
 * yet - never a fabricated amount.
 */
class FeeDashboardStatProvider @Inject constructor(
    private val feeRepository: FeeRepository
) : DashboardStatProvider {

    override val statId: String = STAT_ID

    override suspend fun provideStat(): DashboardStatContribution? {
        val statement = try {
            feeRepository.getMyFeeStatement()
        } catch (e: CancellationException) {
            throw e
        } catch (e: APIError) {
            return null
        } catch (e: Exception) {
            return null
        }

        if (statement.isEmpty) return null

        val due = statement.payableAmount
        return if (due != null) {
            DashboardStatContribution(
                id = STAT_ID,
                title = "Pending Fees",
                value = formatInr(due),
                tag = TAG_DUE
            )
        } else {
            DashboardStatContribution(
                id = STAT_ID,
                title = "Pending Fees",
                value = formatInr(0.0),
                tag = TAG_PAID
            )
        }
    }

    companion object {
        const val STAT_ID = "fees"

        /** Matches the "there is a real amount due" branch the Dashboard cards key off. */
        const val TAG_DUE = "Due now"

        /** Matches the "backend confirmed nothing outstanding" branch. */
        const val TAG_PAID = "All paid"
    }
}

/** Same grouped-rupee style as FeeScreen's own formatAmount, minus the decimals - this
 * value lives on a compact stat card, not a ledger line. */
private fun formatInr(amount: Double): String = "₹%,.0f".format(amount)
