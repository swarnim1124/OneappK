package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetAttendanceTypesUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): List<AttendanceType> = repository.getAttendanceTypes()
}
