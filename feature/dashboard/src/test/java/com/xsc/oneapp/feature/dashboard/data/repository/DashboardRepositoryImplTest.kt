package com.xsc.oneapp.feature.dashboard.data.repository

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.xsc.sdk.network.api.ApiClient
import com.xsc.sdk.network.api.DispatchResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class DashboardRepositoryImplTest {

    private val gson = Gson()

    private fun repository(prefs: SharedPreferences = mockk(relaxed = true), apiClient: ApiClient = mockk()) =
        DashboardRepositoryImpl(apiClient, gson, prefs)

    private fun failingApiClient(): ApiClient = mockk<ApiClient>().also {
        coEvery { it.dispatch(any()) } throws RuntimeException("network down")
    }

    @Test
    fun `a successful dispatch keeps the returned modules and their backend metadata`() = runTest {
        val apiClient = mockk<ApiClient>()
        val modules = """
            [{"id":"academics","displayName":"Academics","icon":"school","route":"/academics","status":"Active","accentColor":"#4F46E5"}]
        """.trimIndent()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString(modules)))

        val result = repository(apiClient = apiClient).getAccessibleModules()

        // Backend metadata wins for what it returned.
        assertEquals("academics", result.first().id)
        assertEquals("Academics", result.first().displayName)
    }

    @Test
    fun `implemented modules the backend omits are still merged in`() = runTest {
        val apiClient = mockk<ApiClient>()
        val modules = """
            [{"id":"academics","displayName":"Academics","icon":"school","route":"/academics","status":"Active","accentColor":"#4F46E5"}]
        """.trimIndent()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString(modules)))

        val result = repository(apiClient = apiClient).getAccessibleModules().map { it.id }

        // The regression this guards: a backend that only knows about `academics`
        // used to hide every other shipped feature, Exams and Fees included.
        assertTrue("exams" in result)
        assertTrue("fees" in result)
        assertEquals(DashboardRepositoryImpl.IMPLEMENTED_MODULE_IDS, result.toSet())
    }

    @Test
    fun `unimplemented modules are not force-added to a backend-supplied list`() = runTest {
        val apiClient = mockk<ApiClient>()
        val modules = """
            [{"id":"academics","displayName":"Academics","icon":"school","route":"/academics","status":"Active","accentColor":"#4F46E5"}]
        """.trimIndent()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString(modules)))

        val result = repository(apiClient = apiClient).getAccessibleModules().map { it.id }

        assertTrue("library" !in result)
        assertTrue("placements" !in result)
    }

    @Test
    fun `a failed dispatch falls back to the full catalog, not a truncated guess`() = runTest {
        val result = repository(apiClient = failingApiClient()).getAccessibleModules().map { it.id }

        assertEquals(DashboardRepositoryImpl.MODULE_CATALOG.map { it.id }, result)
        assertTrue("exams" in result)
        assertTrue("fees" in result)
    }

    @Test
    fun `a non-success envelope also falls back to the full catalog`() = runTest {
        val apiClient = mockk<ApiClient>()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "error", message = "not seeded"))

        val result = repository(apiClient = apiClient).getAccessibleModules().map { it.id }

        assertTrue("exams" in result)
        assertTrue("fees" in result)
    }

    @Test
    fun `an empty backend array falls back to the catalog rather than showing nothing`() = runTest {
        val apiClient = mockk<ApiClient>()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString("[]")))

        val result = repository(apiClient = apiClient).getAccessibleModules()

        assertEquals(DashboardRepositoryImpl.MODULE_CATALOG, result)
    }

    @Test
    fun `an unknown status string does not drop the module`() = runTest {
        val apiClient = mockk<ApiClient>()
        val modules = """
            [{"id":"fees","displayName":"Fees","icon":"currency_rupee","route":"/fees","status":"Piloting","accentColor":"#06B6D4"}]
        """.trimIndent()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString(modules)))

        val result = repository(apiClient = apiClient).getAccessibleModules()

        val fees = result.first { it.id == "fees" }
        assertEquals("Fees", fees.displayName)
    }

    @Test
    fun `a row with no id is skipped without taking out the rest of the list`() = runTest {
        val apiClient = mockk<ApiClient>()
        val modules = """
            [{"displayName":"Broken"},{"id":"exams","displayName":"Exams","icon":"description","route":"/exams","status":"Active","accentColor":"#EF4444"}]
        """.trimIndent()
        coEvery { apiClient.dispatch(any()) } returns
            Response.success(DispatchResponse(status = "success", data = JsonParser.parseString(modules)))

        val result = repository(apiClient = apiClient).getAccessibleModules()

        assertTrue(result.any { it.id == "exams" })
    }

    @Test
    fun `the curriculum module keeps the academics contract id and route`() = runTest {
        val result = repository(apiClient = failingApiClient()).getAccessibleModules()

        val curriculum = result.first { it.displayName == "Curriculum" }
        assertEquals("academics", curriculum.id)
        assertEquals("/academics", curriculum.route)
    }

    @Test
    fun `getPinnedModuleIds reads the stored set, defaulting to empty`() {
        val prefs = mockk<SharedPreferences>()
        every { prefs.getStringSet("pinned_module_ids", emptySet()) } returns setOf("academics", "exams")

        val result = repository(prefs = prefs).getPinnedModuleIds()

        assertEquals(setOf("academics", "exams"), result)
    }

    @Test
    fun `getPinnedModuleIds returns empty when nothing was ever stored`() {
        val prefs = mockk<SharedPreferences>()
        every { prefs.getStringSet("pinned_module_ids", emptySet()) } returns null

        val result = repository(prefs = prefs).getPinnedModuleIds()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `setPinnedModuleIds persists the given set under the pinned-modules key`() {
        val prefs = mockk<SharedPreferences>()
        val editor = mockk<SharedPreferences.Editor>()
        val capturedSet = slot<Set<String>>()
        every { prefs.edit() } returns editor
        every { editor.putStringSet("pinned_module_ids", capture(capturedSet)) } returns editor
        every { editor.apply() } returns Unit

        repository(prefs = prefs).setPinnedModuleIds(setOf("attendance"))

        verify { editor.putStringSet("pinned_module_ids", any()) }
        assertEquals(setOf("attendance"), capturedSet.captured)
    }
}
