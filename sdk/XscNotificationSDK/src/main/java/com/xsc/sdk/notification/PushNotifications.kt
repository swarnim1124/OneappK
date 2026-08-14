package com.xsc.sdk.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Channel setup shared by this module's notifications.
 *
 * Separate from [OneAppFirebaseMessagingService]: creating a channel is plain Android
 * ([NotificationManager]), no Firebase dependency at all, and must happen before any
 * notification can post - so it stays callable (and useful) independently of whether
 * `google-services.json` exists yet.
 */
object PushNotifications {
    const val CHANNEL_ID = "oneapp_default"

    /** Safe to call on every app start - creating an already-existing channel with
     * identical parameters is a documented no-op, not a reset. */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "OneApp notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}
