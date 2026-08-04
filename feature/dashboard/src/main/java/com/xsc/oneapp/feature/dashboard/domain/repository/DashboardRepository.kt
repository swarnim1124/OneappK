package com.xsc.oneapp.feature.dashboard.domain.repository

import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem

interface DashboardRepository {
    /**
     * No longer takes the signed-in email: it existed only to drive a client-side
     * role guess (`email.startsWith("admin")` -> admin module set), which both
     * granted access from an address prefix and silently hid implemented modules
     * from any account that didn't match one of three hardcoded prefixes.
     */
    suspend fun getAccessibleModules(): List<ModuleItem>
    fun getPinnedModuleIds(): Set<String>
    fun setPinnedModuleIds(ids: Set<String>)
}
