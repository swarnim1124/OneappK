package com.xsc.oneapp.feature.login.ui.effect

sealed class LoginEffect {
    object NavigateToDashboard : LoginEffect()
    data class ShowToast(val message: String, val isError: Boolean = false) : LoginEffect()
}
