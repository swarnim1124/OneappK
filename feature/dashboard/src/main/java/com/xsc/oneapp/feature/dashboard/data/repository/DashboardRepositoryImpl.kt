package com.xsc.oneapp.feature.dashboard.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xsc.oneapp.feature.dashboard.data.mapper.toDomain
import com.xsc.oneapp.feature.dashboard.data.remote.dto.ModuleItemDto
import com.xsc.oneapp.feature.dashboard.di.DashboardPrefs
import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.domain.repository.DashboardRepository
import com.xsc.sdk.network.api.ApiClient
import com.xsc.sdk.network.api.DispatchRequest
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
    private val gson: Gson,
    @DashboardPrefs private val prefs: SharedPreferences
) : DashboardRepository {

    /**
     * [MODULE_CATALOG] is the source of truth for what this app can actually open, and
     * the remote `accessibleModules` response is treated as an *enrichment* of it,
     * never a replacement.
     *
     * This used to work the other way around. A successful remote call replaced the
     * list wholesale, and any failure fell through to a set of module ids guessed from
     * the signed-in user's email prefix - with an `else` branch that resolved to just
     * `["academics", "attendance"]`. Both paths could silently drop a module that is
     * fully built and reachable, which is exactly what happened to Exams and Fees.
     *
     * Showing a tile is a navigation affordance, not an access control. Every action
     * behind these screens is authorised server-side against the caller's JWT, so
     * surfacing a module the backend later refuses is a recoverable, visible error
     * state - whereas hiding a shipped feature is a silent bug no user can report.
     */
    override suspend fun getAccessibleModules(): List<ModuleItem> =
        mergeWithCatalog(fetchRemoteModules().orEmpty())

    override fun getPinnedModuleIds(): Set<String> =
        prefs.getStringSet(PINNED_MODULES_KEY, emptySet()).orEmpty()

    override fun setPinnedModuleIds(ids: Set<String>) {
        prefs.edit { putStringSet(PINNED_MODULES_KEY, ids) }
    }

    /**
     * Backend order and metadata win for anything it returned; every *implemented*
     * module the backend omitted is appended in catalog order. When the call fails or
     * returns nothing, the result is the full catalog rather than a truncated guess.
     */
    private fun mergeWithCatalog(remote: List<ModuleItem>): List<ModuleItem> {
        if (remote.isEmpty()) return MODULE_CATALOG

        val returnedIds = remote.mapTo(mutableSetOf()) { it.id }
        val missingButImplemented = MODULE_CATALOG.filter {
            it.id in IMPLEMENTED_MODULE_IDS && it.id !in returnedIds
        }
        return remote + missingButImplemented
    }

    private suspend fun fetchRemoteModules(): List<ModuleItem>? {
        return try {
            val request = DispatchRequest(
                mod = "m_AAA",
                subMod = "sm_rbac",
                action = "accessibleModules",
                actionType = "view",
                payload = emptyMap()
            )
            val response = apiClient.dispatch(request)
            if (!response.isSuccessful || response.body()?.isSuccess != true) return null

            val dataElement = response.body()?.data
            if (dataElement == null || !dataElement.isJsonArray) return null

            val type = object : TypeToken<List<ModuleItemDto>>() {}.type
            val dtos: List<ModuleItemDto> = gson.fromJson(dataElement, type)
            dtos.mapNotNull { it.toDomain() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PINNED_MODULES_KEY = "pinned_module_ids"

        /**
         * Module ids that have a real destination wired into the navigation graph
         * (see RootNavHost). These are guaranteed to appear regardless of what the
         * backend returns - shipping a screen the user cannot reach is never correct.
         *
         * `academics` is the backend's id for what the UI now labels "Curriculum".
         * The id and its `/academics` route are part of the m_AAA contract and are
         * deliberately left unchanged; only the display name was renamed.
         */
        val IMPLEMENTED_MODULE_IDS = setOf(
            "academics", "attendance", "exams", "fees", "timetable"
        )

        /**
         * `library` and `placements` have no screen yet and resolve to the generic
         * module template. They stay in the catalog so accounts that already saw them
         * keep seeing them, but they are not force-added to a backend-supplied list.
         */
        val MODULE_CATALOG: List<ModuleItem> = listOf(
            ModuleItem("academics", "Curriculum", "school", "/academics", ModuleItem.ModuleStatus.ACTIVE, "#4F46E5"),
            ModuleItem("attendance", "Attendance", "event_available", "/attendance", ModuleItem.ModuleStatus.ACTIVE, "#10B981"),
            ModuleItem("exams", "Exams", "description", "/exams", ModuleItem.ModuleStatus.ACTIVE, "#EF4444"),
            ModuleItem("fees", "Fees", "currency_rupee", "/fees", ModuleItem.ModuleStatus.ACTIVE, "#06B6D4"),
            ModuleItem("timetable", "Timetable", "schedule", "/timetable", ModuleItem.ModuleStatus.ACTIVE, "#F97316"),
            ModuleItem("library", "Library", "local_library", "/library", ModuleItem.ModuleStatus.ACTIVE, "#8B5CF6"),
            ModuleItem("placements", "Placements", "work", "/placements", ModuleItem.ModuleStatus.ACTIVE, "#0EA5E9")
        )
    }
}
