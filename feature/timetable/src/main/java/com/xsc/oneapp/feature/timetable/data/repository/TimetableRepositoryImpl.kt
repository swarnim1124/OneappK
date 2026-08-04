package com.xsc.oneapp.feature.timetable.data.repository

import com.google.gson.JsonElement
import com.xsc.oneapp.core.json.JsonRowUtils
import com.xsc.oneapp.feature.timetable.data.mapper.toAcademicCalendar
import com.xsc.oneapp.feature.timetable.data.mapper.toFacultyAllocation
import com.xsc.oneapp.feature.timetable.data.mapper.toRoomAllocation
import com.xsc.oneapp.feature.timetable.data.mapper.toSubstitution
import com.xsc.oneapp.feature.timetable.data.mapper.toTimeSlot
import com.xsc.oneapp.feature.timetable.data.mapper.toTimetableApproval
import com.xsc.oneapp.feature.timetable.data.mapper.toTimetableEntry
import com.xsc.oneapp.feature.timetable.data.mapper.toWorkingDay
import com.xsc.oneapp.feature.timetable.data.network.TimetableEndpoint
import com.xsc.oneapp.feature.timetable.domain.model.AcademicCalendar
import com.xsc.oneapp.feature.timetable.domain.model.FacultyAllocation
import com.xsc.oneapp.feature.timetable.domain.model.RoomAllocation
import com.xsc.oneapp.feature.timetable.domain.model.Substitution
import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.model.TimetableApproval
import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.model.WorkingDay
import com.xsc.oneapp.feature.timetable.domain.repository.TimetableRepository
import com.xsc.sdk.auth.SessionManager
import com.xsc.sdk.network.APIClient
import javax.inject.Inject

/** No request-payload example was confirmed for any of these 8 view calls - only
 * the response shapes were. Every response row here does carry "inst_id" though,
 * so as a defensive-optional inference (not a confirmed requirement) this sends
 * it when the session has one, matching m_fees's request-key casing for the same
 * field - same "doesn't hurt if unused, helps if required" reasoning as exam's
 * getExamSchedules(). No other filter (secId/termId/scheduleId/...) is guessed. */
class TimetableRepositoryImpl @Inject constructor(
    private val apiClient: APIClient,
    private val sessionManager: SessionManager
) : TimetableRepository {

    private fun instIdPayload(): MutableMap<String, Any> {
        val payload = mutableMapOf<String, Any>()
        sessionManager.getInstitutionId()?.let { payload["inst_id"] = it }
        return payload
    }

    /** m_timetable contract: sm_schedule/timetable/view. */
    override suspend fun getTimetableEntries(): List<TimetableEntry> {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.SCHEDULE,
            action = TimetableEndpoint.Actions.TIMETABLE,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toTimetableEntry() }
    }

    /** m_timetable contract: sm_configuration/workingDay/view. */
    override suspend fun getWorkingDays(): List<WorkingDay> {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.CONFIGURATION,
            action = TimetableEndpoint.Actions.WORKING_DAY,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toWorkingDay() }
    }

    /** m_timetable contract: sm_configuration/timeSlot/view. */
    override suspend fun getTimeSlots(): List<TimeSlot> {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.CONFIGURATION,
            action = TimetableEndpoint.Actions.TIME_SLOT,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toTimeSlot() }
    }

    /** m_timetable contract: sm_configuration/academicCalendar/view - a single
     * proxy-metadata object, not a row list (see AcademicCalendar's doc comment). */
    override suspend fun getAcademicCalendar(): AcademicCalendar? {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.CONFIGURATION,
            action = TimetableEndpoint.Actions.ACADEMIC_CALENDAR,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).firstOrNull()?.toAcademicCalendar()
    }

    /** m_timetable contract: sm_allocation/facultyAllocation/view. */
    override suspend fun getFacultyAllocations(): List<FacultyAllocation> {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.ALLOCATION,
            action = TimetableEndpoint.Actions.FACULTY_ALLOCATION,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toFacultyAllocation() }
    }

    /** m_timetable contract: sm_allocation/roomAllocation/view. */
    override suspend fun getRoomAllocations(): List<RoomAllocation> {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.ALLOCATION,
            action = TimetableEndpoint.Actions.ROOM_ALLOCATION,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toRoomAllocation() }
    }

    /** m_timetable contract: sm_substitution/substitution/view. */
    override suspend fun getSubstitutions(): List<Substitution> {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.SUBSTITUTION,
            action = TimetableEndpoint.Actions.SUBSTITUTION,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toSubstitution() }
    }

    /** m_timetable contract: sm_approval/timetableApproval/view. */
    override suspend fun getTimetableApprovals(): List<TimetableApproval> {
        val data = apiClient.request<JsonElement?>(
            module = TimetableEndpoint.MODULE,
            submodule = TimetableEndpoint.SubModules.APPROVAL,
            action = TimetableEndpoint.Actions.TIMETABLE_APPROVAL,
            actionType = TimetableEndpoint.ActionTypes.VIEW,
            payload = instIdPayload()
        )
        return JsonRowUtils.asRows(data).map { it.toTimetableApproval() }
    }
}
