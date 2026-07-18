package com.digi.digi_vamana.vamana.isolate

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

/**
 * Device Policy Controller receiver backing the VAMANA-Isolate work profile.
 *
 * Android grants this receiver Profile Owner status once the user accepts
 * the provisioning flow started from [WorkProfileManager.buildProvisioningIntent].
 * From that point on it applies every policy in [PolicyEnforcer], including
 * enabling the profile — a freshly provisioned profile stays hidden from the
 * launcher and from [android.os.UserManager.getUserProfiles] until a profile
 * owner explicitly enables it.
 */
class WorkProfileAdminReceiver : DeviceAdminReceiver() {

    /** Fires in the PERSONAL profile once managed profile provisioning completes. */
    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        applyPoliciesIfOwner(context)
    }

    /**
     * Fires inside the WORK PROFILE itself after it's fully provisioned and
     * started. Re-asserting policies here too is belt-and-suspenders:
     * onProfileProvisioningComplete doesn't reliably fire on every device.
     */
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        applyPoliciesIfOwner(context)
    }

    private fun applyPoliciesIfOwner(context: Context) {
        val enforcer = PolicyEnforcer(context)
        if (enforcer.isProfileOwner()) {
            enforcer.applyAllInitialPolicies()
        }
    }
}
