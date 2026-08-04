package com.xsc.oneapp.feature.login.ui.event

sealed class ForgotPasswordEvent {
    data class EmailChanged(val email: String) : ForgotPasswordEvent()
    object SendOTP : ForgotPasswordEvent()
    object ClearMessages : ForgotPasswordEvent()
}
