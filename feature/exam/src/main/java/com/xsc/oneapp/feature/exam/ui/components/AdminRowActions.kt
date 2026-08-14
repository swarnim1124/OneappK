package com.xsc.oneapp.feature.exam.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Shared edit/delete row-action pair for every admin CRUD list this session builds
 * (exam centres, seating, invigilation, question papers, external examiners, ...) -
 * genuine duplication across ~10 near-identical admin screens, not a speculative
 * abstraction. Renders nothing when both actions are gated off. */
@Composable
fun AdminRowActions(
    canUpdate: Boolean,
    canDelete: Boolean,
    onUpdate: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!canUpdate && !canDelete) return
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        if (canUpdate && onUpdate != null) {
            IconButton(onClick = onUpdate) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (canDelete && onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
