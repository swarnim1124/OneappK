package com.xsc.oneapp.core.result

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateTest {

    @Test
    fun `deniesRoute is false while still loading - never bounce before the fetch resolves`() {
        val state: UiState<Set<String>> = UiState.Loading

        assertFalse(state.deniesRoute("fees"))
    }

    @Test
    fun `deniesRoute is false on any failure - a transient error must never bounce an already-granted user`() {
        assertFalse(UiState.BusinessError("nope").deniesRoute("fees"))
        assertFalse(UiState.NetworkError("offline").deniesRoute("fees"))
        assertFalse(UiState.UnexpectedError("boom").deniesRoute("fees"))
    }

    @Test
    fun `deniesRoute is false once resolved and the route is present`() {
        val state: UiState<Set<String>> = UiState.Success(setOf("fees", "attendance_graph"))

        assertFalse(state.deniesRoute("fees"))
    }

    @Test
    fun `deniesRoute is true only once resolved and the route is absent`() {
        val state: UiState<Set<String>> = UiState.Success(setOf("fees"))

        assertTrue(state.deniesRoute("timetable"))
    }
}
