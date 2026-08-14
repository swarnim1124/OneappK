package com.xsc.oneapp.feature.exam.ui.state

/** One-shot feedback for a write action (revaluation, photocopy, appeal, challenge,
 * supplementary/reappear registration). There is no per-screen Event/State pair the
 * way Profile has - every read section here is a [com.xsc.oneapp.core.result.SectionLoader]
 * already, so only the submit outcome needs a channel of its own. */
sealed class ExamEffect {
    data class ShowToast(val message: String) : ExamEffect()
}
