package com.xsc.oneapp.feature.timetable.domain.usecase

import com.xsc.oneapp.feature.timetable.domain.model.TimetableApproval
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import javax.inject.Inject

class GetTimetableApprovalsUseCase @Inject constructor(
    private val repository: TimetableRepository
) {
    suspend operator fun invoke(): List<TimetableApproval> = repository.getTimetableApprovals()
}
