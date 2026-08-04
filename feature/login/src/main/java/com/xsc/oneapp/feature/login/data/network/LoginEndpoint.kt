package com.xsc.oneapp.feature.login.data.network

object LoginEndpoint {
    const val MODULE = "m_AAA"
    const val SUBMODULE = "sm_auth"

    /** Confirmed against the real m_AAA_sm_auth.py source (2026-07-31): the
     * passwordReset flow's 3 steps all route through subMod "auth", not "sm_auth" -
     * a single action ("passwordReset") with the step selected by actionType
     * (add/view/update). Login's own "session" action hasn't been confirmed against
     * that source, so it stays on SUBMODULE until it is. */
    const val SUBMODULE_AUTH = "auth"

    object Actions {
        const val SESSION = "session"
        const val PASSWORD_RESET = "passwordReset"
    }

    object ActionTypes {
        const val ADD = "add"
        const val VIEW = "view"
        const val UPDATE = "update"
    }
}
