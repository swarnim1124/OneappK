package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §8.3 - per-course eligibility for a reappear exam. The contract
 * documents this route as rule-setting (`add`) and does not fix a response shape for
 * `view`, so [isEligible] is read from whichever of several candidate flag fields the
 * backend row actually carries, and is null when none is present. */
data class ReappearEligibility(
    val id: String?,
    val studentId: String?,
    val courseId: String?,
    val reappearExamId: String?,
    val isEligible: Boolean?,
    val reason: String?,
    val status: String?
)
