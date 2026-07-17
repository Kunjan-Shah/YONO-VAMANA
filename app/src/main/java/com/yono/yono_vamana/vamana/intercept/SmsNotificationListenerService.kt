package com.yono.yono_vamana.vamana.intercept

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Telephony
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yono.yono_vamana.MainActivity
import com.yono.yono_vamana.R
import com.yono.yono_vamana.data.InterceptPreferences

/**
 * Watches notifications posted by the device's default SMS app and, while
 * VAMANA-Intercept is active, raises a persistent warning that the message
 * may be malicious. Bound/unbound by the system in response to
 * [requestActivate] / [requestDeactivate] once the user has granted
 * notification-listener access from Settings.
 */
class SmsNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(LOG_TAG, "VAMANA-Intercept listener connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i(LOG_TAG, "VAMANA-Intercept listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (!InterceptPreferences(this).isActive) return
        if (sbn.packageName == packageName) return

        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
        if (defaultSmsPackage == null || sbn.packageName != defaultSmsPackage) return

        showMaliciousSmsAlert()
    }

    private fun showMaliciousSmsAlert() {
        ensureNotificationChannel()

        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(MainActivity.EXTRA_OPEN_SMS_INTERCEPTED, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_vamana_alert)
            .setContentTitle("You might have got a malicious SMS")
            .setContentText("Stay protected with VAMANA")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
        }
    }

    private fun ensureNotificationChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "VAMANA-Intercept alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Persistent warnings when VAMANA-Intercept flags a suspicious SMS."
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val LOG_TAG = "VamanaIntercept"
        private const val CHANNEL_ID = "vamana_intercept_alerts"
        private const val NOTIFICATION_ID = 1001

        private fun componentName(context: Context) =
            ComponentName(context, SmsNotificationListenerService::class.java)

        /** Asks the system to (re)bind the listener now that access has been granted. */
        fun requestActivate(context: Context) {
            NotificationListenerService.requestRebind(componentName(context))
        }

        /** Asks the system to unbind the listener and clears any alert it raised. */
        fun requestDeactivate(context: Context) {
            NotificationListenerService.requestUnbind(componentName(context))
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }
}
