package com.example.engine

import com.example.data.model.MacroAction
import com.example.data.model.MacroActionType
import com.example.shizuku.ShizukuManager
import com.example.shizuku.ShizukuState

class ShizukuGestureBackend : GesturePlaybackBackend {

    override val backendName: String = "Shizuku (ADB Shell Input)"

    override fun isAvailable(): Boolean {
        val state = ShizukuManager.getInstance().shizukuState.value
        return state == ShizukuState.AUTHORIZED_ADB || state == ShizukuState.AUTHORIZED_ROOT
    }

    override suspend fun dispatchAction(
        action: MacroAction,
        displayWidth: Int,
        displayHeight: Int
    ): Boolean {
        if (!isAvailable()) return false

        val startX = (action.startXNormalized * displayWidth).toInt().coerceIn(0, displayWidth)
        val startY = (action.startYNormalized * displayHeight).toInt().coerceIn(0, displayHeight)
        val endX = (action.endXNormalized * displayWidth).toInt().coerceIn(0, displayWidth)
        val endY = (action.endYNormalized * displayHeight).toInt().coerceIn(0, displayHeight)
        val duration = maxOf(1L, action.durationMs)

        val command = when (action.actionType) {
            MacroActionType.TAP -> "input tap $startX $startY"
            MacroActionType.LONG_PRESS -> "input swipe $startX $startY $startX $startY $duration"
            MacroActionType.SWIPE, MacroActionType.DRAG -> "input swipe $startX $startY $endX $endY $duration"
        }

        return ShizukuManager.getInstance().executeShellCommand(command)
    }

    override suspend fun cancelCurrentPlayback() {
        // Process killed when coroutine cancels
    }
}
