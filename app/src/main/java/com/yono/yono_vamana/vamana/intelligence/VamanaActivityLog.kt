package com.yono.yono_vamana.vamana.intelligence

import android.content.Context
import android.util.Log
import com.yono.yono_vamana.data.IntelligencePreferences
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

/**
 * Human-readable, cross-layer activity log — VAMANA-Intelligence fuses
 * signals from every other layer, so its toggle doubles as the on/off
 * switch for this audit trail. While active, every call below appends one
 * plain-language line to a daily log file in app-internal storage
 * (files/vamana_logs/vamana_log_yyyy-MM-dd.txt); while inactive, calls are
 * no-ops. Writes happen off the caller's thread so logging never blocks a
 * binder callback, the main thread, or a coroutine.
 */
object VamanaActivityLog {

    enum class Category(val label: String) {
        LAYER("LAYER"),
        INTERCEPT("INTERCEPT"),
        ISOLATE("ISOLATE"),
        VERIFY("VERIFY"),
        PERMISSION("PERMISSION"),
        TRANSACTION("TRANSACTION")
    }

    private const val LOG_TAG = "VamanaActivityLog"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    private val writeExecutor = Executors.newSingleThreadExecutor()

    @Volatile
    private var appContext: Context? = null

    /** Call once from Application.onCreate so logging is ready before any Activity or Service runs. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun log(category: Category, message: String) {
        val context = appContext ?: return
        if (!IntelligencePreferences(context).isActive) return

        val now = Date()
        val line = "[${timeFormat.format(now)}] [${category.label}] $message"
        writeExecutor.execute {
            try {
                val dir = File(context.filesDir, "vamana_logs").apply { mkdirs() }
                val file = File(dir, "vamana_log_${dateFormat.format(now)}.txt")
                file.appendText(line + System.lineSeparator())
            } catch (e: Exception) {
                Log.w(LOG_TAG, "Failed to write activity log line", e)
            }
        }
    }
}
