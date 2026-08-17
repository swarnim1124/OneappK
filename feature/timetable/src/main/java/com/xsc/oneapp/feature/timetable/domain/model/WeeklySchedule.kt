package com.xsc.oneapp.feature.timetable.domain.model

/** One column of the weekly grid. */
data class ScheduleDay(
    /** Stable identity for cell lookup - the working day's row id when there is one. */
    val key: String,
    val label: String,
    val shortLabel: String,
    val isToday: Boolean
)

/**
 * A day x period grid assembled entirely client side from the three `view` calls
 * m_timetable already exposes: `workingDay` (columns), `timeSlot` (rows) and
 * `timetable` (cells).
 *
 * There is no backend action that returns a grid - `timetable:view` returns a flat
 * list of `tb_tt_entry` rows (contract v2 §4.1), which is why the Schedule tab was a
 * flat list. A student looking for "what do I have on Wednesday at 11" had to read
 * every row. The join is cheap and needs no contract change, so it is done here.
 *
 * The two joins that matter:
 *  - `tb_tt_entry.working_day_id` is a foreign key to `tb_working_day.id`, *not* a
 *    day-of-week number, so entries are matched on the working day's row id first.
 *    `day_of_week_id` and the day name are accepted as fallbacks because different
 *    deployments have been seen returning each.
 *  - contract v2's documented `timetable:view` response has no `day_of_week` field at
 *    all - only `working_day_id` - so a grid built purely on the day name would come
 *    back empty against a compliant backend. Both paths are supported.
 */
class WeeklySchedule internal constructor(
    val days: List<ScheduleDay>,
    val periods: List<TimeSlot>,
    private val cells: Map<String, List<TimetableEntry>>
) {
    fun entriesAt(day: ScheduleDay, period: TimeSlot): List<TimetableEntry> =
        cells[cellKey(day.key, period.id)].orEmpty()

    /** Nothing to draw - the caller should fall back to the list view or an empty state. */
    val isEmpty: Boolean get() = days.isEmpty() || periods.isEmpty()

    companion object {
        private val WEEK_ORDER = listOf(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"
        )

        private fun cellKey(dayKey: String, slotId: String?): String = "$dayKey#${slotId ?: "-"}"

        /**
         * @param today `java.time.DayOfWeek.name` for the current day, or null to skip
         *   today-highlighting. Passed in rather than read here so this stays a pure
         *   function and can be tested without freezing a clock.
         */
        fun build(
            entries: List<TimetableEntry>,
            workingDays: List<WorkingDay>,
            timeSlots: List<TimeSlot>,
            today: String?
        ): WeeklySchedule {
            val days = buildDays(entries, workingDays, today)
            val periods = timeSlots.sortedWith(
                compareBy({ it.slotSequence?.toIntOrNull() ?: Int.MAX_VALUE }, { it.startTime ?: "" })
            )

            val cells = mutableMapOf<String, MutableList<TimetableEntry>>()
            for (entry in entries) {
                val day = days.firstOrNull { matches(entry, it, workingDays) } ?: continue
                cells.getOrPut(cellKey(day.key, entry.timeSlotId)) { mutableListOf() }.add(entry)
            }

            return WeeklySchedule(days, periods, cells)
        }

        private fun buildDays(
            entries: List<TimetableEntry>,
            workingDays: List<WorkingDay>,
            today: String?
        ): List<ScheduleDay> {
            // Preferred source: the configured working-day pattern. Non-working days
            // are dropped so a five-day institution does not render two dead columns.
            val configured = workingDays
                .filter { it.isWorkingDay?.equalsTruthy() != false }
                .mapNotNull { day ->
                    val name = day.dayName ?: day.name ?: return@mapNotNull null
                    ScheduleDay(
                        key = day.id ?: day.dayOfWeekId ?: name.uppercase(),
                        label = name.prettyDay(),
                        shortLabel = name.take(3).uppercase(),
                        isToday = today != null && name.equals(today, ignoreCase = true)
                    )
                }
                .sortedBy { orderOf(it.label.uppercase()) }

            if (configured.isNotEmpty()) return configured

            // Fallback: the working-day tab has its own request and may not have loaded
            // (or may be unconfigured on this tenant). Deriving columns from the entries
            // themselves keeps the grid usable instead of showing nothing.
            return entries
                .mapNotNull { it.dayOfWeek ?: it.workingDayId }
                .distinct()
                .map { value ->
                    ScheduleDay(
                        key = value,
                        label = value.prettyDay(),
                        shortLabel = value.take(3).uppercase(),
                        isToday = today != null && value.equals(today, ignoreCase = true)
                    )
                }
                .sortedBy { orderOf(it.label.uppercase()) }
        }

        private fun matches(
            entry: TimetableEntry,
            day: ScheduleDay,
            workingDays: List<WorkingDay>
        ): Boolean {
            if (entry.workingDayId != null && entry.workingDayId == day.key) return true
            if (entry.dayOfWeek != null && entry.dayOfWeek.equals(day.label, ignoreCase = true)) {
                return true
            }

            // day.key is a tb_working_day row id; the entry may carry the day-of-week
            // number instead, so resolve through the working-day list once more.
            val configured = workingDays.firstOrNull { it.id == day.key || it.dayOfWeekId == day.key }
                ?: return false
            return entry.workingDayId != null &&
                (entry.workingDayId == configured.id || entry.workingDayId == configured.dayOfWeekId)
        }

        private fun orderOf(dayName: String): Int =
            WEEK_ORDER.indexOf(dayName.uppercase()).takeIf { it >= 0 } ?: WEEK_ORDER.size

        private fun String.prettyDay(): String =
            lowercase().replaceFirstChar { it.uppercase() }

        /** `is_working_day` arrives as a JSON boolean on some rows and "true"/"1" on
         * others, since these are raw ORM dictionaries. */
        private fun String.equalsTruthy(): Boolean =
            equals("true", ignoreCase = true) || this == "1"
    }
}
