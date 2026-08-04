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

    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_INSTITUTION_ID)
            .apply()
        _accessTokenFlow.value = null
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_INSTITUTION_ID = "institution_id"
    }
}
