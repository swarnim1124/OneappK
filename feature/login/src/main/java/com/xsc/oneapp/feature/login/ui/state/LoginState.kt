package com.xsc.oneapp.feature.login.ui.state

data class LoginState(
    val isLoading: Boolean = false,
    val emailInput: String = "",
    val passwordInput: String = "",
    val isPasswordVisible: Boolean = false,
    /** Persists until the next submit attempt or navigation away - never cleared on
     * a keystroke (see LoginViewModel.onEvent). */
    val error: LoginError? = null
)
