package com.xsc.oneapp.feature.attendance.domain.usecase

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class GetAttendanceConfigurationsUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(): List<AttendanceConfiguration> = repository.getAttendanceConfigurations()
}
