package com.xsc.oneapp.feature.timetable.domain.repository

import com.xsc.oneapp.feature.timetable.domain.model.AcademicCalendar
import com.xsc.oneapp.feature.timetable.domain.model.FacultyAllocation
import com.xsc.oneapp.feature.timetable.domain.model.RoomAllocation
import com.xsc.oneapp.feature.timetable.domain.model.Substitution
import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.model.TimetableApproval
import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.model.WorkingDay

interface TimetableRepository {
    suspend fun getTimetableEntries(): List<TimetableEntry>
    suspend fun getWorkingDays(): List<WorkingDay>
    suspend fun getTimeSlots(): List<TimeSlot>
    suspend fun getAcademicCalendar(): AcademicCalendar?
    suspend fun getFacultyAllocations(): List<FacultyAllocation>
    suspend fun getRoomAllocations(): List<RoomAllocation>
    suspend fun getSubstitutions(): List<Substitution>
    suspend fun getTimetableApprovals(): List<TimetableApproval>
}
