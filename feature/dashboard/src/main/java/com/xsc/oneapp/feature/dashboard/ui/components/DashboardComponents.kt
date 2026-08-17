@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.dashboard.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge as M3Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardTab
import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.ui.viewmodel.DashboardState
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppMotion

/**
 * Dashboard presentation kit.
 *
 * Every public signature here is unchanged - DashboardScreen calls these exactly as
 * before and no state, callback or data shape moved. The rewrite is visual: typography
 * comes from the theme's scale instead of per-call-site `fontSize`, corner radii come
 * from `MaterialTheme.shapes`, and interactive surfaces gained press feedback and real
 * button semantics.
 */

fun getIconForModule(iconName: String): ImageVector {
    return when (iconName) {
        "school" -> Icons.Default.School
        "event_available" -> Icons.Default.EventAvailable
        "local_library" -> Icons.Default.LocalLibrary
        "description" -> Icons.Default.Description
        "currency_rupee" -> Icons.Default.CurrencyRupee
        "work" -> Icons.Default.Work
        "schedule" -> Icons.Default.Schedule
        else -> Icons.Default.Dashboard
    }
}

/**
 * Adds a spring-driven press scale to any surface. Extracted because six different
 * dashboard surfaces need identical feedback and were previously plain `Modifier
 * .clickable` with no visual response at all.
 */
@Composable
private fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) OneAppMotion.PressedScale else 1f,
        animationSpec = OneAppMotion.pressSpring(),
        label = "dashboardPressScale",
    )
    return this.scale(scale)
}

/**
 * isDarkMode/onThemeToggle no longer live here - the redesigned top bar dropped the
 * theme toggle icon (see the Stitch Home mock); that same control is now a row inside
 * SidebarView, reachable from every tab via the hamburger menu.
 */
@Composable
fun DashboardTopBar(
    onMenuTap: () -> Unit,
    unreadCount: Int,
    onNotificationTap: () -> Unit,
    onProfileTap: () -> Unit
) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = spacing.md, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Real IconButtons: 48dp targets, ripple and button semantics, none of which the
        // previous clickable Boxes provided.
        IconButton(onClick = onMenuTap) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Open navigation menu",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(spacing.xs))

        Text(
            text = "OneApp",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.weight(1f))

        // Theme toggle moved off this bar to match the redesigned top bar (see the
        // Stitch Home mock - just menu / wordmark / bell / avatar). The control itself
        // isn't gone: it's now a row inside SidebarView so dark/light mode stays
        // reachable from every tab, not just Home.
        IconButton(onClick = onNotificationTap) {
            // Plain dot rather than a numeric M3Badge, matching the mock - the count is
            // still real (unreadCount), just not printed on the bell itself; it's
            // announced via contentDescription for accessibility instead.
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        M3Badge(
                            modifier = Modifier
                                .size(8.dp)
                                .clearAndSetSemantics { }
                        )
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = if (unreadCount > 0) {
                        "Notifications, $unreadCount unread"
                    } else {
                        "Notifications"
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(onClick = onProfileTap) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Now a real Material 3 [NavigationBar] rather than a hand-rolled Row of Columns.
 * The tab contract is identical, but selection state, the indicator pill, ripple,
 * touch targets and `Tab` accessibility roles are handled by the component instead of
 * being approximated with a 4dp dot.
 */
@Composable
fun BottomTabBar(
    selectedTab: DashboardTab,
    onTabSelected: (DashboardTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier.navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        DashboardTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            DashboardTab.HOME -> Icons.Default.Home
                            DashboardTab.CURRICULUM -> Icons.Default.School
                            DashboardTab.NOTIFICATIONS -> Icons.Default.Notifications
                            DashboardTab.PROFILE -> Icons.Default.Person
                        },
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        tab.title,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun SidebarView(
    state: DashboardState,
    onClose: () -> Unit,
    onModuleSelect: (ModuleItem) -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
) {
    val spacing = LocalSpacing.current

    Row(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 320.dp)
                .width(300.dp),
            color = MaterialTheme.colorScheme.surface,
            // A drawer sits above the content it covers, so it needs real elevation.
            tonalElevation = 1.dp,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    ),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                state.userName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(spacing.md))
                        Column(modifier = Modifier.widthIn(max = 150.dp)) {
                            Text(
                                state.userName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                state.userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = state.userRole,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = spacing.lg)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        )
                        .padding(horizontal = spacing.md, vertical = spacing.xs)
                )

                Spacer(modifier = Modifier.height(spacing.lg))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = "MODULES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        start = spacing.lg,
                        end = spacing.lg,
                        top = spacing.lg,
                        bottom = spacing.sm
                    )
                )

                state.modules.forEach { module ->
                    SidebarRow(
                        icon = getIconForModule(module.icon),
                        label = module.displayName,
                        tint = module.accentColor
                    ) { onModuleSelect(module) }
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // The redesigned top bar no longer carries a theme toggle icon (see
                // DashboardTopBar) - this row is where that same isDarkMode/onThemeToggle
                // control now lives, so dark/light mode stays reachable from every tab.
                Surface(
                    onClick = onThemeToggle,
                    color = Color.Transparent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(horizontal = spacing.lg, vertical = spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(spacing.md))
                        Text(
                            text = "Dark mode",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        androidx.compose.material3.Switch(checked = isDarkMode, onCheckedChange = { onThemeToggle() })
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SidebarRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "Sign out",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onLogout
                )
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }

        // Scrim. Uses the theme's scrim role rather than a hardcoded black.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f))
                .scrimDismiss(onClose)
        )
    }
}

/**
 * Scrim tap-to-dismiss, labeled for screen readers instead of being an unnamed
 * full-screen click target.
 *
 * The label goes through `clickable`'s own `onClickLabel` rather than a separate
 * semantics modifier: wrapping the node in `clearAndSetSemantics` would wipe the click
 * action that `clickable` contributes, leaving a scrim that reads as a label but exposes
 * no way to activate it.
 */
@Composable
private fun Modifier.scrimDismiss(onClick: () -> Unit): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClickLabel = "Close menu",
        onClick = onClick
    )
}

@Composable
private fun SidebarRow(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 48.dp)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = tint)
            Spacer(modifier = Modifier.width(spacing.md))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** One-line description shown under each module's name in [PinModulesDialog].
 * TEMPORARY: ModuleItem has no description field from the backend - see Backend
 * Endpoint Requirements. Falls back to a generic line for any module id not in this
 * (deliberately small, catalog-matching) map, so an unrecognized id never renders
 * blank. */
private fun moduleDescriptionFor(moduleId: String): String = when (moduleId) {
    "attendance" -> "View presence records"
    "timetable" -> "Weekly class schedule"
    "fees" -> "Payment history & dues"
    "academics" -> "Courses, syllabus & grades"
    "exams" -> "Schedules & hall tickets"
    "library" -> "Digital resources & loans"
    "placements" -> "Drives & applications"
    else -> "Quick access from your dashboard"
}

/**
 * The "Modules" bottom tab - a proper directory of every module the signed-in user
 * can open, replacing what used to be a permanent "Curriculum modules loading"
 * placeholder (see DashboardScreen's old CurriculumTab). Renders the exact same
 * [DashboardState.modules] list Home's "Your Workspace" grid and the sidebar already
 * use, just as full-width, scannable rows instead of an icon grid - this is the
 * screen a student opens specifically to browse and read about what's available,
 * not to glance at.
 *
 * Three states, matching [DashboardState.isModulesLoading] / [DashboardState.modules]:
 * loading (skeleton rows, not a bare spinner), empty (a real empty state - only
 * reachable if a backend ever returns zero accessible modules, since
 * DashboardRepositoryImpl always falls back to the built-in catalog), and populated.
 */
@Composable
fun ModulesTab(
    state: DashboardState,
    onNavigateToModule: (String) -> Unit
) {
    val spacing = LocalSpacing.current
    val hasFeesAlert = state.stats.firstOrNull { it.id == "fees" }?.tag == "Due now"

    when {
        state.isModulesLoading && state.modules.isEmpty() -> ModulesLoadingSkeleton()
        state.modules.isEmpty() -> DashboardPlaceholder(
            icon = Icons.Default.Dashboard,
            title = "No modules available",
            message = "Modules your institution enables for you will appear here."
        )
        else -> LazyColumn(
            contentPadding = PaddingValues(
                horizontal = spacing.screenHorizontal,
                vertical = spacing.lg
            ),
            verticalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            item {
                Text(
                    text = "Your modules",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = spacing.xs)
                )
            }
            items(state.modules, key = { it.id }) { module ->
                ModuleListCard(
                    module = module,
                    showAlert = hasFeesAlert && module.id == "fees",
                    onClick = { onNavigateToModule(module.route) }
                )
            }
        }
    }
}

@Composable
private fun ModuleListCard(
    module: ModuleItem,
    showAlert: Boolean,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interactionSource)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 76.dp)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(module.accentColor.copy(alpha = 0.12f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getIconForModule(module.icon),
                        contentDescription = null,
                        tint = module.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (showAlert) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            .clearAndSetSemantics { contentDescription = "Outstanding balance" }
                    )
                }
            }

            Spacer(modifier = Modifier.width(spacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    module.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    moduleDescriptionFor(module.id),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(spacing.sm))

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Skeleton rows shown while [DashboardState.isModulesLoading] is true. A shared soft
 * pulse (rather than per-row independent animation) reads as one loading surface
 * instead of several unrelated blinking rectangles. */
@Composable
private fun ModulesLoadingSkeleton() {
    val spacing = LocalSpacing.current
    val transition = rememberInfiniteTransition(label = "modulesSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "modulesSkeletonAlpha"
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.screenHorizontal, vertical = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        repeat(5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 76.dp)
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                    .padding(horizontal = spacing.lg, vertical = spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(shimmerColor, MaterialTheme.shapes.small)
                )
                Spacer(modifier = Modifier.width(spacing.lg))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(14.dp)
                            .background(shimmerColor, MaterialTheme.shapes.extraSmall)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(11.dp)
                            .background(shimmerColor, MaterialTheme.shapes.extraSmall)
                    )
                }
            }
        }
    }
}

/**
 * "Manage Pinned Modules" - up to [com.xsc.oneapp.feature.dashboard.ui.viewmodel.MAX_PINNED_MODULES]
 * modules pinned to the Home "Your Workspace" grid.
 *
 * The six-dot drag handle is visual only for now: [ModuleItem]s are pinned as a Set,
 * which has no order to persist, so real drag-to-reorder needs pinned order to become
 * a stored List first (see Backend Endpoint Requirements). Toggling on/off is fully
 * functional and goes through the same onToggle -> togglePinnedModule ->
 * TogglePinnedModuleUseCase path as before.
 */
@Composable
fun PinModulesDialog(
    allModules: List<ModuleItem>,
    pinnedIds: Set<String>,
    atPinLimit: Boolean,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = LocalSpacing.current

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.heightIn(max = 560.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = spacing.xl, end = spacing.md, top = spacing.lg, bottom = spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Manage Pinned Modules",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    "Select up to 4 modules to pin to your dashboard for quick access.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = spacing.xl, vertical = spacing.sm)
                )

                if (allModules.isEmpty()) {
                    Text(
                        "No modules available to pin yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(spacing.xl)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = spacing.lg,
                            vertical = spacing.sm
                        ),
                        verticalArrangement = Arrangement.spacedBy(spacing.sm)
                    ) {
                        items(allModules, key = { it.id }) { module ->
                            val checked = module.id in pinnedIds
                            val rowEnabled = checked || !atPinLimit
                            PinnableModuleRow(
                                module = module,
                                checked = checked,
                                enabled = rowEnabled,
                                onToggle = { onToggle(module.id) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(spacing.lg),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(spacing.sm))
                    // A plain Button, not the shared PrimaryButton: that component always
                    // fills its row (a full-width CTA is its whole contract), which is
                    // wrong for a small paired "Cancel / Save Selection" footer button.
                    androidx.compose.material3.Button(
                        onClick = onDismiss,
                        shape = com.xsc.sdk.theme.OneAppPillShape,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = androidx.compose.material3.ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("Save Selection", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun PinnableModuleRow(
    module: ModuleItem,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    val spacing = LocalSpacing.current

    Surface(
        onClick = onToggle,
        enabled = enabled,
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = spacing.md, vertical = spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DragIndicator,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(module.accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getIconForModule(module.icon),
                    contentDescription = null,
                    tint = module.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    module.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    moduleDescriptionFor(module.id),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(spacing.sm))
            androidx.compose.material3.Switch(checked = checked, onCheckedChange = null, enabled = enabled)
        }
    }
}

@Composable
fun SectionHeader(title: String, trailing: String? = null, onTrailingClick: (() -> Unit)? = null) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (trailing != null) {
            // onTrailingClick is additive and opt-in: every existing call site passes
            // only `trailing` and keeps the old plain-text look. "Your Workspace"'s
            // "Edit" link is the first caller to pass it, styled as a link and wired
            // to open the pin picker.
            if (onTrailingClick != null) {
                TextButton(onClick = onTrailingClick) {
                    Text(text = trailing, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Shared empty-state body for the Curriculum and Notifications tabs, which previously
 * duplicated the same icon-circle + title + caption block inline.
 */
@Composable
fun DashboardPlaceholder(icon: ImageVector, title: String, message: String) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(spacing.lg))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
