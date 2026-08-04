package com.xsc.oneapp.feature.login.ui.state

data class LoginState(
    val isLoading: Boolean = false,
    val emailInput: String = "",
    val passwordInput: String = "",
    val isPasswordVisible: Boolean = false
)
