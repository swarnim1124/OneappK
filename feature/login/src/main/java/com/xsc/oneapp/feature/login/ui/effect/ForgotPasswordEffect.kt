package com.xsc.oneapp.feature.login.ui.effect

sealed class ForgotPasswordEffect {
    data class NavigateToVerifyOtp(val resetToken: String) : ForgotPasswordEffect()
}
