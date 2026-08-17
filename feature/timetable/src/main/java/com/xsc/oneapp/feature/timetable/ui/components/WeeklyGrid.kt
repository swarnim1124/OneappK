package com.xsc.oneapp.feature.timetable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xsc.oneapp.feature.timetable.domain.model.ScheduleDay
import com.xsc.oneapp.feature.timetable.domain.model.TimeSlot
import com.xsc.oneapp.feature.timetable.domain.model.TimetableEntry
import com.xsc.oneapp.feature.timetable.domain.model.WeeklySchedule

private val PERIOD_COLUMN_WIDTH = 84.dp
private val DAY_COLUMN_WIDTH = 116.dp
private val ROW_HEIGHT = 76.dp

/**
 * The week as a grid: one column per working day, one row per period.
 *
 * Horizontally scrollable rather than squeezed to fit - six days at a legible width
 * does not fit a phone, and a grid you cannot read the course codes in is worse than
 * the list it replaced. The period column is inside the same scroll region: pinning
 * it would need a nested-scroll dance that fights the parent LazyColumn, and periods
 * are the axis users scan down, not across.
 */
@Composable
fun WeeklyTimetableGrid(
    schedule: WeeklySchedule,
    modifier: Modifier = Modifier
) {
    val horizontalScroll = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(start = 12.dp, end = 12.dp, top = 8.dp)
        ) {
            Box(modifier = Modifier.width(PERIOD_COLUMN_WIDTH))
            schedule.days.forEach { day -> DayHeader(day) }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(schedule.periods, key = { it.id ?: it.hashCode().toString() }) { period ->
                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    PeriodLabel(period)
                    schedule.days.forEach { day ->
                        GridCell(
                            entries = schedule.entriesAt(day, period),
                            isBreak = period.isBreakSlot,
                            isToday = day.isToday
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHeader(day: ScheduleDay) {
    Box(
        modifier = Modifier
            .width(DAY_COLUMN_WIDTH)
            .padding(horizontal = 3.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            day.shortLabel,
            fontSize = 12.sp,
            fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (day.isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun PeriodLabel(period: TimeSlot) {
    Column(
        modifier = Modifier
            .width(PERIOD_COLUMN_WIDTH)
            .height(ROW_HEIGHT)
            .padding(end = 6.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            period.slotName ?: "Period",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        period.displayRange()?.let {
            Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun GridCell(
    entries: List<TimetableEntry>,
    isBreak: Boolean,
    isToday: Boolean
) {
    val shape = RoundedCornerShape(8.dp)
    val background = when {
        isBreak -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        entries.isEmpty() -> MaterialTheme.colorScheme.surface
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
    }

    Box(
        modifier = Modifier
            .width(DAY_COLUMN_WIDTH)
            .height(ROW_HEIGHT)
            .padding(horizontal = 3.dp)
            .background(background, shape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(6.dp)
    ) {
        when {
            isBreak -> Text(
                "Break",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Center)
            )

            entries.isEmpty() -> Text(
                "—",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.align(Alignment.Center)
            )

            else -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val entry = entries.first()
                Text(
                    entry.courseLabel(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                entry.roomId?.let {
                    Text(
                        "Room $it",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                entry.sessionTypeId?.let {
                    Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, maxLines = 1)
                }
                // A clash is data the backend's conflict guard should have prevented
                // (contract v2 §4.1). Surfacing it beats silently rendering one of them.
                if (entries.size > 1) {
                    Text(
                        "+${entries.size - 1} clash",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * m_timetable resolves none of its foreign keys to names - there is no course or room
 * master-list call in this module (see TimetableNotes.kt), so the honest label is the
 * id with enough of a prefix that it reads as a reference rather than a mystery number.
 */
private fun TimetableEntry.courseLabel(): String {
    val id = courseOfferingId ?: courseId
    return if (id != null) "Course #$id" else "Class"
}

private val TimeSlot.isBreakSlot: Boolean
    get() = isBreak?.let { it.equals("true", ignoreCase = true) || it == "1" } == true

/** "09:00:00" -> "09:00" - strips the seconds the backend always sends. */
private fun TimeSlot.displayRange(): String? {
    val start = startTime?.take(5)
    val end = endTime?.take(5)
    return listOfNotNull(start, end).takeIf { it.isNotEmpty() }?.joinToString("–")
}
