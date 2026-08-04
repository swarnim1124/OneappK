package com.xsc.oneapp.feature.timetable.data.network

/** m_timetable contract confirmed 2026-07-31 (gateway routing table). 8 view
 * actions across 5 submodules - see TimetableNotes.kt for the fuller scope
 * decision, raw-ID-resolution note, and RBAC architecture. sm_approval is marked
 * "(optional)" in the routing table but dispatches the same as any other subMod. */
object TimetableEndpoint {
    const val MODULE = "m_timetable"

    object SubModules {
        const val CONFIGURATION = "sm_configuration"
        const val SCHEDULE = "sm_schedule"
        const val ALLOCATION = "sm_allocation"
        const val SUBSTITUTION = "sm_substitution"
        const val APPROVAL = "sm_approval"
    }

    object Actions {
        const val WORKING_DAY = "workingDay"
        const val TIME_SLOT = "timeSlot"
        const val ACADEMIC_CALENDAR = "academicCalendar"
        const val TIMETABLE = "timetable"
        const val FACULTY_ALLOCATION = "facultyAllocation"
        const val ROOM_ALLOCATION = "roomAllocation"
        const val SUBSTITUTION = "substitution"
        const val TIMETABLE_APPROVAL = "timetableApproval"
    }

    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
        const val DELETE = "delete"
    }
}
