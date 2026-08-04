package com.xsc.oneapp.feature.login.ui.state

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val forgotEmail: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null
)
