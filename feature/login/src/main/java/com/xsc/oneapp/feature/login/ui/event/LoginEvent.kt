package com.xsc.oneapp.feature.login.ui.event

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object SubmitLogin : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()
    
    // MFA Events
    data class OtpChanged(val otp: String) : LoginEvent()
    object SubmitMfa : LoginEvent()
    object ToggleMfaMode : LoginEvent()
    object BackToLogin : LoginEvent()
}
