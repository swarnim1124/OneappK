package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §6.1/§6.2 - a single student's marks row, shared shape for both
 * `internalMarksEntry` and end-semester `marksEntry` (both take a `records[]` of
 * `{studentId, marks}` and neither documents a separate response schema for `view`,
 * so field names here are inferred from the `add` payload's own vocabulary, not
 * confirmed against a real response example - same honesty caveat already applied to
 * [ReappearEligibility]. */
data class MarksRecord(
    val id: String?,
    val scheduleId: String?,
    val courseId: String?,
    val studentId: String?,
    val marks: String?,
    val status: String?
)
