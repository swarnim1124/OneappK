package com.xsc.oneapp.feature.profile.domain.model

data class MfaMethod(
    val id: String,
    val mfaTypeId: String,
    val isPrimary: Boolean,
    val isActive: Boolean
)

data class MfaEnrollment(
    val mfaType: String,
    val provisioningUri: String,
    val backupCodes: List<String>,
    val enrollmentChallengeId: String
)

data class BackupCodesResult(
    val backupCodes: List<String>
)
