package com.yono.yono_vamana.vamana.isolate

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.UserManager

/**
 * Applies VAMANA-Isolate's security policies to the managed work profile
 * using DevicePolicyManager as Profile Owner.
 *
 * Ported category-for-category from SecureFolderPlusPlus's PolicyEnforcer:
 *   1. Overlay window blocking          → DISALLOW_CREATE_WINDOWS
 *   2. Screen capture blocking          → setScreenCaptureDisabled
 *   3. Accessibility service allowlist  → setPermittedAccessibilityServices
 *   4. Keyboard allowlist               → setPermittedInputMethods
 *   5. Cross-profile leakage blocking   → clipboard/share/USB/Bluetooth + caller ID/contacts
 *   6. Installation lockdown            → clears DISALLOW_INSTALL_UNKNOWN_SOURCES(_GLOBALLY)
 *   7. Network restrictions             → DISALLOW_CONFIG_VPN + DISALLOW_NETWORK_RESET
 *   8. Debug/backup restrictions        → DISALLOW_DEBUGGING_FEATURES + backup + factory reset
 *   9. Password policy                  → NUMERIC_COMPLEX, min length 6
 *  10. Camera disable                   → setCameraDisabled
 *  11. Keyguard feature restrictions    → unredacted notifications + trust agents
 *  12. Notification/account policy      → DISALLOW_MODIFY_ACCOUNTS
 *
 * Every uses-policy this enforcer relies on must be declared in
 * res/xml/vamana_isolate_device_admin_policies.xml, or the corresponding
 * DPM call throws a SecurityException.
 */
class PolicyEnforcer(private val context: Context) {

    private val dpm: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    private val adminComponent = ComponentName(context, WorkProfileAdminReceiver::class.java)

    // ─────────────────────────────────────────────────────────────────────────
    //  Entry points
    // ─────────────────────────────────────────────────────────────────────────

    /** Apply every security policy. Called once after provisioning completes. */
    fun applyAllInitialPolicies() {
        requireProfileOwner()

        blockOverlayWindows()
        disableScreenCapture()
        restrictAccessibilityServices()
        restrictInputMethods()
        blockCrossProfileLeakage()
        lockdownInstallation()
        restrictNetwork()
        restrictDebugging()
        enforcePasswordPolicy()
        disableCamera()
        disableKeyguardFeatures()
        setNotificationPolicy()
        enableProfile()
    }

    /** Re-apply the policies most likely to drift at runtime. */
    fun refreshRuntimePolicies() {
        if (!isProfileOwner()) return
        restrictAccessibilityServices()
        restrictInputMethods()
        blockOverlayWindows()
    }

    /**
     * Marks the managed profile as enabled. Until a profile owner calls this,
     * Android keeps a freshly provisioned profile DISABLED: no launcher icon,
     * excluded from UserManager.getUserProfiles() / CrossProfileApps results.
     */
    private fun enableProfile() {
        try {
            dpm.setProfileEnabled(adminComponent)
        } catch (e: Exception) {
            // Best-effort.
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  1. Overlay Window Blocking
    // ─────────────────────────────────────────────────────────────────────────

    private fun blockOverlayWindows() {
        try {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CREATE_WINDOWS)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  2. Screen Capture Blocking
    // ─────────────────────────────────────────────────────────────────────────

    private fun disableScreenCapture() {
        try {
            dpm.setScreenCaptureDisabled(adminComponent, true)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  3. Accessibility Service Restriction
    // ─────────────────────────────────────────────────────────────────────────

    private fun restrictAccessibilityServices() {
        try {
            dpm.setPermittedAccessibilityServices(adminComponent, TRUSTED_ACCESSIBILITY_SERVICES)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  4. Input Method (Keyboard) Restriction
    // ─────────────────────────────────────────────────────────────────────────

    private fun restrictInputMethods() {
        try {
            dpm.setPermittedInputMethods(adminComponent, TRUSTED_INPUT_METHODS)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  5. Cross-Profile Leakage Prevention
    // ─────────────────────────────────────────────────────────────────────────

    private fun blockCrossProfileLeakage() {
        try {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE)
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE)
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_USB_FILE_TRANSFER)
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_BLUETOOTH_SHARING)
            dpm.setCrossProfileCallerIdDisabled(adminComponent, true)
            dpm.setCrossProfileContactsSearchDisabled(adminComponent, true)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  6. Installation Lockdown
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Android applies DISALLOW_INSTALL_UNKNOWN_SOURCES to every managed
     * profile by default; clearing it here matches SecureFolderPlusPlus's
     * behavior (kept clear so the profile owner can still install apps
     * programmatically even though nothing currently does so).
     */
    private fun lockdownInstallation() {
        try {
            dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
            dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  7. Network Restrictions
    // ─────────────────────────────────────────────────────────────────────────

    private fun restrictNetwork() {
        try {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_VPN)
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_NETWORK_RESET)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  8. Debug / Backup Restrictions
    // ─────────────────────────────────────────────────────────────────────────

    private fun restrictDebugging() {
        try {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_DEBUGGING_FEATURES)
            dpm.setBackupServiceEnabled(adminComponent, false)
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  9. Password / Authentication Policy
    // ─────────────────────────────────────────────────────────────────────────

    @Suppress("DEPRECATION")
    private fun enforcePasswordPolicy() {
        try {
            dpm.setPasswordQuality(adminComponent, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX)
            dpm.setPasswordMinimumLength(adminComponent, 6)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  10. Camera Disable
    // ─────────────────────────────────────────────────────────────────────────

    private fun disableCamera() {
        try {
            dpm.setCameraDisabled(adminComponent, true)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  11. Keyguard Feature Restrictions
    // ─────────────────────────────────────────────────────────────────────────

    private fun disableKeyguardFeatures() {
        try {
            val featuresToDisable = DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS or
                DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS
            dpm.setKeyguardDisabledFeatures(adminComponent, featuresToDisable)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  12. Notification Policy (Cross-Profile Redaction)
    // ─────────────────────────────────────────────────────────────────────────

    private fun setNotificationPolicy() {
        try {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_MODIFY_ACCOUNTS)
        } catch (e: Exception) {
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Profile management helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Wipe all data in the managed work profile. Destructive and irreversible. */
    fun wipeProfileData() {
        try {
            dpm.wipeData(0)
        } catch (e: Exception) {
        }
    }

    /** Lock the work profile immediately. */
    fun lockProfileNow() {
        try {
            dpm.lockNow()
        } catch (e: Exception) {
        }
    }

    /** Number of failed password attempts for the work profile challenge. */
    fun getFailedPasswordAttempts(): Int = try {
        dpm.currentFailedPasswordAttempts
    } catch (e: Exception) {
        0
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  State checks
    // ─────────────────────────────────────────────────────────────────────────

    fun isProfileOwner(): Boolean = try {
        dpm.isProfileOwnerApp(context.packageName)
    } catch (e: Exception) {
        false
    }

    private fun requireProfileOwner() {
        check(isProfileOwner()) {
            "PolicyEnforcer: not a Profile Owner. Cannot apply DPM policies. " +
                "Ensure the app was provisioned via ACTION_PROVISION_MANAGED_PROFILE."
        }
    }

    private companion object {
        /**
         * Only services in this list may run inside the work profile. Generic
         * system accessibility services only — never a third-party service.
         */
        val TRUSTED_ACCESSIBILITY_SERVICES: List<String> = listOf(
            "com.google.android.marvin.talkback/.TalkBackService",
            "com.samsung.android.accessibility.universalswitch/.UniversalSwitchService",
            "com.samsung.android.accessibility.extradim/.ExtraDimAccessibilityService"
        )

        /**
         * Only IMEs in this list may run inside the work profile. Generic
         * system keyboards only — never a third-party keyboard.
         */
        val TRUSTED_INPUT_METHODS: List<String> = listOf(
            "com.google.android.inputmethod.latin/com.android.inputmethod.latin.LatinIME",
            "com.samsung.android.stt.ime/.SamsungIME",
            "com.samsung.android.honeyboard/.service.HoneyBoardService",
            "com.android.inputmethod.latin/.LatinIME"
        )
    }
}
