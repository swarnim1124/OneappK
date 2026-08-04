package com.xsc.oneapp.feature.login.ui.event

sealed class VerifyOtpEvent {
    data class OtpChanged(val code: String) : VerifyOtpEvent()
    object Submit : VerifyOtpEvent()
}
