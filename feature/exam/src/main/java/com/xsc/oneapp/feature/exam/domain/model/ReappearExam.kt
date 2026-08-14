package com.xsc.oneapp.feature.exam.domain.model

/** m_exam contract §8.3 - a reappear/improvement examination session. Unlike a
 * supplementary session this carries its own registration window, so the UI can say
 * whether registration is open rather than only when the exam itself runs. */
data class ReappearExam(
    val id: String?,
    val name: String?,
    val academicTermId: String?,
    val examStartDate: String?,
    val examEndDate: String?,
    val registrationStartDate: String?,
    val registrationEndDate: String?,
    val status: String?,
    val courses: List<String>
)
