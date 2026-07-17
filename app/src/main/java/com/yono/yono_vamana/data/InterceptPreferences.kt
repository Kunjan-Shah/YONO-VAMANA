package com.yono.yono_vamana.data

import android.content.Context

/** Tracks whether the user has toggled VAMANA-Intercept on for this device. */
class InterceptPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isActive: Boolean
        get() = prefs.getBoolean(KEY_ACTIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_ACTIVE, value).apply()

    private companion object {
        const val PREFS_NAME = "vamana_intercept_prefs"
        const val KEY_ACTIVE = "is_active"
    }
}
