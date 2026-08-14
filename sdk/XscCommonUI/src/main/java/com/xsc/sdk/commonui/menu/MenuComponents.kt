package com.xsc.sdk.commonui.menu

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.xsc.sdk.theme.LocalSpacing

/**
 * A clickable icon+label list row on a transparent surface - a settings/menu item,
 * not a card. Dashboard's own profile-menu rows and its sidebar drawer rows were two
 * independent copies of this same row shape (56dp vs 48dp tall, lg vs md icon-to-label
 * gap, one with a trailing chevron); this converges them on the taller, chevron-capable
 * version, since that's the more complete of the two and matches this app's other
 * 56dp+ list-row conventions elsewhere.
 *
 * Deliberately not merged with Dashboard's `QuickActionRow` (a bordered, elevated
 * `Card`) or Profile's dashlet row (icon-in-tinted-circle, title+description, a press
 * animation) - those are a different visual weight for a different purpose, not a
 * duplicate of this plain menu row.
 */
@Composable
fun MenuRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    val spacing = LocalSpacing.current

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
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
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(spacing.lg))
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (showChevron) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
