package com.xsc.oneapp.feature.dashboard.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Every field is nullable because this is untrusted wire data: the `accessibleModules`
 * response is hand-maintained per environment and routinely omits fields (or sends a
 * status string the app doesn't know yet). Gson does not honour Kotlin nullability, so
 * declaring these non-null only moved the failure to an unrelated call site later.
 * [com.xsc.oneapp.feature.dashboard.data.mapper.toDomain] applies the defaults.
 */
data class ModuleItemDto(
    @SerializedName("id") val id: String?,
    @SerializedName("displayName") val displayName: String?,
    @SerializedName("icon") val icon: String?,
    @SerializedName("route") val route: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("accentColor") val accentColor: String?
)
