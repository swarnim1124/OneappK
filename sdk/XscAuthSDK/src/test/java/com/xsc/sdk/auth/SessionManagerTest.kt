package com.xsc.sdk.auth

import android.util.Base64
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Base64 as JavaBase64

/**
 * SessionManager.init() calls refreshFromToken(tokenManager.accessToken) synchronously
 * before returning, so these assertions don't need to wait on the async
 * accessTokenFlow collector - the state under test is already settled right after
 * construction.
 */
class SessionManagerTest {

    @Before
    fun setUp() {
        mockkStatic(Base64::class)
        every { Base64.decode(any<String>(), any()) } answers {
            val input = firstArg<String>()
            val padded = input + "=".repeat((4 - input.length % 4) % 4)
            JavaBase64.getUrlDecoder().decode(padded)
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(Base64::class)
    }

    private fun tokenFor(json: String): String {
        val payload = JavaBase64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())
        return "header.$payload.signature"
    }

    private fun tokenManagerReturning(token: String?, email: String? = null): TokenManager {
        val manager = mockk<TokenManager>()
        every { manager.accessToken } returns token
        every { manager.accessTokenFlow } returns MutableStateFlow(token)
        every { manager.email } returns email
        return manager
    }

    @Test
    fun `getUserId returns the real decoded id - regression guard for the removed DEV_FORCE_USER_ID shim`() {
        val token = tokenFor("""{"user_id":3,"email":"student@oneapp.local","exp":9999999999}""")
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertEquals("3", sessionManager.getUserId())
    }

    @Test
    fun `session state reflects a valid non-expired token`() {
        val token = tokenFor(
            """{"user_id":3,"email":"student@oneapp.local","roles":["student"],"name":"Student One","exp":9999999999}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(sessionManager.isAuthenticated.value)
        assertEquals("student@oneapp.local", sessionManager.currentEmail.value)
        assertEquals("student", sessionManager.currentRole.value)
        assertEquals("Student One", sessionManager.getDisplayName())
    }

    @Test
    fun `currentPermissions decodes the JWT permissions array - RBAC is permission-driven, not role-driven`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["timetable.timetable.view","timetable.workingDay.view"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertEquals(listOf("timetable.timetable.view", "timetable.workingDay.view"), sessionManager.currentPermissions.value)
    }

    @Test
    fun `a token with no permissions claim yields an empty list, not a crash`() {
        val token = tokenFor("""{"user_id":3,"exp":9999999999}""")
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(sessionManager.currentPermissions.value.isEmpty())
    }

    @Test
    fun `no token means not authenticated and no user id`() {
        val sessionManager = SessionManager(tokenManagerReturning(null))

        assertFalse(sessionManager.isAuthenticated.value)
        assertNull(sessionManager.currentEmail.value)
        assertNull(sessionManager.getUserId())
    }

    @Test
    fun `getDisplayName falls back to the login response email when the token has neither a name nor an email claim`() {
        val token = tokenFor("""{"user_id":3,"exp":9999999999}""")
        val sessionManager = SessionManager(tokenManagerReturning(token, email = "vivaan.reddy@oneapp.dev"))

        assertEquals("vivaan.reddy", sessionManager.getDisplayName())
    }

    @Test
    fun `getDisplayName falls back to the literal placeholder when neither the token nor the login response has an email`() {
        val token = tokenFor("""{"user_id":3,"exp":9999999999}""")
        val sessionManager = SessionManager(tokenManagerReturning(token, email = null))

        assertEquals("there", sessionManager.getDisplayName())
    }

    @Test
    fun `getEmail falls back to the login response email when the token has no email claim`() {
        val token = tokenFor("""{"user_id":3,"exp":9999999999}""")
        val sessionManager = SessionManager(tokenManagerReturning(token, email = "vivaan.reddy@oneapp.dev"))

        assertEquals("vivaan.reddy@oneapp.dev", sessionManager.getEmail())
    }

    @Test
    fun `getEmail prefers the token's own email claim over the login response fallback`() {
        val token = tokenFor("""{"user_id":3,"exp":9999999999,"email":"from-token@oneapp.local"}""")
        val sessionManager = SessionManager(tokenManagerReturning(token, email = "from-login-response@oneapp.dev"))

        assertEquals("from-token@oneapp.local", sessionManager.getEmail())
    }

    @Test
    fun `an expired token is not authenticated`() {
        val token = tokenFor("""{"user_id":3,"exp":1}""")
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertFalse(sessionManager.isAuthenticated.value)
    }

    @Test
    fun `hasPermission is true only for a permission actually present on the token`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["timetable.timetable.view"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(sessionManager.hasPermission("timetable.timetable.view"))
        assertFalse(sessionManager.hasPermission("timetable.workingDay.view"))
    }

    @Test
    fun `hasPermission is false for every permission when the token has none`() {
        val token = tokenFor("""{"user_id":3,"exp":9999999999}""")
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertFalse(sessionManager.hasPermission("timetable.timetable.view"))
    }

    @Test
    fun `hasAnyPermission is true if at least one of the requested permissions is present`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["timetable.workingDay.view"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(
            sessionManager.hasAnyPermission("timetable.timetable.view", "timetable.workingDay.view")
        )
        assertFalse(
            sessionManager.hasAnyPermission("attendance.attendance.view", "fees.fees.view")
        )
    }

    @Test
    fun `getInstitutionId delegates to TokenManager (it's not a JWT claim)`() {
        val tokenManager = tokenManagerReturning(tokenFor("""{"user_id":3,"exp":9999999999}"""))
        every { tokenManager.institutionId } returns 1
        val sessionManager = SessionManager(tokenManager)

        assertEquals(1, sessionManager.getInstitutionId())
    }

    // --- Wildcard matching (mirrors xsc_security/authorization.py, confirmed by the
    // backend team's RBAC audit) ---

    @Test
    fun `the literal global wildcard grants every permission unconditionally`() {
        val token = tokenFor("""{"user_id":3,"exp":9999999999,"permissions":["*.*.*.*"]}""")
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(sessionManager.hasPermission("m_attendance.sm_records.attendanceRecord.view"))
        // Unconditional means it must also cover a malformed/legacy-shaped required
        // string that doesn't split into 4 segments - the global wildcard is checked
        // before the segment-count gate, not as a degenerate case of it.
        assertTrue(sessionManager.hasPermission("timetable.timetable.view"))
    }

    @Test
    fun `a module-level wildcard grants every action within that module`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["m_attendance.*.*.*"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(sessionManager.hasPermission("m_attendance.sm_records.attendanceRecord.view"))
        assertTrue(sessionManager.hasPermission("m_attendance.sm_shortage.condonation.view"))
        assertFalse(sessionManager.hasPermission("m_fee.sm_records.feeRecord.view"))
    }

    @Test
    fun `a submodule-level wildcard grants every action within that submodule only`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["m_attendance.sm_records.*.*"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(sessionManager.hasPermission("m_attendance.sm_records.attendanceRecord.view"))
        assertFalse(sessionManager.hasPermission("m_attendance.sm_shortage.condonation.view"))
    }

    @Test
    fun `an action-level wildcard grants every actionType for that action only`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["m_attendance.sm_records.attendanceRecord.*"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(sessionManager.hasPermission("m_attendance.sm_records.attendanceRecord.view"))
        assertTrue(sessionManager.hasPermission("m_attendance.sm_records.attendanceRecord.add"))
        assertFalse(sessionManager.hasPermission("m_attendance.sm_records.attendanceSession.view"))
    }

    @Test
    fun `a segment-count mismatch never falls back to a partial match`() {
        // Granted permission has 4 segments but the required one doesn't split into 4 -
        // per the confirmed backend algorithm this fails closed, it never trims or pads
        // either side to force a comparison.
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["m_attendance.sm_records.*.*"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertFalse(sessionManager.hasPermission("m_attendance.sm_records.attendanceRecord"))
    }

    @Test
    fun `hasAnyPermission also resolves wildcards, not just exact matches`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"permissions":["m_attendance.*.*.*"]}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertTrue(
            sessionManager.hasAnyPermission("m_fee.sm_records.feeRecord.view", "m_attendance.sm_records.attendanceRecord.view")
        )
        assertFalse(
            sessionManager.hasAnyPermission("m_fee.sm_records.feeRecord.view", "m_exam.sm_schedule.examSchedule.view")
        )
    }
}
