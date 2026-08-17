package com.xsc.oneapp.feature.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MfaMethodDTO(
    @SerializedName("id") val id: String?,
    @SerializedName("mfa_type_id") val mfaTypeId: String?,
    @SerializedName("is_primary") val isPrimary: String?,
    @SerializedName("is_active") val isActive: String?
)

data class MfaEnrollmentDTO(
    @SerializedName("mfaType") val mfaType: String?,
    @SerializedName("provisioningUri") val provisioningUri: String?,
    @SerializedName("backupCodes") val backupCodes: List<String>?,
    @SerializedName("enrollmentChallengeId") val enrollmentChallengeId: String?
)

data class BackupCodesResultDTO(
    @SerializedName("backupCodes") val backupCodes: List<String>?
)
