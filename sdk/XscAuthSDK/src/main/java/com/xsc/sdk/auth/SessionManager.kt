package com.xsc.sdk.auth

import com.xsc.sdk.auth.internal.JwtUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Derives "who is the current user" from the JWT held by [TokenManager] so callers
 * don't need to hit `m_AAA / sm_auth / user:view` just to render a name badge.
 * Automatically reacts to login (TokenManager.saveTokens) and logout
 * (TokenManager.clearTokens) via [TokenManager.accessTokenFlow] - no manual refresh
 * call needed from LoginViewModel or anywhere else.
 */
@Singleton
class SessionManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _currentEmail = MutableStateFlow<String?>(null)
    val currentEmail: StateFlow<String?> = _currentEmail.asStateFlow()

    private val _currentRole = MutableStateFlow<String?>(null)
    val currentRole: StateFlow<String?> = _currentRole.asStateFlow()

    /** OneApp's RBAC is permission-driven, not role-driven (confirmed 2026-07-31):
     * access checks compare against explicit strings like "timetable.timetable.view"
     * in the JWT's "permissions" array, with [currentRole]'s role_code (e.g.
     * "hod", "timetable_coordinator") only a secondary/display concern. */
    private val _currentPermissions = MutableStateFlow<List<String>>(emptyList())
    val currentPermissions: StateFlow<List<String>> = _currentPermissions.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    @Volatile private var userId: String? = null
    @Volatile private var displayName: String? = null

    init {
        refreshFromToken(tokenManager.accessToken)
        scope.launch {
            tokenManager.accessTokenFlow.collect { token ->
                refreshFromToken(token)
            }
        }
    }

    private fun refreshFromToken(token: String?) {
        if (token.isNullOrBlank()) {
            userId = null
            displayName = null
            _currentEmail.value = null
            _currentRole.value = null
            _currentPermissions.value = emptyList()
            _isAuthenticated.value = false
            return
        }

        val claims = JwtUtils.decodeClaims(token)
        val expired = JwtUtils.isExpired(claims)

        userId = (claims["user_id"] ?: claims["sub"] ?: claims["userId"])?.let(::normalizeIdClaim)
        val email = claims["email"] as? String
        val roles = (claims["roles"] as? List<*>)?.firstOrNull()?.toString()
        val permissions = (claims["permissions"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        val name = claims["name"] as? String ?: claims["given_name"] as? String

        displayName = name ?: email?.substringBefore("@")
        _currentEmail.value = email
        _currentRole.value = roles
        _currentPermissions.value = permissions
        _isAuthenticated.value = !expired
    }

    fun getUserId(): String? = userId?.let { DEV_FORCE_USER_ID ?: it }

    /** Institution ID captured from the login response (see [TokenManager.institutionId] -
     * it isn't a JWT claim, so it can't be derived in [refreshFromToken]). Null until a
     * successful login has saved one. */
    fun getInstitutionId(): Int? = tokenManager.institutionId

    /**
     * Gson decodes JWT claims into `Map<String, Any?>` with no schema, so every JSON
     * number - including "user_id": 3 - lands as a Double. A bare `.toString()` on
     * that turns it into "3.0", which the backend then fails to match against the
     * integer user ID (see m_profile personalDetail returning "Profile not found"
     * for a session that authenticated fine). Collapse whole-number Doubles back to
     * their plain integer form before they go out as a string.
     */
    private fun normalizeIdClaim(raw: Any): String {
        val asDouble = raw as? Double ?: return raw.toString()
        return if (asDouble == Math.floor(asDouble) && !asDouble.isInfinite()) {
            asDouble.toLong().toString()
        } else {
            asDouble.toString()
        }
    }

    fun getDisplayName(): String = displayName ?: "there"

    /**
     * Defense-in-depth only. [currentPermissions] was captured from the JWT and unit
     * tested since RBAC work started here but never actually consulted by anything -
     * every screen was reachable to every signed-in user regardless of their
     * permissions array, with the backend as the only real gate (see
     * PRODUCTION_READINESS_FINAL.md risk #5).
     *
     * The backend re-checks every action and remains the authority: a `true` here
     * only means "show/enable this," never "this call is guaranteed to succeed," and
     * a `false` should hide or disable a UI affordance, not be trusted as proof the
     * backend would also refuse it. Do not use this to decide what to *send* to a
     * write endpoint, only what to *show*.
     */
    fun hasPermission(permission: String): Boolean = permission in currentPermissions.value

    /** True if the current user holds any one of [permissions]. See [hasPermission]. */
    fun hasAnyPermission(vararg permissions: String): Boolean =
        permissions.any { it in currentPermissions.value }

    suspend fun clearSession() {
        tokenManager.clearTokens()
    }

    companion object {
        /**
         * Dev-only override hook: when non-null, forces every userId/studId payload
         * this app sends to the given value, regardless of who is actually
         * authenticated. Historically flipped to "1" to work around
         * dev.globaloneapp.com's student@oneapp.local (user_id 3) having no seeded
         * m_profile row - only user_id 1 did.
         *
         * MUST stay `null` outside a local run against that specific unseeded dev
         * backend: every signed-in user's profile/attendance/family calls resolve
         * to this ID, so a non-null value here is a cross-user data leak, not a
         * convenience. If you need it locally, set it in your own working copy and
         * do not commit a non-null value.
         */
        private val DEV_FORCE_USER_ID: String? = null
    }
}
