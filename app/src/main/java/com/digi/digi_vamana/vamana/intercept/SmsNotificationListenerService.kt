package com.digi.digi_vamana.vamana.intercept

import android.app.Notification
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
import com.digi.digi_vamana.MainActivity
import com.digi.digi_vamana.R
import com.digi.digi_vamana.data.InterceptPreferences
import com.digi.digi_vamana.vamana.intelligence.VamanaActivityLog

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
        VamanaActivityLog.log(VamanaActivityLog.Category.INTERCEPT, "SMS notification listener connected — actively monitoring.")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.i(LOG_TAG, "VAMANA-Intercept listener disconnected")
        VamanaActivityLog.log(VamanaActivityLog.Category.INTERCEPT, "SMS notification listener disconnected.")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (!InterceptPreferences(this).isActive) return
        if (sbn.packageName == packageName) return

        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
        if (defaultSmsPackage == null || sbn.packageName != defaultSmsPackage) return

        VamanaActivityLog.log(VamanaActivityLog.Category.INTERCEPT, "New SMS notification received from the default messaging app.")

        if (!containsSuspiciousLink(sbn)) {
            VamanaActivityLog.log(VamanaActivityLog.Category.INTERCEPT, "Message text carries no suspicious link — no warning raised.")
            return
        }

        showMaliciousSmsAlert()
    }

    /**
     * Dummy malicious-SMS classifier: real SMS-scam detection is out of scope
     * for this demo, so this stands in for it — flag the message purely on
     * whether its text contains a raw "https://" link.
     */
    private fun containsSuspiciousLink(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        return text.contains("https://") || bigText.contains("https://")
    }

    private fun showMaliciousSmsAlert() {
        ensureNotificationChannel()
        VamanaActivityLog.log(VamanaActivityLog.Category.INTERCEPT, "Malicious SMS warning raised — persistent alert shown to the user.")

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
            .setSmallIcon(R.drawable.vamana_mascot)
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
