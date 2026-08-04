package com.xsc.oneapp.feature.dashboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardStat
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardTab
import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.domain.model.QuickAction
import com.xsc.oneapp.feature.dashboard.domain.usecase.GetAccessibleModulesUseCase
import com.xsc.oneapp.feature.dashboard.domain.usecase.GetPinnedModuleIdsUseCase
import com.xsc.oneapp.feature.dashboard.domain.usecase.TogglePinnedModuleUseCase
import com.xsc.sdk.auth.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardState(
    val selectedTab: DashboardTab = DashboardTab.HOME,
    val isSidebarOpen: Boolean = false,
    val unreadNotifications: Int = 3,
    val modules: List<ModuleItem> = emptyList(),
    val stats: List<DashboardStat> = emptyList(),
    val quickActions: List<QuickAction> = emptyList(),
    val selectedModule: ModuleItem? = null,
    val userName: String = "",
    val userEmail: String = "",
    val userRole: String = "User",
    val pinnedModuleIds: Set<String> = emptySet(),
    val isPinPickerOpen: Boolean = false,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val getAccessibleModulesUseCase: GetAccessibleModulesUseCase,
    private val getPinnedModuleIdsUseCase: GetPinnedModuleIdsUseCase,
    private val togglePinnedModuleUseCase: TogglePinnedModuleUseCase,
    private val statProviders: Set<@JvmSuppressWildcards DashboardStatProvider>
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadSessionData()
        loadAccessibleModules()
        _state.update { it.copy(pinnedModuleIds = getPinnedModuleIdsUseCase()) }
    }

    private fun loadSessionData() {
        _state.update {
            it.copy(
                // We use mock names if not present in session yet
                userName = sessionManager.getDisplayName(),
                userEmail = sessionManager.currentEmail.value ?: "jane.doe@university.edu",
                userRole = sessionManager.currentRole.value ?: "Student"
            )
        }
    }

    fun getGreeting(): String {
        return when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..21 -> "Good Evening"
            else -> "Good Night"
        }
    }

    fun getRoleSubtitle(): String {
        return when (_state.value.userRole.lowercase()) {
            "student" -> "B.Tech Computer Science"
            "teacher" -> "Associate Professor"
            "admin" -> "System Administrator"
            else -> _state.value.userRole
        }
    }

    fun setTab(tab: DashboardTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun toggleSidebar() {
        _state.update { it.copy(isSidebarOpen = !it.isSidebarOpen) }
    }

    fun closeSidebar() {
        _state.update { it.copy(isSidebarOpen = false) }
    }

    fun selectModule(module: ModuleItem) {
        _state.update { it.copy(selectedModule = module) }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
        }
    }

    fun togglePinnedModule(moduleId: String) {
        val updated = togglePinnedModuleUseCase(moduleId)
        _state.update { it.copy(pinnedModuleIds = updated) }
    }

    fun openPinPicker() {
        _state.update { it.copy(isPinPickerOpen = true) }
    }

    fun closePinPicker() {
        _state.update { it.copy(isPinPickerOpen = false) }
    }

    private fun loadAccessibleModules() {
        viewModelScope.launch {
            val modules = getAccessibleModulesUseCase()
            val mergedStats = mergeLiveStats(getMockStats())
            _state.update {
                it.copy(
                    stats = mergedStats,
                    quickActions = getMockQuickActions(),
                    modules = modules
                )
            }
        }
    }

    /** Overlays any stat a business module actually has live data for (via
     * DashboardStatProvider) onto the placeholder list, matched by stat id. A stat
     * id with no registered provider - or whose provider returns null - keeps its
     * placeholder value untouched. */
    private suspend fun mergeLiveStats(placeholders: List<DashboardStat>): List<DashboardStat> {
        val liveById = statProviders
            .mapNotNull { provider -> provider.provideStat()?.let { it.id to it } }
            .toMap()
        return placeholders.map { placeholder ->
            val live = liveById[placeholder.id] ?: return@map placeholder
            placeholder.copy(value = live.value, tag = live.tag, title = live.title)
        }
    }

    private fun getMockStats(): List<DashboardStat> {
        return listOf(
            DashboardStat("attendance", "ATTENDANCE", "--", "ic_clock", "Coming Soon", DashboardStat.TagStyle.NEUTRAL),
            DashboardStat("fees", "PENDING FEES", "--", "ic_rupee", "Coming Soon", DashboardStat.TagStyle.NEUTRAL),
            DashboardStat("nextclass", "NEXT CLASS", "--", "ic_clock_outline", "Coming Soon", DashboardStat.TagStyle.NEUTRAL),
            DashboardStat("assignments", "ASSIGNMENTS", "--", "ic_document", "Coming Soon", DashboardStat.TagStyle.NEUTRAL)
        )
    }

    private fun getMockQuickActions(): List<QuickAction> {
        return listOf(
            QuickAction("digitalid", "Download Digital ID", "ic_id_card", isAvailable = false),
            QuickAction("qrscan", "QR Scan Attendance", "ic_qr_code", isAvailable = false),
            QuickAction("directory", "Faculty Directory", "ic_users", isAvailable = false),
            QuickAction("sgpa", "View SGPA / CGPA", "ic_chart", isAvailable = false)
        )
    }
}
