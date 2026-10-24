package com.example.engine

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.MacroAction
import com.example.data.model.MacroActionType
import com.example.service.MacroAccessibilityService
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AccessibilityGestureBackend : GesturePlaybackBackend {

    override val backendName: String = "AccessibilityService"

    override fun isAvailable(): Boolean {
        return MacroAccessibilityService.isServiceConnected()
    }

    override suspend fun dispatchAction(
        action: MacroAction,
        displayWidth: Int,
        displayHeight: Int
    ): Boolean {
        val service = MacroAccessibilityService.getInstance()
            ?: return false

        val startX = (action.startXNormalized * displayWidth).coerceIn(0f, displayWidth.toFloat())
        val startY = (action.startYNormalized * displayHeight).coerceIn(0f, displayHeight.toFloat())
        val endX = (action.endXNormalized * displayWidth).coerceIn(0f, displayWidth.toFloat())
        val endY = (action.endYNormalized * displayHeight).coerceIn(0f, displayHeight.toFloat())

        val path = Path().apply {
            moveTo(startX, startY)
            if (action.actionType == MacroActionType.SWIPE || action.actionType == MacroActionType.DRAG) {
                lineTo(endX, endY)
            } else if (action.actionType == MacroActionType.LONG_PRESS) {
                // For long press, stay at start coordinate
                lineTo(startX + 0.1f, startY + 0.1f)
            }
        }

        val gestureDuration = maxOf(1L, action.durationMs)
        val stroke = GestureDescription.StrokeDescription(path, 0L, gestureDuration)
        val gestureDescription = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return suspendCancellableCoroutine { continuation ->
            val callback = object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    super.onCompleted(gestureDescription)
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    super.onCancelled(gestureDescription)
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
            }

            try {
                val dispatched = service.dispatchGesture(gestureDescription, callback, Handler(Looper.getMainLooper()))
                if (!dispatched && continuation.isActive) {
                    continuation.resume(false)
                }
            } catch (e: Exception) {
                Log.e("AccessibilityBackend", "Error dispatching gesture", e)
                if (continuation.isActive) {
                    continuation.resume(false)
                }
            }
        }
    }

    override suspend fun cancelCurrentPlayback() {
        // AccessibilityService cancels stroke if new gesture or service state changes
    }
}
