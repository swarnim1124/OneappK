package com.xsc.oneapp.navigation

import com.xsc.oneapp.core.registry.ModuleDefinition
import com.xsc.oneapp.core.registry.ModuleRegistry
import com.xsc.oneapp.feature.attendance.navigation.AttendanceDestinations
import com.xsc.oneapp.feature.exam.navigation.ExamDestinations
import com.xsc.oneapp.feature.profile.navigation.ProfileDestinations
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * destinationFor had no test coverage before the Module Registry refactor - these
 * pin down the exact same alias behaviour the old hardcoded `when` block had, so the
 * registry-backed version can't silently drift from it.
 */
class RoutesTest {

    private val registry = ModuleRegistry(
        setOf(
            ModuleDefinition("attendance", "Attendance", AttendanceDestinations.GRAPH_ROUTE, setOf("attendance")),
            ModuleDefinition("profile", "Profile", ProfileDestinations.PROFILE_ROUTE, setOf("profile")),
            ModuleDefinition("exams", "Exams", ExamDestinations.GRAPH_ROUTE, setOf("exams", "exam")),
            ModuleDefinition("fees", "Fees", "fees", setOf("fees", "fee")),
            ModuleDefinition("curriculum", "Curriculum", "curriculum", setOf("academics", "curriculum")),
            ModuleDefinition("timetable", "Timetable", "timetable", setOf("timetable"))
        )
    )

    @Test
    fun `every known alias resolves to its module's navigationEntry`() {
        assertEquals(ProfileDestinations.PROFILE_ROUTE, Routes.destinationFor("profile", registry))
        assertEquals(Routes.EXAMS, Routes.destinationFor("exams", registry))
        assertEquals(Routes.EXAMS, Routes.destinationFor("exam", registry))
        assertEquals(Routes.FEES, Routes.destinationFor("fees", registry))
        assertEquals(Routes.FEES, Routes.destinationFor("fee", registry))
        assertEquals(Routes.ATTENDANCE, Routes.destinationFor("attendance", registry))
        assertEquals(Routes.CURRICULUM, Routes.destinationFor("academics", registry))
        assertEquals(Routes.CURRICULUM, Routes.destinationFor("curriculum", registry))
        assertEquals(Routes.TIMETABLE, Routes.destinationFor("timetable", registry))
    }

    @Test
    fun `matching is normalised - case, slashes and whitespace never matter`() {
        assertEquals(Routes.FEES, Routes.destinationFor("/Fees", registry))
        assertEquals(Routes.FEES, Routes.destinationFor("fees/", registry))
        assertEquals(Routes.FEES, Routes.destinationFor("  FEES  ", registry))
        assertEquals(Routes.CURRICULUM, Routes.destinationFor("/Academics", registry))
    }

    @Test
    fun `an unrecognised module falls through to the generic module template`() {
        assertEquals(Routes.module("library"), Routes.destinationFor("library", registry))
    }
}
