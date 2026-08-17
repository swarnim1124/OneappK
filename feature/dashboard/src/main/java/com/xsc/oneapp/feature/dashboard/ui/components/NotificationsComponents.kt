package com.xsc.oneapp.feature.dashboard.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.xsc.oneapp.feature.dashboard.domain.model.NotificationGroup
import com.xsc.oneapp.feature.dashboard.domain.model.NotificationItem
import com.xsc.sdk.theme.LocalSpacing

/**
 * Notifications tab.
 *
 * TEMPORARY DATA SOURCE: [notifications] comes from DashboardViewModel's static list
 * (see NotificationItem's doc comment) - no notification endpoint exists yet.
 * "Mark all as read" is real and functional against that local state
 * (DashboardViewModel.markAllNotificationsRead), it just isn't persisted anywhere.
 */
@Composable
fun NotificationsList(
    notifications: List<NotificationItem>,
    onMarkAllRead: () -> Unit
) {
    val spacing = LocalSpacing.current

    if (notifications.isEmpty()) {
        DashboardPlaceholder(
            icon = Icons.Default.NotificationsOff,
            title = "No notifications yet",
            message = "Notifications will appear here once the notification service is integrated."
        )
        return
    }

    val grouped = notifications.groupBy { it.group }
    val hasUnread = notifications.any { it.isUnread }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.sm,
            bottom = spacing.xxl
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.sm)
    ) {
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (hasUnread) {
                    TextButton(onClick = onMarkAllRead) {
                        Text("Mark all as read", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        NotificationGroup.entries.forEach { group ->
            val rows = grouped[group].orEmpty()
            if (rows.isEmpty()) return@forEach

            item(key = "group-${group.name}") {
                Text(
                    text = group.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = spacing.sm, bottom = spacing.xs)
                )
            }
            items(rows, key = { it.id }) { item -> NotificationRow(item = item) }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem) {
    val spacing = LocalSpacing.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (item.isUnread) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
                MaterialTheme.shapes.medium
            )
            .padding(spacing.md),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                getIconForNotification(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.let {
                        if (item.isUnread) it.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold) else it
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(spacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.isUnread) {
                        Spacer(modifier = Modifier.width(spacing.xs))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
