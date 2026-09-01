package com.fyllo.filemanager.core.admin

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class DeviceAdminManager(private val context: Context) {

    private val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager

    val adminComponent = ComponentName(context, AdminReceiver::class.java)

    val isAdminActive: Boolean
        get() = devicePolicyManager.isAdminActive(adminComponent)

    fun requestAdminRights(activity: Activity) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Enable Device Administrator rights to protect File Manager from unauthorized uninstallation without Admin Passcode."
            )
        }
        activity.startActivity(intent)
    }

    fun deactivateAdminRights() {
        if (isAdminActive) {
            devicePolicyManager.removeActiveAdmin(adminComponent)
        }
    }
}
