package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetAttendanceRecordsUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): List<AttendanceRecord> = repository.getAttendanceRecords()
}
