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

    private fun tokenManagerReturning(token: String?): TokenManager {
        val manager = mockk<TokenManager>()
        every { manager.accessToken } returns token
        every { manager.accessTokenFlow } returns MutableStateFlow(token)
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
    fun `getInstitutionId prefers TokenManager over any JWT claim`() {
        val tokenManager = tokenManagerReturning(
            tokenFor("""{"user_id":3,"exp":9999999999,"institutionId":2}""")
        )
        every { tokenManager.institutionId } returns 1
        val sessionManager = SessionManager(tokenManager)

        assertEquals(1, sessionManager.getInstitutionId())
    }

    @Test
    fun `getInstitutionId falls back to a JWT claim when TokenManager has none - covers a session saved before that field, or a login response that omitted it`() {
        val tokenManager = tokenManagerReturning(
            tokenFor("""{"user_id":3,"exp":9999999999,"institutionId":7}""")
        )
        every { tokenManager.institutionId } returns null
        val sessionManager = SessionManager(tokenManager)

        assertEquals(7, sessionManager.getInstitutionId())
    }

    @Test
    fun `getInstitutionId tries the snake_case and short-form claim keys too`() {
        val tokenManager = tokenManagerReturning(
            tokenFor("""{"user_id":3,"exp":9999999999,"inst_id":9}""")
        )
        every { tokenManager.institutionId } returns null
        val sessionManager = SessionManager(tokenManager)

        assertEquals(9, sessionManager.getInstitutionId())
    }

    @Test
    fun `getFirstName returns only the first token of the resolved name`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"name":"Aarav Sharma"}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertEquals("Aarav Sharma", sessionManager.getDisplayName())
        assertEquals("Aarav", sessionManager.getFirstName())
    }

    @Test
    fun `a name-less token derives a display name from the email local-part instead of a placeholder like 'there'`() {
        val token = tokenFor(
            """{"user_id":3,"exp":9999999999,"email":"aarav.sharma@oneapp.local"}"""
        )
        val sessionManager = SessionManager(tokenManagerReturning(token))

        assertEquals("Aarav Sharma", sessionManager.getDisplayName())
        assertEquals("Aarav", sessionManager.getFirstName())
    }

    @Test
    fun `no token and no email falls back to a generic, non-broken-looking name`() {
        val sessionManager = SessionManager(tokenManagerReturning(null))

        assertEquals("Student", sessionManager.getDisplayName())
        assertEquals("Student", sessionManager.getFirstName())
    }

    @Test
    fun `getInstitutionId is null when neither TokenManager nor the token has one`() {
        val tokenManager = tokenManagerReturning(tokenFor("""{"user_id":3,"exp":9999999999}"""))
        every { tokenManager.institutionId } returns null
        val sessionManager = SessionManager(tokenManager)

        assertNull(sessionManager.getInstitutionId())
    }
}
