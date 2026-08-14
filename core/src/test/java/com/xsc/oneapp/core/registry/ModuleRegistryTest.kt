package com.xsc.oneapp.core.registry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModuleRegistryTest {

    private val attendance = ModuleDefinition(
        moduleId = "attendance",
        displayName = "Attendance",
        navigationEntry = "attendance_graph",
        routeAliases = setOf("attendance")
    )
    private val exams = ModuleDefinition(
        moduleId = "exams",
        displayName = "Exams",
        navigationEntry = "exams",
        routeAliases = setOf("exams", "exam")
    )
    private val registry = ModuleRegistry(setOf(attendance, exams))

    @Test
    fun `all returns every contributed definition`() {
        assertEquals(setOf(attendance, exams), registry.all().toSet())
    }

    @Test
    fun `findById returns the matching definition`() {
        assertEquals(attendance, registry.findById("attendance"))
    }

    @Test
    fun `findById returns null for an unknown module id`() {
        assertNull(registry.findById("nonexistent"))
    }

    @Test
    fun `findByRouteAlias matches any of a definition's aliases`() {
        assertEquals(exams, registry.findByRouteAlias("exams"))
        assertEquals(exams, registry.findByRouteAlias("exam"))
    }

    @Test
    fun `findByRouteAlias returns null for an unrecognised key`() {
        assertNull(registry.findByRouteAlias("library"))
    }

    @Test
    fun `an empty registry answers every lookup with null or empty, never throws`() {
        val empty = ModuleRegistry(emptySet())

        assertTrue(empty.all().isEmpty())
        assertNull(empty.findById("attendance"))
        assertNull(empty.findByRouteAlias("attendance"))
    }
}
