package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.service.MacroAccessibilityService

object PermissionManager {

    fun isAccessibilityEnabled(): Boolean = MacroAccessibilityService.isServiceConnected()

    fun isOverlayPermissionGranted(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun openAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun openOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
