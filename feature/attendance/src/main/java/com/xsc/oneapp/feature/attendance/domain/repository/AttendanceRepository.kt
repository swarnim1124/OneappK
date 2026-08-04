package com.xsc.oneapp.feature.attendance.domain.repository

import com.xsc.oneapp.feature.attendance.domain.model.AttendanceConfiguration
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceCorrectionRequest
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceRecord
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceSession
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceShortage
import com.xsc.oneapp.feature.attendance.domain.model.AttendanceType

interface AttendanceRepository {
    suspend fun getMyShortageReport(): List<AttendanceShortage>
    suspend fun getAttendanceConfigurations(): List<AttendanceConfiguration>
    suspend fun getAttendanceTypes(): List<AttendanceType>
    suspend fun getAttendanceSessions(): List<AttendanceSession>
    suspend fun getSubmissionComplianceReport(): List<AttendanceSession>
    suspend fun getAttendanceRecords(): List<AttendanceRecord>
    suspend fun getAttendanceExceptions(): List<AttendanceCorrectionRequest>
    suspend fun getCondonations(): List<AttendanceCorrectionRequest>
}
