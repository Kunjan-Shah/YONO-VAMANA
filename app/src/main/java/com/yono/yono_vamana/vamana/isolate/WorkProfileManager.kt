package com.yono.yono_vamana.vamana.isolate

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.CrossProfileApps
import android.os.Process
import android.os.UserManager
import com.yono.yono_vamana.MainActivity

/**
 * Manages the lifecycle of the VAMANA-Isolate managed work profile — the
 * isolated execution environment backing the VAMANA-Isolate layer.
 *
 * This app requests [DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE]
 * naming itself as the admin, so Android automatically clones YONO-VAMANA
 * into the newly created work profile and grants it Profile Owner status
 * there — the same mechanism SecureFolderPlusPlus's ProfileManager uses.
 */
class WorkProfileManager(private val context: Context) {

    private val dpm: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val userManager: UserManager =
        context.getSystemService(Context.USER_SERVICE) as UserManager

    private val prefs =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val adminComponent =
        ComponentName(context, WorkProfileAdminReceiver::class.java)

    /** True once the isolated work profile exists and is enabled. */
    fun isProfileReady(): Boolean {
        val hasProfile = hasExistingManagedProfile()
        if (hasProfile && !prefs.getBoolean(KEY_PROFILE_CREATED, false)) {
            prefs.edit().putBoolean(KEY_PROFILE_CREATED, true).apply()
        }
        return hasProfile
    }

    private fun wasProfileEverCreated(): Boolean =
        prefs.getBoolean(KEY_PROFILE_CREATED, false)

    /** Returns why provisioning can't start, or null if it's safe to proceed. */
    fun checkProvisioningBlocked(): ProvisioningBlockReason? {
        if (!canSystemProvisionManagedProfile()) return ProvisioningBlockReason.SYSTEM_UNSUPPORTED
        if (hasExistingManagedProfile()) {
            return if (wasProfileEverCreated()) {
                ProvisioningBlockReason.ALREADY_PROVISIONED
            } else {
                ProvisioningBlockReason.EXISTING_PROFILE_FROM_ANOTHER_DPC
            }
        }
        return null
    }

    private fun canSystemProvisionManagedProfile(): Boolean = try {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        context.packageManager.resolveActivity(intent, 0) != null
    } catch (e: Exception) {
        false
    }

    private fun hasExistingManagedProfile(): Boolean = try {
        val profiles = userManager.userProfiles
        if (profiles.size > 1) {
            true
        } else {
            !dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        }
    } catch (e: Exception) {
        prefs.getBoolean(KEY_PROFILE_CREATED, false)
    }

    /** Builds the system provisioning Intent, or null if provisioning is blocked. */
    fun buildProvisioningIntent(): Intent? {
        if (checkProvisioningBlocked() != null) return null
        return Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE).apply {
            putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, adminComponent)
            // Skip the "add account" step — the isolated profile doesn't need one.
            putExtra("android.app.extra.SKIP_USER_SETUP", true)
        }
    }

    /** Call after the provisioning Intent returns RESULT_OK. */
    fun markProfileCreated() {
        prefs.edit().putBoolean(KEY_PROFILE_CREATED, true).apply()
    }

    /**
     * Opens YONO-VAMANA's own work-profile instance — equivalent to tapping
     * its cloned icon in the launcher's "Work" tab, but reachable even on
     * launchers that don't surface that tab.
     */
    fun openWorkProfileInstance(activity: Activity): Boolean {
        return try {
            val workHandle = userManager.userProfiles.firstOrNull { it != Process.myUserHandle() }
                ?: return false
            val crossProfileApps = context.getSystemService(CrossProfileApps::class.java)
            crossProfileApps.startMainActivity(ComponentName(activity, MainActivity::class.java), workHandle)
            true
        } catch (e: Exception) {
            false
        }
    }

    enum class ProvisioningBlockReason(val userMessage: String) {
        SYSTEM_UNSUPPORTED(
            "This device does not support isolated work profiles."
        ),
        EXISTING_PROFILE_FROM_ANOTHER_DPC(
            "This device already has a work profile managed by another app. " +
                "Only one work profile is allowed per device."
        ),
        ALREADY_PROVISIONED(
            "The VAMANA-Isolate work profile is already set up on this device."
        )
    }

    private companion object {
        const val PREFS_NAME = "vamana_isolate_work_profile"
        const val KEY_PROFILE_CREATED = "profile_created"
    }
}
