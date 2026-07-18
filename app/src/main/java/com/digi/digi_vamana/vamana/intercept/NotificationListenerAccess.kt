package com.digi.digi_vamana.vamana.intercept

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * Notification-listener access can't be granted through a runtime permission
 * dialog — the user has to enable it manually from system Settings. These
 * helpers wrap that special-access check/launch for [SmsNotificationListenerService].
 */
object NotificationListenerAccess {

    fun isEnabled(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)

    fun settingsIntent(): Intent =
        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
}
