package com.xsc.oneapp.feature.login.ui.effect

sealed class VerifyOtpEffect {
    data class NavigateToResetPassword(val resetToken: String) : VerifyOtpEffect()
}
