package com.xsc.oneapp.feature.exam.domain.usecase

import com.xsc.oneapp.feature.exam.domain.repository.ExamRepository
import javax.inject.Inject

/** Admin write actions for m_exam contract §3.2 (sm_schedule): authoring and the
 * publish/hold/unpublish workflow. The read side ([GetExamSchedulesUseCase]) is
 * shared with the student-facing schedule list. */

class AddExamScheduleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        name: String,
        examType: String,
        fromDate: String,
        toDate: String,
        sessionDate: String,
        session: String
    ) = repository.addExamSchedule(name, examType, fromDate, toDate, sessionDate, session)
}

class UpdateExamScheduleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(
        scheduleId: String,
        name: String? = null,
        fromDate: String? = null,
        toDate: String? = null
    ) = repository.updateExamSchedule(scheduleId, name, fromDate, toDate)
}

class DeleteExamScheduleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.deleteExamSchedule(scheduleId)
}

class PublishExamScheduleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.publishExamSchedule(scheduleId)
}

class HoldExamScheduleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.holdExamSchedule(scheduleId)
}

class UnpublishExamScheduleUseCase @Inject constructor(
    private val repository: ExamRepository
) {
    suspend operator fun invoke(scheduleId: String) = repository.unpublishExamSchedule(scheduleId)
}
