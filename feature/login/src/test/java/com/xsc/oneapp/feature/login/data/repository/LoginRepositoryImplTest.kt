package com.xsc.oneapp.feature.login.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.api.ApiClient
import com.xsc.sdk.network.api.DispatchRequest
import com.xsc.sdk.network.api.DispatchResponse
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import retrofit2.Response

class LoginRepositoryImplTest {

    private val gson = Gson()

    @Test
    fun `login dispatches to m_AAA sm_auth session add and maps a successful response`() = runTest {
        val apiClient = mockk<ApiClient>()
        val requestSlot = slot<DispatchRequest>()
        val responseData = JsonParser.parseString(
            """{"token":"access-token","refreshToken":"refresh-token","captchaRequired":false}"""
        )
        coEvery { apiClient.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = responseData))

        val result = LoginRepositoryImpl(apiClient, gson)
            .login(mapOf("username" to "student@oneapp.local", "password" to "Student@123"))

        assertEquals("access-token", result.token)
        assertEquals("refresh-token", result.refreshToken)
        assertEquals("m_AAA", requestSlot.captured.mod)
        assertEquals("sm_auth", requestSlot.captured.subMod)
        assertEquals("session", requestSlot.captured.action)
        assertEquals("add", requestSlot.captured.actionType)
    }

    @Test
    fun `login captures institutionId from the nested user object`() = runTest {
        val apiClient = mockk<ApiClient>()
        val responseData = JsonParser.parseString(
            """{"token":"access-token","refreshToken":"refresh-token","captchaRequired":false,"user":{"userId":1,"email":"admin@xcel.com","roles":["SUPER_ADMIN"],"institutionId":1}}"""
        )
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = responseData))

        val result = LoginRepositoryImpl(apiClient, gson)
            .login(mapOf("username" to "admin@xcel.com", "password" to "Student@123"))

        assertEquals(1, result.institutionId)
    }

    @Test
    fun `login throws a typed BusinessError when the backend reports a non-success status`() {
        val apiClient = mockk<ApiClient>()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "error", message = "Invalid credentials"))

        val exception = assertThrows(APIError.BusinessError::class.java) {
            runBlocking { LoginRepositoryImpl(apiClient, gson).login(emptyMap()) }
        }

        assertEquals("Invalid credentials", exception.errorMessage)
    }

    @Test
    fun `login throws a typed HttpError when the response body is null`() {
        val apiClient = mockk<ApiClient>()
        coEvery { apiClient.dispatch(any()) } returns Response.success(null)

        assertThrows(APIError.HttpError::class.java) {
            runBlocking { LoginRepositoryImpl(apiClient, gson).login(emptyMap()) }
        }
    }

    @Test
    fun `forgotPassword dispatches to m_AAA auth passwordReset add`() = runTest {
        val apiClient = mockk<ApiClient>()
        val requestSlot = slot<DispatchRequest>()
        val responseData = JsonParser.parseString("""{"resetToken":"reset-token-abc","message":"OTP sent"}""")
        coEvery { apiClient.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = responseData))

        val result = LoginRepositoryImpl(apiClient, gson)
            .forgotPassword(mapOf("email" to "student@oneapp.local"))

        assertEquals("reset-token-abc", result.resetToken)
        assertEquals("m_AAA", requestSlot.captured.mod)
        assertEquals("auth", requestSlot.captured.subMod)
        assertEquals("passwordReset", requestSlot.captured.action)
        assertEquals("add", requestSlot.captured.actionType)
    }

    @Test
    fun `verifyOTP dispatches to m_AAA auth passwordReset view`() = runTest {
        val apiClient = mockk<ApiClient>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { apiClient.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("""{"message":"Token valid"}""")))

        LoginRepositoryImpl(apiClient, gson).verifyOTP(mapOf("token" to "reset-token-abc"))

        assertEquals("auth", requestSlot.captured.subMod)
        assertEquals("passwordReset", requestSlot.captured.action)
        assertEquals("view", requestSlot.captured.actionType)
    }

    @Test
    fun `resetPassword dispatches to m_AAA auth passwordReset update`() = runTest {
        val apiClient = mockk<ApiClient>()
        val requestSlot = slot<DispatchRequest>()
        coEvery { apiClient.dispatch(capture(requestSlot)) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("""{"message":"Password reset successfully"}""")))

        LoginRepositoryImpl(apiClient, gson).resetPassword(mapOf("token" to "reset-token-abc", "newPassword" to "NewPass@123"))

        assertEquals("auth", requestSlot.captured.subMod)
        assertEquals("passwordReset", requestSlot.captured.action)
        assertEquals("update", requestSlot.captured.actionType)
    }
}
