package com.xsc.oneapp.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MedicalDetailDTO(
    @SerializedName("userId") val userId: Any?,
    @SerializedName("bloodGroupId") val bloodGroupId: Int?,
    @SerializedName("allergies") val allergies: List<String>?,
    @SerializedName("chronicConditions") val chronicConditions: List<String>?,
    @SerializedName("medications") val medications: List<String>?,
    @SerializedName("height") val height: Double?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("disabilityTypeId") val disabilityTypeId: Int?,
    @SerializedName("doctorName") val doctorName: String?,
    @SerializedName("doctorContact") val doctorContact: String?,
    @SerializedName("insurancePolicyNo") val insurancePolicyNo: String?
)
