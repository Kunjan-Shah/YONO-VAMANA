package com.yono.yono_vamana.vamana.isolate

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Device Policy Controller receiver backing the VAMANA-Isolate work profile.
 *
 * Android grants this receiver Profile Owner status once the user accepts
 * the provisioning flow started from [WorkProfileManager.buildProvisioningIntent].
 * A newly provisioned profile stays hidden from the launcher and from
 * [android.os.UserManager.getUserProfiles] until a profile owner explicitly
 * enables it — that's the one thing this receiver does.
 */
class WorkProfileAdminReceiver : DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, WorkProfileAdminReceiver::class.java)
        dpm.setProfileEnabled(adminComponent)
    }
}
