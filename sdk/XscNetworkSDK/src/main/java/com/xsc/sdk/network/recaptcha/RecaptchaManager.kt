package com.xsc.sdk.network.recaptcha

import com.xsc.sdk.network.di.RecaptchaSiteKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Produces the reCAPTCHA token LoginViewModel attaches to the login payload's
 * `captcha` field. Per AAA_API_CONTRACT.md, `captcha` is only required when the
 * backend's `XSC_CAPTCHA_ENABLED` config is on - when it's off, the field is
 * ignored, so an empty token is a safe default rather than a hard blocker.
 *
 * [siteKey] comes from BuildConfig (see sdk/XscNetworkSDK/build.gradle.kts,
 * `-PrecaptchaSiteKey=...`) - empty by default. This intentionally does not call
 * the `com.google.android.gms.recaptcha.Recaptcha` client API directly even once a
 * site key is configured: the request/response GMS Task surface should be
 * confirmed against the exact play-services-recaptcha version pinned in the
 * version catalog before enabling it. Until then, calls resolve to an empty token
 * so login keeps working end-to-end against environments where captcha is
 * disabled; if the backend does require it, the user gets a clear "Invalid CAPTCHA
 * token" error from the server instead of a client crash.
 */
@Singleton
class RecaptchaManager @Inject constructor(
    @RecaptchaSiteKey private val siteKey: String
) {

    suspend fun execute(action: String): String {
        if (siteKey.isBlank()) {
            return ""
        }
        // TODO: wire com.google.android.gms.recaptcha.Recaptcha.getClient(...)/execute(...)
        // now that a site key is configured - see play-services-recaptcha
        // dependency in sdk/XscNetworkSDK/build.gradle.kts.
        return ""
    }
}
