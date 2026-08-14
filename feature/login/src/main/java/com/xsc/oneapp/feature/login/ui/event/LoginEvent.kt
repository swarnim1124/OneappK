package com.xsc.oneapp.feature.login.ui.event

sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object SubmitLogin : LoginEvent()
    object TogglePasswordVisibility : LoginEvent()

    /** Fired when the user navigates away via a link on this screen (e.g. "Forgot
     * password?") rather than by submitting - clears any lingering error so it
     * doesn't reappear stale if they come back to this same screen instance. */
    object NavigatingAway : LoginEvent()
}
