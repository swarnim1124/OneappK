package com.xsc.oneapp.core.result

import com.xsc.sdk.network.APIError
import com.xsc.sdk.network.NetworkFailureReason
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UiStateCatchingTest {

    @Test
    fun `a successful block maps to Success with its value`() = runTest {
        val result = uiStateCatching { "ok" }

        assertEquals(UiState.Success("ok"), result)
    }

    @Test
    fun `APIError BusinessError maps to UiState BusinessError`() = runTest {
        val result = uiStateCatching {
            throw APIError.BusinessError("NOT_FOUND", "Profile not found")
        }

        assertEquals(UiState.BusinessError("Profile not found"), result)
    }

    @Test
    fun `APIError BusinessError never carries an appError - callers classify their own codes`() = runTest {
        val result = uiStateCatching {
            throw APIError.BusinessError("NOT_FOUND", "Profile not found")
        } as UiState.BusinessError

        assertNull(result.appError)
    }

    @Test
    fun `an unreachable NetworkError maps to the fixed unreachable message, not a passthrough`() = runTest {
        val result = uiStateCatching {
            throw APIError.NetworkError("some low-level socket text", NetworkFailureReason.UNREACHABLE)
        } as UiState.NetworkError

        assertEquals("Server is unreachable. Try again in a few minutes.", result.message)
        assertEquals(AppError.Network.Unreachable, result.appError)
    }

    @Test
    fun `a no-connection NetworkError maps to the fixed no-internet message`() = runTest {
        val result = uiStateCatching {
            throw APIError.NetworkError("unknown host", NetworkFailureReason.NO_CONNECTION)
        } as UiState.NetworkError

        assertEquals("No internet connection. Check your network.", result.message)
        assertEquals(AppError.Network.NoConnection, result.appError)
    }

    @Test
    fun `a timeout NetworkError maps to the fixed timeout message`() = runTest {
        val result = uiStateCatching {
            throw APIError.NetworkError("timed out", NetworkFailureReason.TIMEOUT)
        } as UiState.NetworkError

        assertEquals("Request timed out. Please retry.", result.message)
        assertEquals(AppError.Network.Timeout, result.appError)
    }

    @Test
    fun `APIError HttpError maps to an UnexpectedError mentioning the status code, with a traced appError`() = runTest {
        val result = uiStateCatching {
            throw APIError.HttpError(500, "boom")
        } as UiState.UnexpectedError

        assertTrue(result.message.contains("500"))
        assertTrue(result.appError is AppError.Traced)
        assertEquals(500, (result.appError as AppError.Traced).httpCode)
    }

    @Test
    fun `label flows into a traced HttpError's first line`() = runTest {
        val result = uiStateCatching(label = "attendance shortage") {
            throw APIError.HttpError(403, "boom", requiredPermission = "m_attendance.sm_shortage.attendanceShortage.view")
        } as UiState.UnexpectedError

        assertTrue(result.message.contains("Could not load attendance shortage"))
    }

    @Test
    fun `a generic exception maps to UnexpectedError with its message`() = runTest {
        val result = uiStateCatching {
            throw IllegalStateException("something broke")
        }

        assertEquals(UiState.UnexpectedError("something broke"), result)
    }
}
