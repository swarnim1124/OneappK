package com.xsc.oneapp.feature.profile.ui.state

import com.xsc.oneapp.feature.profile.domain.model.MfaEnrollment
import com.xsc.oneapp.feature.profile.domain.model.MfaMethod

data class SecurityState(
    val isLoading: Boolean = false,
    val mfaMethods: List<MfaMethod> = emptyList(),
    val enrollmentData: MfaEnrollment? = null,
    val showEnrollmentModal: Boolean = false,
    val otpInput: String = "",
    val showBackupCodesModal: Boolean = false,
    val backupCodesToDisplay: List<String> = emptyList(),
    val showDisableConfirmation: Boolean = false,
    val methodToDisable: MfaMethod? = null,
    val error: String? = null
)
