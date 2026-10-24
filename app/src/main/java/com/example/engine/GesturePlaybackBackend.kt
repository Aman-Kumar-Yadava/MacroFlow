package com.example.engine

import com.example.data.model.MacroAction

interface GesturePlaybackBackend {
    val backendName: String
    fun isAvailable(): Boolean
    suspend fun dispatchAction(
        action: MacroAction,
        displayWidth: Int,
        displayHeight: Int
    ): Boolean

    suspend fun cancelCurrentPlayback()
}
