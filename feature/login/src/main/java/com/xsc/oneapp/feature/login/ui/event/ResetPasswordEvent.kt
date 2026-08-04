package com.xsc.oneapp.feature.login.ui.event

sealed class ResetPasswordEvent {
    data class NewPasswordChanged(val password: String) : ResetPasswordEvent()
    data class ConfirmPasswordChanged(val password: String) : ResetPasswordEvent()
    object Submit : ResetPasswordEvent()
}
