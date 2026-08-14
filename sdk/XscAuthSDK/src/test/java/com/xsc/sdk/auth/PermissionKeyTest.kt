package com.xsc.sdk.auth

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionKeyTest {

    @Test
    fun `joins module, submodule, action and actionType with dots`() {
        assertEquals(
            "m_attendance.sm_records.attendanceRecord.view",
            requiredPermissionFor("m_attendance", "sm_records", "attendanceRecord", "view")
        )
    }

    @Test
    fun `a blank actionType falls back to default, matching the backend's action_type_safe`() {
        assertEquals(
            "m_attendance.sm_records.attendanceRecord.default",
            requiredPermissionFor("m_attendance", "sm_records", "attendanceRecord", "")
        )
    }
}
