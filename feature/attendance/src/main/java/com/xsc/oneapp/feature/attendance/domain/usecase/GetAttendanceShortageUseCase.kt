package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceShortage
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetAttendanceShortageUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): List<AttendanceShortage> = repository.getMyShortageReport()
}
