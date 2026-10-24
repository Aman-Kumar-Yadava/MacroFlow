package com.example.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import com.example.service.MacroAccessibilityService
import com.example.shizuku.ShizukuManager
import com.example.shizuku.ShizukuState

data class DeviceCapabilities(
    val androidVersion: String,
    val apiLevel: Int,
    val manufacturer: String,
    val model: String,
    val displayWidthPx: Int,
    val displayHeightPx: Int,
    val densityDpi: Int,
    val isAccessibilityEnabled: Boolean,
    val isOverlayPermissionGranted: Boolean,
    val shizukuState: ShizukuState,
    val canDispatchAccessibilityGestures: Boolean
)

object DeviceCapabilityChecker {

    fun getCapabilities(context: Context): DeviceCapabilities {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        val accessibilityEnabled = MacroAccessibilityService.isServiceConnected()
        val overlayGranted = Settings.canDrawOverlays(context)
        val shizukuState = ShizukuManager.getInstance().shizukuState.value

        return DeviceCapabilities(
            androidVersion = Build.VERSION.RELEASE ?: "Unknown",
            apiLevel = Build.VERSION.SDK_INT,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            displayWidthPx = metrics.widthPixels,
            displayHeightPx = metrics.heightPixels,
            densityDpi = metrics.densityDpi,
            isAccessibilityEnabled = accessibilityEnabled,
            isOverlayPermissionGranted = overlayGranted,
            shizukuState = shizukuState,
            canDispatchAccessibilityGestures = accessibilityEnabled
        )
    }
}
