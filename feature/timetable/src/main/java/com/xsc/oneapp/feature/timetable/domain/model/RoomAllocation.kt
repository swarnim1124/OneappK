package com.xsc.oneapp.feature.timetable.domain.model

/** Timetable entries that have a room assigned - a filtered view over
 * tb_tt_entry, not its own table (see TimetableEntry). */
data class RoomAllocation(
    val id: String?,
    val ttId: String?,
    val roomId: String?,
    val dayOfWeek: String?,
    val timeSlotId: String?
)
