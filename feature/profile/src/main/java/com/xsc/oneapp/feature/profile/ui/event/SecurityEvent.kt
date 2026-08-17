package com.xsc.oneapp.feature.profile.ui.event

import com.xsc.oneapp.feature.profile.domain.model.MfaMethod

sealed class SecurityEvent {
    object LoadMfaStatus : SecurityEvent()
    object InitiateEnrollment : SecurityEvent()
    data class OtpChanged(val otp: String) : SecurityEvent()
    object FinalizeEnrollment : SecurityEvent()
    object CloseEnrollmentModal : SecurityEvent()
    data class RequestDisableMfa(val method: MfaMethod) : SecurityEvent()
    object ConfirmDisableMfa : SecurityEvent()
    object CancelDisableMfa : SecurityEvent()
    data class RegenerateBackupCodes(val methodId: String) : SecurityEvent()
    object CloseBackupCodesModal : SecurityEvent()
    object ClearError : SecurityEvent()
}
