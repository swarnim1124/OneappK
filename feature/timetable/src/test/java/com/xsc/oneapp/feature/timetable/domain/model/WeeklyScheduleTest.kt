package com.xsc.oneapp.feature.timetable.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Field values below follow the contract v2 §4.1 response shape - note it carries
 * `working_day_id` and no `day_of_week`, which is the case the grid has to get right.
 */
class WeeklyScheduleTest {

    private fun entry(
        id: String,
        workingDayId: String?,
        timeSlotId: String?,
        dayOfWeek: String? = null,
        roomId: String? = null
    ) = TimetableEntry(
        id = id, ttId = "1", institutionId = "1", academicYearId = "2026", termId = "1",
        programId = null, semesterId = null, sectionId = "1", courseId = null,
        courseOfferingId = "101", facultyId = "12", facultyCourseAssignmentId = "12",
        workingDayId = workingDayId, dayOfWeek = dayOfWeek, timeSlotId = timeSlotId,
        roomId = roomId, sessionTypeId = "LECTURE", startDate = null, endDate = null,
        isActive = "true", ttCode = "TT_CSE_S3_A", ttStatus = "PUBLISHED"
    )

    private fun workingDay(id: String, dowId: String, name: String, working: String = "true") =
        WorkingDay(
            id = id, institutionId = "1", academicYearId = "2026", name = null,
            effectiveFrom = null, effectiveTo = null, dayOfWeekId = dowId, dayName = name,
            isWorkingDay = working, isActive = "true"
        )

    private fun slot(id: String, seq: String, name: String, isBreak: String = "false") =
        TimeSlot(
            id = id, institutionId = "1", slotName = name, startTime = "09:00:00",
            endTime = "10:00:00", slotSequence = seq, isBreak = isBreak, isActive = "true"
        )

    private val week = listOf(
        workingDay("1", "1", "MONDAY"),
        workingDay("2", "2", "TUESDAY"),
        workingDay("3", "3", "WEDNESDAY")
    )

    @Test
    fun `entries are placed by working day id, which is what the contract returns`() {
        val monday9 = entry("1", workingDayId = "1", timeSlotId = "10")
        val schedule = WeeklySchedule.build(
            entries = listOf(monday9),
            workingDays = week,
            timeSlots = listOf(slot("10", "1", "Period 1")),
            today = null
        )

        val monday = schedule.days.first { it.label == "Monday" }
        assertEquals(listOf(monday9), schedule.entriesAt(monday, schedule.periods.first()))
        assertTrue(
            schedule.entriesAt(
                schedule.days.first { it.label == "Tuesday" },
                schedule.periods.first()
            ).isEmpty()
        )
    }

    @Test
    fun `columns follow calendar order, not the order rows arrived in`() {
        val schedule = WeeklySchedule.build(
            entries = emptyList(),
            workingDays = week.reversed(),
            timeSlots = listOf(slot("10", "1", "Period 1")),
            today = null
        )

        assertEquals(listOf("Monday", "Tuesday", "Wednesday"), schedule.days.map { it.label })
    }

    @Test
    fun `non-working days are not given a column`() {
        val schedule = WeeklySchedule.build(
            entries = emptyList(),
            workingDays = week + workingDay("7", "7", "SUNDAY", working = "false"),
            timeSlots = listOf(slot("10", "1", "Period 1")),
            today = null
        )

        assertFalse(schedule.days.any { it.label == "Sunday" })
    }

    @Test
    fun `rows follow slot sequence, not slot id`() {
        val schedule = WeeklySchedule.build(
            entries = emptyList(),
            workingDays = week,
            timeSlots = listOf(slot("30", "3", "Period 3"), slot("10", "1", "Period 1")),
            today = null
        )

        assertEquals(listOf("Period 1", "Period 3"), schedule.periods.map { it.slotName })
    }

    @Test
    fun `columns fall back to the entries themselves when working days are unconfigured`() {
        val schedule = WeeklySchedule.build(
            entries = listOf(entry("1", workingDayId = null, timeSlotId = "10", dayOfWeek = "MONDAY")),
            workingDays = emptyList(),
            timeSlots = listOf(slot("10", "1", "Period 1")),
            today = null
        )

        assertEquals(listOf("Monday"), schedule.days.map { it.label })
        assertEquals(1, schedule.entriesAt(schedule.days.first(), schedule.periods.first()).size)
    }

    @Test
    fun `two entries in one cell are both kept so the clash is visible`() {
        val schedule = WeeklySchedule.build(
            entries = listOf(
                entry("1", workingDayId = "1", timeSlotId = "10", roomId = "301"),
                entry("2", workingDayId = "1", timeSlotId = "10", roomId = "302")
            ),
            workingDays = week,
            timeSlots = listOf(slot("10", "1", "Period 1")),
            today = null
        )

        assertEquals(2, schedule.entriesAt(schedule.days.first(), schedule.periods.first()).size)
    }

    @Test
    fun `today is flagged on the matching column only`() {
        val schedule = WeeklySchedule.build(
            entries = emptyList(),
            workingDays = week,
            timeSlots = listOf(slot("10", "1", "Period 1")),
            today = "TUESDAY"
        )

        assertEquals(listOf("Tuesday"), schedule.days.filter { it.isToday }.map { it.label })
    }

    @Test
    fun `no time slots means no grid, so the caller can fall back to the list`() {
        val schedule = WeeklySchedule.build(
            entries = listOf(entry("1", "1", "10")),
            workingDays = week,
            timeSlots = emptyList(),
            today = null
        )

        assertTrue(schedule.isEmpty)
    }
}
