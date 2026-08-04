package com.xsc.oneapp.core.result

import com.xsc.sdk.network.APIError
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `APIError NetworkError maps to UiState NetworkError`() = runTest {
        val result = uiStateCatching {
            throw APIError.NetworkError("timeout")
        }

        assertEquals(UiState.NetworkError("timeout"), result)
    }

    @Test
    fun `APIError HttpError maps to an UnexpectedError mentioning the status code`() = runTest {
        val result = uiStateCatching {
            throw APIError.HttpError(500, "boom")
        } as UiState.UnexpectedError

        assertTrue(result.message.contains("500"))
    }

    @Test
    fun `a generic exception maps to UnexpectedError with its message`() = runTest {
        val result = uiStateCatching {
            throw IllegalStateException("something broke")
        }

        assertEquals(UiState.UnexpectedError("something broke"), result)
    }
}
