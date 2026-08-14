package com.xsc.sdk.auth

import android.content.SharedPreferences
import com.xsc.sdk.auth.di.AuthPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the auth token pair. AuthInterceptor (XscNetworkSDK)
 * reads [accessToken] synchronously on every outgoing request; LoginViewModel writes
 * to it once on successful login via [saveTokens].
 *
 * [prefs] is provided by AuthModule as an EncryptedSharedPreferences instance, so the
 * JWT access/refresh token pair is encrypted at rest.
 */
@Singleton
class TokenManager @Inject constructor(
    @AuthPrefs private val prefs: SharedPreferences
) {
    private val _accessTokenFlow = MutableStateFlow(prefs.getString(KEY_ACCESS_TOKEN, null))
    val accessTokenFlow: StateFlow<String?> = _accessTokenFlow.asStateFlow()

    val accessToken: String?
        get() = _accessTokenFlow.value

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)

    /** The signed-in user's institution ID, as returned in AAA_API_CONTRACT.md
     * §3.1's login response (`user.institutionId`) - not present in the JWT itself,
     * so it's saved here alongside the token pair rather than derived from claims. */
    val institutionId: Int?
        get() = prefs.getInt(KEY_INSTITUTION_ID, -1).takeIf { it != -1 }

    /** The signed-in user's email, as returned in AAA_API_CONTRACT.md §3.1's login
     * response (`user.email`) - not reliably present as a JWT claim on every
     * backend/token, so it's saved here alongside the token pair rather than solely
     * derived from claims (see SessionManager.refreshFromToken's fallback). */
    val email: String?
        get() = prefs.getString(KEY_EMAIL, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        _accessTokenFlow.value = accessToken
    }

    fun saveInstitutionId(institutionId: Int) {
        prefs.edit().putInt(KEY_INSTITUTION_ID, institutionId).apply()
    }

    fun saveEmail(email: String) {
        prefs.edit().putString(KEY_EMAIL, email).apply()
    }

    /** True only when the session ended because [expireSession] forced it (a failed
     * token refresh - see TokenAuthenticator), never for an explicit user-initiated
     * [clearTokens] logout. LoginViewModel reads this once on arrival to show
     * "Session expired. Please sign in again." instead of leaving the user wondering
     * why they're suddenly looking at the login screen. */
    private val _sessionExpired = MutableStateFlow(false)
    val sessionExpired: StateFlow<Boolean> = _sessionExpired.asStateFlow()

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_INSTITUTION_ID)
            .remove(KEY_EMAIL)
            .apply()
        _accessTokenFlow.value = null
        _sessionExpired.value = false
    }

    /** Ends the session the same way [clearTokens] does, but flags it as forced
     * (see [sessionExpired]) rather than user-initiated. */
    fun expireSession() {
        clearTokens()
        _sessionExpired.value = true
    }

    /** Consumes the flag so it doesn't reappear on a later, unrelated visit to
     * Login (e.g. after a normal logout that happens afterwards). */
    fun consumeSessionExpired() {
        _sessionExpired.value = false
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_INSTITUTION_ID = "institution_id"
        const val KEY_EMAIL = "email"
    }
}
