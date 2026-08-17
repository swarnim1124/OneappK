@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.xsc.oneapp.feature.profile.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.xsc.oneapp.feature.profile.navigation.ProfileDestinations
import com.xsc.oneapp.feature.profile.ui.components.ProfileContentColumn
import com.xsc.sdk.theme.LocalSpacing
import com.xsc.sdk.theme.OneAppMotion

data class ProfileDashlet(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
)

/**
 * Profile hub.
 *
 * The six destinations and the `onNavigateTo` contract are unchanged - this screen still
 * owns no state and performs no data access. Visual changes only: the single 6-row
 * bordered block becomes three labelled groups, plus an entrance stagger and press
 * feedback per row.
 *
 * The signature is deliberately not extended with a back callback; adding one would mean
 * editing ProfileNavigation, which is navigation logic rather than presentation.
 */
@Composable
fun ProfileDashboardScreen(
    onNavigateTo: (String) -> Unit
) {
    val spacing = LocalSpacing.current

    val identity = listOf(
        ProfileDashlet(
            title = "Personal details",
            description = "Identity and contact information",
            icon = Icons.Default.Person,
            route = ProfileDestinations.PERSONAL_DETAIL
        ),
        ProfileDashlet(
            title = "Academic identifiers",
            description = "Enrollment, employee codes and IDs",
            icon = Icons.Default.Info,
            route = ProfileDestinations.ACADEMIC_DETAIL
        )
    )

    val people = listOf(
        ProfileDashlet(
            title = "Family details",
            description = "Family and guardian records",
            icon = Icons.Default.AccountBox,
            route = ProfileDestinations.FAMILY_DETAIL
        ),
        ProfileDashlet(
            title = "Emergency contacts",
            description = "Priority-ordered emergency contacts",
            icon = Icons.Default.Call,
            route = ProfileDestinations.EMERGENCY_CONTACT
        ),
        ProfileDashlet(
            title = "Medical details",
            description = "Health records and conditions",
            icon = Icons.Default.Favorite,
            route = ProfileDestinations.MEDICAL_DETAIL
        )
    )

    val preferences = listOf(
        ProfileDashlet(
            title = "Settings",
            description = "Preferences and app appearance",
            icon = Icons.Default.Settings,
            route = ProfileDestinations.USER_PREFERENCE
        )
    )

    val security = listOf(
        ProfileDashlet(
            title = "Security",
            description = "Two-factor authentication",
            icon = Icons.Default.Security,
            route = ProfileDestinations.SECURITY
        )
    )

    var visible by remember { mutableStateOf(value = false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // This destination has no Scaffold of its own, so it must consume the
                // system-bar insets itself - MainActivity draws edge to edge.
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.screenHorizontal, vertical = spacing.lg)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(spacing.xl))

            ProfileContentColumn {
                listOf(
                    "Identity" to identity,
                    "People" to people,
                    "Preferences" to preferences,
                    "Security" to security
                ).forEachIndexed { index, group ->
                    AnimatedVisibility(
                        visible = visible,
                        enter = OneAppMotion.contentEnter(indexOffset = index)
                    ) {
                        Column {
                            Text(
                                text = group.first,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = spacing.sm)
                            )
                            DashletGroup(items = group.second, onNavigateTo = onNavigateTo)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(spacing.xxl))
        }
    }
}

@Composable
private fun DashletGroup(items: List<ProfileDashlet>, onNavigateTo: (String) -> Unit) {
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
        items.forEachIndexed { index, dashlet ->
            ProfileDashletRow(dashlet = dashlet) {
                onNavigateTo(dashlet.route)
            }
            if (index != items.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 64.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileDashletRow(
    dashlet: ProfileDashlet,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.99f else 1f,
        animationSpec = OneAppMotion.pressSpring(),
        label = "dashletPressScale"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = spacing.lg, vertical = spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = dashlet.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(spacing.lg))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dashlet.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dashlet.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(spacing.sm))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
