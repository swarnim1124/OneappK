package com.xsc.oneapp.feature.dashboard.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.xsc.sdk.commonui.record.RecordScaffold
import com.xsc.sdk.commonui.record.StatusPill
import com.xsc.sdk.theme.LocalSpacing
import java.util.Locale

/**
 * Placeholder for a module that has no screen yet - reached through the dashboard's
 * catch-all route.
 *
 * Restyled only; the route argument, the icon mapping and the back callback are
 * unchanged. Worth flagging separately: this is still reachable in release builds, so
 * a user who taps Library or Placements lands here. That is a product decision rather
 * than a styling one, so it was left as-is.
 */
@Composable
fun DummyModuleScreen(
    moduleName: String,
    onBack: () -> Unit
) {
    val spacing = LocalSpacing.current

    val formattedName = moduleName.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }

    val icon = when (moduleName.lowercase()) {
        "academics", "curriculum" -> Icons.Default.School
        "attendance" -> Icons.Default.EventAvailable
        "library" -> Icons.Default.LocalLibrary
        "exams" -> Icons.Default.Description
        "fees" -> Icons.Default.CurrencyRupee
        "placements" -> Icons.Default.Work
        "timetable" -> Icons.Default.Schedule
        else -> Icons.Default.Dashboard
    }

    RecordScaffold(title = formattedName, onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            Text(
                text = formattedName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacing.md))

            Text(
                text = "This module isn't available yet. It will appear here once it's connected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(modifier = Modifier.height(spacing.xl))

            StatusPill("Coming soon", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
