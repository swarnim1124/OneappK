package com.xsc.oneapp.feature.dashboard.domain.model

import androidx.compose.ui.graphics.Color
import com.google.gson.annotations.SerializedName

data class ModuleItem(
    val id: String,
    val displayName: String,
    val icon: String,
    val route: String,
    val status: ModuleStatus,
    val accentColorString: String
) {
    enum class ModuleStatus(val value: String) {
        @SerializedName("Active") ACTIVE("Active"),
        @SerializedName("Coming Soon") COMING_SOON("Coming Soon"),
        @SerializedName("Maintenance") MAINTENANCE("Maintenance");

        companion object {
            /**
             * Maps a raw wire value to a status, defaulting to [ACTIVE] for anything
             * unrecognized (including null). Previously the DTO declared this enum
             * directly, which meant Gson wrote `null` into a non-null Kotlin field for
             * any status string the backend added later - producing a module that
             * blew up at render time instead of simply showing. Unknown status must
             * never be a reason a working module disappears.
             */
            fun fromWire(raw: String?): ModuleStatus =
                ModuleStatus.entries.firstOrNull { it.value.equals(raw, ignoreCase = true) }
                    ?: ModuleStatus.ACTIVE
        }
    }

    /**
     * Parsed once per instance rather than on every read: this is called from inside
     * a LazyVerticalGrid item and a LazyRow item, so a `get()` that parsed a string
     * (and caught an exception) ran on every recomposition of every visible card.
     */
    val accentColor: Color by lazy {
        try {
            Color(android.graphics.Color.parseColor(accentColorString))
        } catch (e: Exception) {
            // Stays a broad catch on purpose: on the JVM unit-test classpath
            // android.graphics.Color throws RuntimeException("Stub!"), not
            // IllegalArgumentException, and a colour string must never be fatal.
            Color(0xFF4F46E5) // Fallback color
        }
    }
}

data class DashboardStat(
    val id: String,
    val title: String,
    val value: String,
    val icon: String,
    val tag: String,
    val tagStyle: TagStyle
) {
    enum class TagStyle {
        POSITIVE, WARNING, NEUTRAL
    }
}

data class QuickAction(
    val id: String,
    val title: String,
    val icon: String,
    val isAvailable: Boolean
)

enum class DashboardTab(val title: String, val icon: String) {
    HOME("Home", "ic_home"),
    // Display labels only - renamed to match the redesigned bottom tab bar. Enum
    // names (and every setTab(DashboardTab.CURRICULUM/.NOTIFICATIONS) call site)
    // are unchanged, so no navigation wiring moved.
    CURRICULUM("Modules", "ic_school"),
    NOTIFICATIONS("Alerts", "ic_notifications"),
    PROFILE("Profile", "ic_person")
}

/**
 * A single row in the Home "Actions & Feed" list and the Notifications tab.
 *
 * TEMPORARY DATA SOURCE: no activity/notification feed endpoint exists anywhere in the
 * codebase yet (sdk/XscNotificationSDK is an unimplemented placeholder). DashboardViewModel
 * currently populates this from a static in-memory list - see its
 * Backend Endpoint Requirements note for the real endpoint this should be replaced with.
 */
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val icon: String,
    val isUnread: Boolean,
    val group: NotificationGroup
)

enum class NotificationGroup(val label: String) {
    TODAY("TODAY"),
    EARLIER("EARLIER")
}
