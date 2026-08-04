package com.xsc.oneapp.feature.login.ui.state

data class ResetPasswordState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
