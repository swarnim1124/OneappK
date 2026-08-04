package com.xsc.sdk.network.internal

import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Raw Retrofit surface for the single `POST /` dispatcher endpoint every OneApp
 * module shares. Feature modules should inject [com.xsc.sdk.network.api.ApiClient]
 * or [com.xsc.sdk.network.APIClient] instead of this directly - those sit on top
 * of it and handle the envelope/error-mapping.
 */
interface DispatcherApi {
    @POST(".")
    suspend fun dispatch(@Body request: DispatchRequest): Response<DispatchResponse>
}
