package com.xsc.sdk.network

import com.google.gson.Gson
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.errorCodeAsString
import com.xsc.sdk.network.internal.DispatcherApi
import java.io.IOException
import javax.inject.Inject

/**
 * Higher-level client used by feature data sources (see
 * feature/profile/.../ProfileRemoteDataSourceImpl.kt): pass the envelope fields,
 * get the deserialized `data` payload back, or a typed [APIError] thrown for you.
 *
 * `dispatcherApi`/`gson` can't be `private`: Kotlin forbids a public inline function
 * from referencing private members of its own class (the function body gets copied
 * into every call site, which couldn't see a private member there). Both types are
 * public, so there's no encapsulation lost by leaving these as plain public vals.
 */
class APIClient @Inject constructor(
    val dispatcherApi: DispatcherApi,
    val gson: Gson
) {
    suspend inline fun <reified T> request(
        module: String,
        submodule: String,
        action: String,
        actionType: String,
        payload: Map<String, Any?> = emptyMap()
    ): T {
        val envelope = DispatchRequest(module, submodule, action, actionType, payload)

        val httpResponse = try {
            dispatcherApi.dispatch(envelope)
        } catch (e: IOException) {
            throw APIError.NetworkError(e.message ?: "Unable to reach the server")
        }

        if (!httpResponse.isSuccessful) {
            throw APIError.HttpError(
                httpResponse.code(),
                "Server returned HTTP ${httpResponse.code()}"
            )
        }

        val body = httpResponse.body()
            ?: throw APIError.NetworkError("Empty response body")

        if (!body.isSuccess) {
            throw APIError.BusinessError(body.errorCodeAsString(), body.message ?: "Request failed")
        }

        // Callers using Unit as T (fire-and-forget writes) never need `data` deserialized.
        if (T::class == Unit::class) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }

        val dataElement = body.data
        if (dataElement == null || dataElement.isJsonNull) {
            // Several successful writes/`view`s legitimately omit or null out `data`
            // (e.g. m_profile medicalDetail:view -> "No medical detail found").
            // Callers expecting a nullable type get null; callers expecting a non-null
            // type will fail fast downstream instead of silently here, since reified
            // generics can't check nullability at runtime.
            @Suppress("UNCHECKED_CAST")
            return null as T
        }

        return try {
            gson.fromJson(dataElement, T::class.java)
        } catch (e: Exception) {
            throw APIError.BusinessError("", "Failed to parse server response: ${e.message}")
        }
    }
}
