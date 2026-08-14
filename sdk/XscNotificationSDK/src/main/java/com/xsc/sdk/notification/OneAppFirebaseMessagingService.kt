package com.xsc.sdk.notification

import android.Manifest
import android.app.PendingIntent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM entry point. Registered in this module's AndroidManifest.xml so `:app` never
 * has to know FCM exists - same "feature module owns its own manifest wiring"
 * precedent as `:sdk:XscNetworkSDK` declaring `INTERNET`.
 *
 * Client-only for now: there is no backend device-token registration endpoint yet
 * (confirmed 2026-08-14, part of the Firebase completion plan), so [onNewToken] logs
 * the token instead of sending it anywhere. The moment a real contract exists, that's
 * the only method that needs a body change - everything else here (channel creation,
 * foreground display) is already real.
 */
class OneAppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM registration token refreshed (not yet sent anywhere - no backend endpoint exists)")
    }

    /**
     * FCM only auto-displays a *notification*-payload message when the app is
     * backgrounded/killed; a foregrounded app always receives it here regardless of
     * payload shape, so this is the only place a notification reliably shows for
     * both cases.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val notification = message.notification ?: return
        showNotification(notification.title, notification.body)
    }

    private fun showNotification(title: String?, body: String?) {
        // POST_NOTIFICATIONS (API 33+) is a runtime permission the user may have
        // denied - the manifest declaration alone doesn't mean it was granted, and
        // posting without checking risks a SecurityException on some OEM builds.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, PushNotifications.CHANNEL_ID)
            // Placeholder system glyph - this module has no launcher-icon resources of
            // its own (those live in :app) and a status-bar icon must be a solid
            // white/alpha silhouette per Android's notification design guidelines, not
            // an arbitrary drawable. Replace with a real monochrome asset in this
            // module once one exists.
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .apply { contentIntent?.let { setContentIntent(it) } }
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val TAG = "OneAppFCM"
        const val NOTIFICATION_ID = 1001
    }
}
