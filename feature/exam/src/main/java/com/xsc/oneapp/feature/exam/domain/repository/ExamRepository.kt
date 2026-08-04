package com.xsc.oneapp.feature.exam.domain.repository

import com.xsc.oneapp.feature.exam.domain.model.ChallengeRevaluation
import com.xsc.oneapp.feature.exam.domain.model.ExamResult
import com.xsc.oneapp.feature.exam.domain.model.ExamSchedule
import com.xsc.oneapp.feature.exam.domain.model.HallTicket
import com.xsc.oneapp.feature.exam.domain.model.RevaluationRequest

interface ExamRepository {
    suspend fun getExamSchedules(): List<ExamSchedule>
    suspend fun getHallTicket(scheduleId: String): List<HallTicket>
    suspend fun getMyResults(): List<ExamResult>
    suspend fun getMyRevaluationRequests(): List<RevaluationRequest>
    suspend fun getMyChallengeRevaluations(): List<ChallengeRevaluation>
}
