package com.xsc.oneapp.feature.login.ui.state

data class VerifyOtpState(
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
