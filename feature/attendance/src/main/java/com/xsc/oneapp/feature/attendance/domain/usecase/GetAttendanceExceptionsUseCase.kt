package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetAttendanceExceptionsUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): List<AttendanceCorrectionRequest> = repository.getAttendanceExceptions()
}
