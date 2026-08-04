package com.xsc.sdk.auth.internal

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Minimal, dependency-free JWT payload reader. OneApp does not need signature
 * verification on-device (the backend already validated the token before issuing
 * it); this only extracts claims like user_id/email/roles for UI display and for
 * resolving the "current user" without an extra network round trip.
 */
internal object JwtUtils {

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any?>>() {}.type

    fun decodeClaims(token: String): Map<String, Any?> {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return emptyMap()
            val payload = String(
                Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            )
            gson.fromJson<Map<String, Any?>>(payload, mapType) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Fail-closed: a missing or unparseable `exp` claim is treated as expired, not
     * "not expired". This used to `return false` here - a token with no readable
     * expiry (corrupt, tampered, or a future backend format change dropping/renaming
     * the claim) was then treated as permanently valid by [com.xsc.sdk.auth.SessionManager],
     * which drives [com.xsc.sdk.auth.SessionManager.isAuthenticated]. Silently staying
     * "logged in" forever on unreadable token state is the wrong default for an auth
     * check; forcing re-authentication is the safe one.
     */
    fun isExpired(claims: Map<String, Any?>): Boolean {
        val exp = (claims["exp"] as? Double)?.toLong() ?: return true
        val nowSeconds = System.currentTimeMillis() / 1000
        return nowSeconds >= exp
    }
}
