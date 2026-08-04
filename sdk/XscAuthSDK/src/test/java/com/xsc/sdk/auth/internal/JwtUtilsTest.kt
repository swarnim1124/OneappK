package com.xsc.sdk.auth.internal

import android.util.Base64
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Base64 as JavaBase64

/**
 * android.util.Base64 has no implementation on the plain JVM unit-test classpath, so
 * these tests route it to java.util.Base64 (URL-safe, no padding - same alphabet
 * JwtUtils.decodeClaims relies on) rather than pulling in Robolectric for one call.
 */
class JwtUtilsTest {

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
        val payload = JavaBase64.getUrlEncoder().withoutPadding()
            .encodeToString(json.toByteArray())
        return "header.$payload.signature"
    }

    @Test
    fun `decodeClaims parses a well-formed token payload`() {
        val token = tokenFor("""{"user_id":3,"email":"student@oneapp.local"}""")

        val claims = JwtUtils.decodeClaims(token)

        assertEquals(3.0, claims["user_id"])
        assertEquals("student@oneapp.local", claims["email"])
    }

    @Test
    fun `decodeClaims returns empty map for a token with fewer than two parts`() {
        val claims = JwtUtils.decodeClaims("not-a-jwt")

        assertTrue(claims.isEmpty())
    }

    @Test
    fun `decodeClaims returns empty map for unparseable payload`() {
        val token = "header.%%%not-base64%%%.signature"

        val claims = JwtUtils.decodeClaims(token)

        assertTrue(claims.isEmpty())
    }

    @Test
    fun `isExpired is true (fail-closed) when exp claim is absent`() {
        assertTrue(JwtUtils.isExpired(emptyMap()))
    }

    @Test
    fun `isExpired is true (fail-closed) when exp claim is not a number`() {
        assertTrue(JwtUtils.isExpired(mapOf("exp" to "not-a-number")))
    }

    @Test
    fun `isExpired is true once exp is in the past`() {
        val pastSeconds = (System.currentTimeMillis() / 1000) - 3600
        val claims = mapOf<String, Any?>("exp" to pastSeconds.toDouble())

        assertTrue(JwtUtils.isExpired(claims))
    }

    @Test
    fun `isExpired is false while exp is in the future`() {
        val futureSeconds = (System.currentTimeMillis() / 1000) + 3600
        val claims = mapOf<String, Any?>("exp" to futureSeconds.toDouble())

        assertFalse(JwtUtils.isExpired(claims))
    }
}
