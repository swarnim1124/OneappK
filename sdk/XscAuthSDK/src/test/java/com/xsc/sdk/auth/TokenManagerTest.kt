package com.xsc.sdk.auth

import android.content.SharedPreferences
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [SharedPreferences] and its [SharedPreferences.Editor] are backed here by a plain
 * in-memory map rather than the real EncryptedSharedPreferences (that wiring lives
 * in AuthModule and needs the Android Keystore) - TokenManager itself only depends
 * on the SharedPreferences interface, which is exactly what this test exercises.
 */
class TokenManagerTest {

    private val store = mutableMapOf<String, String?>()
    private val intStore = mutableMapOf<String, Int>()
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        store.clear()
        intStore.clear()
        prefs = mockk()
        editor = mockk()

        every { prefs.getString(any(), any()) } answers {
            store[firstArg()] ?: secondArg()
        }
        every { prefs.getInt(any(), any()) } answers {
            intStore[firstArg()] ?: secondArg()
        }
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } answers {
            store[firstArg<String>()] = secondArg<String?>()
            editor
        }
        every { editor.putInt(any(), any()) } answers {
            intStore[firstArg<String>()] = secondArg<Int>()
            editor
        }
        every { editor.remove(any()) } answers {
            store.remove(firstArg<String>())
            intStore.remove(firstArg<String>())
            editor
        }
        every { editor.apply() } just Runs
    }

    @Test
    fun `accessToken reflects the value persisted before construction`() {
        store["access_token"] = "existing-token"

        val manager = TokenManager(prefs)

        assertEquals("existing-token", manager.accessToken)
    }

    @Test
    fun `accessToken is null when nothing has been saved`() {
        val manager = TokenManager(prefs)

        assertNull(manager.accessToken)
    }

    @Test
    fun `saveTokens persists both tokens and updates accessTokenFlow immediately`() = runTest {
        val manager = TokenManager(prefs)

        manager.saveTokens("new-access", "new-refresh")

        assertEquals("new-access", manager.accessToken)
        assertEquals("new-refresh", manager.refreshToken)
        assertEquals("new-access", manager.accessTokenFlow.first())
    }

    @Test
    fun `clearTokens removes both tokens and nulls out accessToken`() {
        store["access_token"] = "existing-access"
        store["refresh_token"] = "existing-refresh"
        val manager = TokenManager(prefs)

        manager.clearTokens()

        assertNull(manager.accessToken)
        assertNull(manager.refreshToken)
    }

    @Test
    fun `institutionId is null when nothing has been saved`() {
        val manager = TokenManager(prefs)

        assertNull(manager.institutionId)
    }

    @Test
    fun `saveInstitutionId persists and is readable back`() {
        val manager = TokenManager(prefs)

        manager.saveInstitutionId(1)

        assertEquals(1, manager.institutionId)
    }

    @Test
    fun `clearTokens also clears institutionId`() {
        val manager = TokenManager(prefs)
        manager.saveInstitutionId(1)

        manager.clearTokens()

        assertNull(manager.institutionId)
    }

    @Test
    fun `email is null when nothing has been saved`() {
        val manager = TokenManager(prefs)

        assertNull(manager.email)
    }

    @Test
    fun `saveEmail persists and is readable back`() {
        val manager = TokenManager(prefs)

        manager.saveEmail("vivaan.reddy@oneapp.dev")

        assertEquals("vivaan.reddy@oneapp.dev", manager.email)
    }

    @Test
    fun `clearTokens also clears email`() {
        val manager = TokenManager(prefs)
        manager.saveEmail("vivaan.reddy@oneapp.dev")

        manager.clearTokens()

        assertNull(manager.email)
    }

    @Test
    fun `sessionExpired is false by default`() {
        val manager = TokenManager(prefs)

        assertFalse(manager.sessionExpired.value)
    }

    @Test
    fun `expireSession clears tokens and flags sessionExpired`() {
        store["access_token"] = "existing-access"
        val manager = TokenManager(prefs)

        manager.expireSession()

        assertNull(manager.accessToken)
        assertTrue(manager.sessionExpired.value)
    }

    @Test
    fun `an explicit clearTokens never sets sessionExpired`() {
        store["access_token"] = "existing-access"
        val manager = TokenManager(prefs)

        manager.clearTokens()

        assertFalse(manager.sessionExpired.value)
    }

    @Test
    fun `consumeSessionExpired resets the flag`() {
        val manager = TokenManager(prefs)
        manager.expireSession()

        manager.consumeSessionExpired()

        assertFalse(manager.sessionExpired.value)
    }
}
