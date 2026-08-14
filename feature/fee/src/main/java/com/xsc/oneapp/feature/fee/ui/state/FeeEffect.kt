package com.xsc.oneapp.feature.fee.ui.state

/** One-shot feedback for a write action - same shape as
 * [com.xsc.oneapp.feature.exam.ui.state.ExamEffect]: every read section here is
 * already a [com.xsc.oneapp.core.result.SectionLoader], so only the submit outcome
 * needs a channel of its own. */
sealed class FeeEffect {
    data class ShowToast(val message: String) : FeeEffect()
}
