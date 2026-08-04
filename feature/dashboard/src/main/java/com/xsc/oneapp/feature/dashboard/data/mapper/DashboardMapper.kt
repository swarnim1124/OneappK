package com.xsc.oneapp.feature.dashboard.data.mapper

import com.xsc.oneapp.feature.dashboard.data.remote.dto.ModuleItemDto
import com.xsc.oneapp.feature.dashboard.domain.model.ModuleItem

/**
 * Returns null for a row the app genuinely can't render (no id) rather than throwing -
 * one malformed entry in the `accessibleModules` array must not take out the whole
 * dashboard. Every other field falls back to a usable default, so a partially
 * populated backend row still produces a tappable module tile instead of vanishing.
 */
fun ModuleItemDto.toDomain(): ModuleItem? {
    val moduleId = id?.takeIf { it.isNotBlank() } ?: return null
    return ModuleItem(
        id = moduleId,
        displayName = displayName?.takeIf { it.isNotBlank() }
            ?: moduleId.replaceFirstChar { it.uppercase() },
        icon = icon.orEmpty(),
        route = route?.takeIf { it.isNotBlank() } ?: "/$moduleId",
        status = ModuleItem.ModuleStatus.fromWire(status),
        accentColorString = accentColor.orEmpty()
    )
}
