package com.digi.digi_vamana.data

import android.content.Context

/** Tracks whether the one-time VAMANA Security Setup flow has been completed. */
class OnboardingPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isSetupComplete: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETE, value).apply()

    private companion object {
        const val PREFS_NAME = "vamana_onboarding"
        const val KEY_SETUP_COMPLETE = "setup_complete"
    }
}
