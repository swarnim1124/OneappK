@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.dashboard.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge as M3Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardStat
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardTab
import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem
import com.xsc.oneapp.feature.dashboard.domain.model.QuickAction
import com.xsc.oneapp.feature.dashboard.ui.viewmodel.DashboardState
import com.xsc.sdk.commonui.avatar.InitialsAvatar
import com.xsc.sdk.commonui.menu.MenuRow
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

fun getIconForStat(iconName: String): ImageVector {
    return when (iconName) {
        "ic_clock" -> Icons.Default.Schedule
        "ic_clock_outline" -> Icons.Default.AccessTime
        "ic_rupee" -> Icons.Default.CurrencyRupee
        "ic_document" -> Icons.Default.Description
        else -> Icons.Default.BarChart
    }
}

fun getIconForQuickAction(iconName: String): ImageVector {
    return when (iconName) {
        "ic_id_card" -> Icons.Default.Badge
        "ic_qr_code" -> Icons.Default.QrCode
        "ic_users" -> Icons.Default.People
        "ic_chart" -> Icons.Default.BarChart
        else -> Icons.Default.FlashOn
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

@Composable
fun DashboardTopBar(
    userName: String,
    onMenuTap: () -> Unit,
    unreadCount: Int,
    onNotificationTap: () -> Unit,
    onProfileTap: () -> Unit,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit
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

        IconButton(onClick = onThemeToggle) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (isDarkMode) "Switch to light theme" else "Switch to dark theme",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onNotificationTap) {
            // A proper M3 badge replaces the hand-drawn dot, so the count is announced
            // rather than being a decorative circle screen readers skip.
            BadgedBox(
                badge = {
                    if (unreadCount > 0) {
                        M3Badge { Text(unreadCount.toString()) }
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

        IconButton(
            onClick = onProfileTap,
            modifier = Modifier.semantics { contentDescription = "Profile" }
        ) {
            InitialsAvatar(
                name = userName,
                size = 32.dp,
                textStyle = MaterialTheme.typography.labelLarge
            )
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
        // Notifications stays a DashboardTab (the top bar's bell icon and NotificationsTab
        // still use it) but is dropped from the bottom bar itself - filtered out here
        // rather than removed from the enum, so nothing else that depends on it breaks.
        DashboardTab.entries.filter { it != DashboardTab.NOTIFICATIONS }.forEach { tab ->
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
    onLogout: () -> Unit
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
                        InitialsAvatar(
                            name = state.userName,
                            size = 44.dp,
                            textStyle = MaterialTheme.typography.titleMedium
                        )
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
                    MenuRow(
                        icon = getIconForModule(module.icon),
                        label = module.displayName,
                        iconTint = module.accentColor
                    ) { onModuleSelect(module) }
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                MenuRow(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    label = "Sign out",
                    iconTint = MaterialTheme.colorScheme.error,
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
 * Scrim tap-to-dismiss, labelled for screen readers instead of being an unnamed
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
fun PinnedInfoSection(
    pinnedModules: List<ModuleItem>,
    onManageClick: () -> Unit,
    onModuleClick: (ModuleItem) -> Unit,
    onUnpin: (String) -> Unit
) {
    val spacing = LocalSpacing.current

    if (pinnedModules.isEmpty()) {
        // The dashed-border placeholder is replaced by a proper outlined card: the same
        // "nothing here yet, tap to add" message without the hand-drawn look.
        Card(
            onClick = onManageClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(spacing.lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(spacing.md))
                Column {
                    Text(
                        "Pinned",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Pin modules here for quick access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    } else {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pinned",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onManageClick) {
                    Text("Manage", style = MaterialTheme.typography.labelLarge)
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                items(pinnedModules, key = { it.id }) { module ->
                    PinnedModuleChip(
                        module = module,
                        onClick = { onModuleClick(module) },
                        onUnpin = { onUnpin(module.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PinnedModuleChip(module: ModuleItem, onClick: () -> Unit, onUnpin: () -> Unit) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .width(132.dp)
            .pressScale(interactionSource),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getIconForModule(module.icon),
                    contentDescription = null,
                    tint = module.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                IconButton(
                    onClick = onUnpin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Unpin ${module.displayName}",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(spacing.sm))
            Text(
                text = module.displayName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PinModulesDialog(
    allModules: List<ModuleItem>,
    pinnedIds: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = LocalSpacing.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.large,
        title = { Text("Pin modules", style = MaterialTheme.typography.headlineSmall) },
        text = {
            if (allModules.isEmpty()) {
                Text(
                    "No modules available to pin yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column {
                    allModules.forEach { module ->
                        val checked = module.id in pinnedIds
                        Surface(
                            onClick = { onToggle(module.id) },
                            color = Color.Transparent,
                            shape = MaterialTheme.shapes.extraSmall,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .padding(vertical = spacing.xs),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // null callback: the row owns the click, so the checkbox
                                // must not be separately focusable or TalkBack announces
                                // two controls for one choice.
                                Checkbox(checked = checked, onCheckedChange = null)
                                Spacer(modifier = Modifier.width(spacing.md))
                                Icon(
                                    getIconForModule(module.icon),
                                    contentDescription = null,
                                    tint = module.accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(spacing.md))
                                Text(
                                    module.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
fun StatCardView(stat: DashboardStat) {
    val spacing = LocalSpacing.current

    Card(
        modifier = Modifier
            .width(168.dp)
            .heightIn(min = 112.dp)
            // Read as one phrase; previously the value and title were two separate
            // announcements with the tag orphaned between them.
            .clearAndSetSemantics {
                contentDescription = "${stat.title}: ${stat.value}. ${stat.tag}"
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.lg),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        getIconForStat(stat.icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = stat.tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.shapes.extraSmall
                        )
                        .padding(horizontal = spacing.sm, vertical = 2.dp)
                )
            }
            Column {
                Text(
                    stat.value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stat.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ModuleCardView(modifier: Modifier = Modifier, module: ModuleItem, onClick: () -> Unit) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    // Accent wash deepens slightly on press - motion the user feels rather than sees.
    val containerColor by animateColorAsState(
        targetValue = if (pressed) {
            module.accentColor.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = OneAppMotion.settleSpring(),
        label = "moduleCardContainer"
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .aspectRatio(1f)
            .pressScale(interactionSource),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(module.accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIconForModule(module.icon),
                    contentDescription = null,
                    tint = module.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(spacing.md))
            Text(
                text = module.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickActionRow(action: QuickAction) {
    val spacing = LocalSpacing.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getIconForQuickAction(action.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(spacing.lg))
            Text(
                action.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Soon",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.extraSmall
                    )
                    .padding(horizontal = spacing.sm, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.width(spacing.sm))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, trailing: String? = null) {
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
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
