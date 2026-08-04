package com.xsc.sdk.network.internal

import com.xsc.sdk.auth.TokenManager
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles a 401 from the dispatcher by refreshing the access token once and retrying
 * the failed request, instead of silently breaking the session.
 *
 * Before this, [TokenManager.refreshToken] was written on login and read nowhere -
 * the absence of a refresh path from the request pipeline was itself the bug (see
 * PRODUCTION_READINESS_FINAL.md risk #4): every session died the moment its access
 * token expired, with no way back short of a manual re-login.
 *
 * [dispatcherApi] is `dagger.Lazy` rather than a direct constructor param: this
 * Authenticator is wired into [com.xsc.sdk.network.di.NetworkModule.provideOkHttpClient],
 * and [DispatcherApi] is itself built on top of that same `OkHttpClient` via Retrofit.
 * A direct `DispatcherApi` dependency here would be a circular Dagger binding; `Lazy`
 * defers construction until the first refresh actually runs, after the graph has
 * finished assembling.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val dispatcherApi: Lazy<DispatcherApi>
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        // Never retry more than once per request chain - a second 401 after a fresh
        // token means the token really is invalid, not just stale, and retrying
        // forever on a hard-rejected token would spin the request pipeline.
        if (responseCount(response) >= MAX_RETRIES) return null

        // A request that never carried a bearer token in the first place wasn't
        // rejected for a stale-token reason a refresh could fix.
        val failedToken = response.request.header(AUTH_HEADER)?.removePrefix(BEARER_PREFIX)
            ?: return null

        val newAccessToken = runBlocking {
            mutex.withLock {
                // Another in-flight request may have already refreshed while this
                // one was waiting on the lock. If the token on file has already
                // moved on from the one that just failed, reuse it instead of
                // hitting the refresh endpoint again for the same expiry.
                val currentToken = tokenManager.accessToken
                if (currentToken != null && currentToken != failedToken) {
                    currentToken
                } else {
                    refreshAndPersist()
                }
            }
        }

        if (newAccessToken == null) {
            // Refresh failed (no refresh token on file, network error, or the
            // backend rejected it): the session cannot be recovered from here, so
            // end it rather than let every subsequent call fail the same way one
            // request at a time. SessionManager reacts to
            // TokenManager.accessTokenFlow going null and flips isAuthenticated to
            // false; RootNavHost observes that to route back to Login even for a
            // logout that happens mid-session (see RootNavHost.kt).
            runBlocking { tokenManager.clearTokens() }
            return null
        }

        return response.request.newBuilder()
            .header(AUTH_HEADER, "$BEARER_PREFIX$newAccessToken")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    private suspend fun refreshAndPersist(): String? {
        val refreshToken = tokenManager.refreshToken ?: return null
        return try {
            performRefresh(refreshToken)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * TODO(refresh-contract): the dispatcher action/payload for token refresh is not
     * yet confirmed. Every other OneApp call is documented against a
     * `*_API_CONTRACT.md`, but none of those seen so far document a "spend this
     * refresh token for a new access token" call the way they document
     * `m_AAA/sm_auth/session`. [TokenManager.refreshToken] has been captured from the
     * login response since that feature shipped; nothing has ever spent it.
     *
     * Wire the real call here once confirmed - it almost certainly goes through the
     * same [dispatcherApi] envelope, shaped roughly like:
     * ```
     * val response = dispatcherApi.get().dispatch(
     *     DispatchRequest(
     *         mod = "m_AAA", subMod = "sm_auth",
     *         action = <confirmed action>, actionType = <confirmed actionType>,
     *         payload = mapOf("refreshToken" to refreshToken)
     *     )
     * )
     * // then parse the new access/refresh token pair out of response.body()?.data
     * // and call tokenManager.saveTokens(newAccessToken, newRefreshToken)
     * ```
     * Until the shape is confirmed, this fails closed - returns null, which ends the
     * session and sends the user back to Login - rather than guessing a request body
     * against a production auth endpoint. Guessing wrong here is worse than a defined
     * "not implemented yet": a malformed refresh call could look like an attacker
     * probing the auth endpoint, or succeed partially and leave the token pair in an
     * inconsistent state.
     */
    private suspend fun performRefresh(refreshToken: String): String? {
        return null
    }

    private companion object {
        const val MAX_RETRIES = 2
        const val AUTH_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer "
    }
}
