package com.xsc.sdk.network.di

import javax.inject.Qualifier

/** Qualifies the reCAPTCHA site key string injected into [com.xsc.sdk.network.recaptcha.RecaptchaManager]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RecaptchaSiteKey
