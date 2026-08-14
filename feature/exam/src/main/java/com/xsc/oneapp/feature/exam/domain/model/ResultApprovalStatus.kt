package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §7.2 - one level of the multi-level result approval chain
 * (`"Results approved at level 'ADMIN'. Chain complete: False"` is the documented
 * `add` response text this is built from). */
data class ResultApprovalStatus(
    val scheduleId: String?,
    val level: String?,
    val chainComplete: Boolean?,
    val remarks: String?
)
