package com.xsc.oneapp.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserPreferenceDTO(
    @SerializedName("language") val language: String?,
    @SerializedName("theme") val theme: String?,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("defaultLandingModule") val defaultLandingModule: String?
)
