package com.giino.bydcontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restart the service after a reboot (and after app update). */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val svc = Intent(context, LockSoundService::class.java)
        // Targeting SDK 28: a normal startService from BOOT_COMPLETED is allowed.
        context.startService(svc)
    }
}
