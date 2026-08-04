@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.dashboard.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xsc.oneapp.feature.dashboard.domain.model.DashboardTab
import com.xsc.oneapp.feature.dashboard.ui.components.BottomTabBar
import com.xsc.oneapp.feature.dashboard.ui.components.DashboardPlaceholder
import com.xsc.oneapp.feature.dashboard.ui.components.DashboardTopBar
import com.xsc.oneapp.feature.dashboard.ui.components.ModuleCardView
import com.xsc.oneapp.feature.dashboard.ui.components.PinModulesDialog
import com.xsc.oneapp.feature.dashboard.ui.components.PinnedInfoSection
import com.xsc.oneapp.feature.dashboard.ui.components.QuickActionRow
import com.xsc.oneapp.feature.dashboard.ui.components.SectionHeader
import com.xsc.oneapp.feature.dashboard.ui.components.SidebarView
import com.xsc.oneapp.feature.dashboard.ui.components.StatCardView
import com.xsc.oneapp.feature.dashboard.ui.viewmodel.DashboardState
import com.xsc.oneapp.feature.dashboard.ui.viewmodel.DashboardViewModel
import com.xsc.sdk.theme.LocalDarkTheme
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.LocalThemeToggle
import com.xsc.sdk.theme.OneAppMotion

/**
 * Dashboard shell.
 *
 * Presentation only. Every ViewModel call, tab value, navigation callback and dialog
 * trigger is unchanged from the previous version - the differences are spacing, the type
 * scale, elevation hierarchy and motion.
 */
@Composable
fun DashboardScreen(
    onNavigateToModule: (String) -> Unit,
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDarkMode = LocalDarkTheme.current
    val toggleTheme = LocalThemeToggle.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DashboardTopBar(
                onMenuTap = { viewModel.toggleSidebar() },
                unreadCount = state.unreadNotifications,
                onNotificationTap = { viewModel.setTab(DashboardTab.NOTIFICATIONS) },
                onProfileTap = { viewModel.setTab(DashboardTab.PROFILE) },
                isDarkMode = isDarkMode,
                onThemeToggle = { toggleTheme() }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (state.selectedTab) {
                    DashboardTab.HOME -> HomeTab(
                        state = state,
                        greeting = viewModel.getGreeting(),
                        roleSubtitle = viewModel.getRoleSubtitle(),
                        onNavigateToModule = onNavigateToModule,
                        onOpenPinPicker = { viewModel.openPinPicker() },
                        onUnpinModule = { viewModel.togglePinnedModule(it) }
                    )
                    DashboardTab.CURRICULUM -> CurriculumTab()
                    DashboardTab.NOTIFICATIONS -> NotificationsTab()
                    DashboardTab.PROFILE -> ProfileTab(
                        state = state,
                        onSignOut = {
                            viewModel.logout()
                            onLogout()
                        },
                        onNavigateToModule = onNavigateToModule,
                        onChangePassword = onChangePassword,
                        onNotificationsClick = { viewModel.setTab(DashboardTab.NOTIFICATIONS) }
                    )
                }
            }

            BottomTabBar(
                selectedTab = state.selectedTab,
                onTabSelected = { viewModel.setTab(it) }
            )
        }

        // Scrim fades while the panel slides - the panel used to appear over an
        // untinted screen, so it read as a floating rectangle rather than a drawer.
        AnimatedVisibility(
            visible = state.isSidebarOpen,
            enter = slideInHorizontally(
                animationSpec = tween(OneAppMotion.DurationLong, easing = OneAppMotion.EmphasizedDecelerate),
                initialOffsetX = { -it }
            ) + fadeIn(tween(OneAppMotion.DurationShort)),
            exit = slideOutHorizontally(
                animationSpec = tween(OneAppMotion.DurationMedium, easing = OneAppMotion.EmphasizedAccelerate),
                targetOffsetX = { -it }
            ) + fadeOut(tween(OneAppMotion.DurationShort)),
            modifier = Modifier.zIndex(10f)
        ) {
            SidebarView(
                state = state,
                onClose = { viewModel.closeSidebar() },
                onModuleSelect = { module ->
                    viewModel.selectModule(module)
                    onNavigateToModule(module.route)
                },
                onLogout = {
                    viewModel.logout()
                    onLogout()
                }
            )
        }

        if (state.isPinPickerOpen) {
            PinModulesDialog(
                allModules = state.modules,
                pinnedIds = state.pinnedModuleIds,
                onToggle = { viewModel.togglePinnedModule(it) },
                onDismiss = { viewModel.closePinPicker() }
            )
        }
    }
}

@Composable
private fun HomeTab(
    state: DashboardState,
    greeting: String,
    roleSubtitle: String,
    onNavigateToModule: (String) -> Unit,
    onOpenPinPicker: () -> Unit,
    onUnpinModule: (String) -> Unit
) {
    val spacing = LocalSpacing.current

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.sm,
            bottom = spacing.xxl
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing.md),
        verticalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        // Adaptive columns rather than a hard Fixed(3): three tiles is right on a
        // phone and cramped on a tablet where five fit comfortably. Full-width rows use
        // `maxLineSpan`, which is only in scope inside the span lambda itself.
        item(key = "greeting", span = { GridItemSpan(maxLineSpan) }) {
            Column(modifier = Modifier.padding(vertical = spacing.sm)) {
                Text(
                    text = "$greeting, ${state.userName}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Text(
                    text = roleSubtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item(key = "pinned", span = { GridItemSpan(maxLineSpan) }) {
            PinnedInfoSection(
                pinnedModules = state.modules.filter { it.id in state.pinnedModuleIds },
                onManageClick = onOpenPinPicker,
                onModuleClick = { onNavigateToModule(it.route) },
                onUnpin = onUnpinModule
            )
        }

        item(key = "overview-header", span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(title = "Overview")
        }

        item(key = "stats", span = { GridItemSpan(maxLineSpan) }) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.md)) {
                items(state.stats, key = { it.id }) { stat -> StatCardView(stat = stat) }
            }
        }

        item(key = "modules-header", span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(title = "Your modules")
        }

        items(state.modules, key = { it.id }) { module ->
            ModuleCardView(module = module) { onNavigateToModule(module.route) }
        }

        item(key = "quick-header", span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(title = "Quick actions")
        }

        item(key = "quick-actions", span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                state.quickActions.forEach { action -> QuickActionRow(action = action) }
            }
        }
    }
}

@Composable
private fun CurriculumTab() {
    DashboardPlaceholder(
        icon = Icons.Default.School,
        title = "Curriculum modules loading",
        message = "Course details, grades and schedules will appear here once integrated."
    )
}

@Composable
private fun NotificationsTab() {
    DashboardPlaceholder(
        icon = Icons.Default.NotificationsOff,
        title = "No notifications yet",
        message = "Notifications will appear here once the notification service is integrated."
    )
}

@Composable
private fun ProfileTab(
    state: DashboardState,
    onSignOut: () -> Unit,
    onNavigateToModule: (String) -> Unit,
    onChangePassword: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(spacing.xxl))

        Box(
            modifier = Modifier
                .size(80.dp)
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
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(spacing.lg))
        Text(
            state.userName,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        Text(
            state.userEmail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(spacing.md))

        Text(
            text = state.userRole,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .padding(horizontal = spacing.md, vertical = spacing.xs)
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            ProfileRow(Icons.Default.PersonOutline, "Edit profile") { onNavigateToModule("/profile") }
            RowDivider()
            ProfileRow(Icons.Default.Lock, "Change password", onClick = onChangePassword)
            RowDivider()
            ProfileRow(Icons.Default.NotificationsNone, "Notifications", onClick = onNotificationsClick)
            RowDivider()
            ProfileRow(Icons.AutoMirrored.Filled.HelpOutline, "Help & support") { onNavigateToModule("/help") }
            RowDivider()
            ProfileRow(Icons.Default.Info, "About OneApp") { onNavigateToModule("/about") }
        }

        Spacer(modifier = Modifier.height(spacing.lg))

        Card(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(spacing.sm))
                Text(
                    "Sign out",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xxl))
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier.padding(start = 56.dp)
    )
}

@Composable
private fun ProfileRow(icon: ImageVector, title: String, onClick: () -> Unit = {}) {
    val spacing = LocalSpacing.current

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(spacing.lg))
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
