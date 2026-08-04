package com.xsc.sdk.network.api

import com.xsc.sdk.network.internal.DispatcherApi
import retrofit2.Response
import javax.inject.Inject

/**
 * Thin, Retrofit-shaped client: callers get the raw [Response]<[DispatchResponse]>
 * back and decide how to unwrap it. Used by call sites (login, dashboard) that want
 * direct access to HTTP status / headers alongside the envelope.
 *
 * For a higher-level client that deserializes `data` straight into a DTO and throws
 * typed [com.xsc.sdk.network.APIError]s, see [com.xsc.sdk.network.APIClient].
 */
class ApiClient @Inject constructor(
    private val dispatcherApi: DispatcherApi
) {
    suspend fun dispatch(request: DispatchRequest): Response<DispatchResponse> =
        dispatcherApi.dispatch(request)
}
