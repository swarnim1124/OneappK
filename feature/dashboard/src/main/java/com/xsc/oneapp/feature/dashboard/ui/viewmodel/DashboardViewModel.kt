package com.xsc.oneapp.feature.dashboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xsc.oneapp.core.dashboard.DashboardStatProvider
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardStat
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardTab
import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.domain.model.NotificationGroup
import com.xsc.oneapp.feature.dashboard.domain.model.NotificationItem
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

/** Home "Your Workspace" grid enforces this via [DashboardState.pinnedLimitReached] -
 * matches the redesigned Pin Modules dialog copy ("Select up to 4 modules"). */
const val MAX_PINNED_MODULES = 4

data class DashboardState(
    val selectedTab: DashboardTab = DashboardTab.HOME,
    val isSidebarOpen: Boolean = false,
    val unreadNotifications: Int = 0,
    val modules: List<ModuleItem> = emptyList(),
    val stats: List<DashboardStat> = emptyList(),
    val quickActions: List<QuickAction> = emptyList(),
    val selectedModule: ModuleItem? = null,
    val userName: String = "",
    /** First token of [userName] - what the Home greeting and any "Hi X" copy should
     * use instead of the full name (see SessionManager.getFirstName). */
    val userFirstName: String = "",
    val userEmail: String = "",
    val userRole: String = "User",
    /** True until [GetAccessibleModulesUseCase] resolves once. Drives the Modules
     * tab's loading state - distinguishes "still fetching" from "backend genuinely
     * returned nothing", which an empty [modules] list alone can't tell apart. */
    val isModulesLoading: Boolean = true,
    val pinnedModuleIds: Set<String> = emptySet(),
    val isPinPickerOpen: Boolean = false,
    // TEMPORARY: see NotificationItem's doc comment - populated from a static list
    // below until a real notification/activity feed endpoint exists.
    val notifications: List<NotificationItem> = emptyList(),
    // TEMPORARY: Home "Actions & Feed" preview - a separate, shorter static list from
    // [notifications] because the Stitch mocks show different sample content for the
    // Home feed vs. the full Notifications tab. Both should come from the same real
    // activity-feed endpoint once one exists (see Backend Endpoint Requirements) -
    // this field can likely be dropped in favour of notifications.take(n) at that point.
    val recentActivity: List<NotificationItem> = emptyList(),
) {
    val pinnedLimitReached: Boolean get() = pinnedModuleIds.size >= MAX_PINNED_MODULES
}

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
        loadNotifications()
    }

    private fun loadSessionData() {
        _state.update {
            it.copy(
                // All come straight from SessionManager, which derives them from the
                // signed-in user's JWT (see SessionManager.refreshFromToken) - there
                // is no per-user data hardcoded here, only generic fallbacks for the
                // brief window before a token has been parsed.
                userName = sessionManager.getDisplayName(),
                userFirstName = sessionManager.getFirstName(),
                userEmail = sessionManager.currentEmail.value ?: "",
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

    /**
     * Display label for the role badge under the greeting.
     *
     * This used to map each role to a fixed, made-up program/title ("B.Tech Computer
     * Science" for every student, "Associate Professor" for every teacher) regardless
     * of who was actually signed in - real-looking but entirely fabricated text. There
     * is no department/course/designation field anywhere in the profile contract this
     * app reads (see ProfileModels.AcademicDetail) to show instead, so rather than
     * invent one, this now just presents the real, backend-provided role
     * (SessionManager.currentRole, from the JWT) in title case.
     */
    fun getRoleSubtitle(): String {
        val role = _state.value.userRole
        return role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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

    /**
     * No-ops once [MAX_PINNED_MODULES] are pinned and [moduleId] isn't already one of
     * them - unpinning always goes through. The dialog reads [DashboardState.pinnedLimitReached]
     * to grey out the remaining switches instead of letting the tap silently do nothing.
     */
    fun togglePinnedModule(moduleId: String) {
        val current = _state.value.pinnedModuleIds
        if (moduleId !in current && current.size >= MAX_PINNED_MODULES) return
        val updated = togglePinnedModuleUseCase(moduleId)
        _state.update { it.copy(pinnedModuleIds = updated) }
    }

    fun openPinPicker() {
        _state.update { it.copy(isPinPickerOpen = true) }
    }

    fun closePinPicker() {
        _state.update { it.copy(isPinPickerOpen = false) }
    }

    /** Local-only, matching the temporary notification list: flips every row to read
     * and zeroes the badge. Nothing is sent to a backend - there is no endpoint yet. */
    fun markAllNotificationsRead() {
        _state.update {
            it.copy(
                notifications = it.notifications.map { n -> n.copy(isUnread = false) },
                unreadNotifications = 0
            )
        }
    }

    private fun loadAccessibleModules() {
        viewModelScope.launch {
            val modules = getAccessibleModulesUseCase()
            val mergedStats = mergeLiveStats(getMockStats())
            _state.update {
                it.copy(
                    stats = mergedStats,
                    quickActions = getMockQuickActions(),
                    modules = modules,
                    isModulesLoading = false
                )
            }
        }
    }

    private fun loadNotifications() {
        val notifications = getMockNotifications()
        _state.update {
            it.copy(
                notifications = notifications,
                unreadNotifications = notifications.count { n -> n.isUnread },
                recentActivity = getMockRecentActivity()
            )
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
            // Overlaid with a real percentage by AttendanceDashboardStatProvider
            // (feature/attendance) when the shortage-report call succeeds; this is
            // only what shows before that resolves or if it fails.
            DashboardStat("attendance", "Overall Attendance", "--", "ic_clock", "Coming Soon", DashboardStat.TagStyle.NEUTRAL),
            DashboardStat("fees", "PENDING FEES", "--", "ic_rupee", "Coming Soon", DashboardStat.TagStyle.NEUTRAL),
            // TEMPORARY: feature/timetable's TimetableEntry has no resolved course/room
            // display name (see that model's own doc comment - courseId/roomId are raw
            // ids with no name-resolution endpoint in this module), so "next class"
            // can't be computed from real data yet. Static placeholder until a resolved
            // endpoint exists - see Backend Endpoint Requirements.
            DashboardStat("nextclass", "Next Class", "Data Structures", "ic_clock_outline", "10:00 AM • Room 302", DashboardStat.TagStyle.NEUTRAL),
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

    /**
     * TEMPORARY: static stand-in for the Home "Actions & Feed" preview - see
     * [DashboardState.recentActivity].
     */
    private fun getMockRecentActivity(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = "exam-upcoming",
                title = "Mathematics Exam Upcoming",
                message = "24 Aug • Room 101",
                timestamp = "24 Aug",
                icon = "ic_document",
                isUnread = false,
                group = NotificationGroup.TODAY
            ),
            NotificationItem(
                id = "attendance-marked",
                title = "Attendance marked for Calculus",
                message = "2 hours ago",
                timestamp = "2 hours ago",
                icon = "ic_clock",
                isUnread = false,
                group = NotificationGroup.TODAY
            ),
            NotificationItem(
                id = "fee-payment-received",
                title = "Fee payment received",
                message = "Yesterday",
                timestamp = "Yesterday",
                icon = "ic_rupee",
                isUnread = false,
                group = NotificationGroup.EARLIER
            )
        )
    }

    /**
     * TEMPORARY: static stand-in for the Home "Actions & Feed" list and the
     * Notifications tab - see [NotificationItem]'s doc comment. Content mirrors the
     * Stitch mock so the redesigned UI isn't empty; replace wholesale once a real
     * feed/notification endpoint exists.
     */
    private fun getMockNotifications(): List<NotificationItem> {
        return listOf(
            NotificationItem(
                id = "fee-payment-success",
                title = "Fee Payment Success",
                message = "Your payment of ₹1,250.00 for the Fall Semester has been processed successfully.",
                timestamp = "10m ago",
                icon = "ic_check_circle",
                isUnread = true,
                group = NotificationGroup.TODAY
            ),
            NotificationItem(
                id = "grade-published",
                title = "New Grade Published",
                message = "Professor Smith has published the mid-term grades for Advanced Algorithms.",
                timestamp = "2h ago",
                icon = "ic_school",
                isUnread = true,
                group = NotificationGroup.TODAY
            ),
            NotificationItem(
                id = "timetable-change",
                title = "Timetable Change",
                message = "Physics 101 lecture scheduled for 2:00 PM today has been moved to 3:30 PM, Room 210.",
                timestamp = "5h ago",
                icon = "ic_event_busy",
                isUnread = false,
                group = NotificationGroup.TODAY
            ),
            NotificationItem(
                id = "library-due",
                title = "Library Book Due Tomorrow",
                message = "\"Introduction to Algorithms, 3rd Edition\" is due tomorrow. Please renew or return it.",
                timestamp = "Yesterday",
                icon = "ic_library",
                isUnread = false,
                group = NotificationGroup.EARLIER
            )
        )
    }
}
