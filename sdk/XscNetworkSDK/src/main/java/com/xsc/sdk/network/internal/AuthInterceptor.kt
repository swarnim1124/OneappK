package com.xsc.sdk.network.internal

import com.xsc.sdk.auth.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Attaches `Authorization: Bearer <jwt>` to every outgoing request when a token is
 * present. Every *_API_CONTRACT.md documents this header as "strongly recommended"
 * (and required for several actions like password:update, medical detail access,
 * accessibleModules:view), so it's simplest to always attach it rather than have
 * every repository remember to.
 */
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = tokenManager.accessToken
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        return chain.proceed(request)
    }
}
