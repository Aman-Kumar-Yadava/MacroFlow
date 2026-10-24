package com.example.data.model

import com.squareup.moshi.JsonClass

enum class MacroActionType {
    TAP,
    LONG_PRESS,
    SWIPE,
    DRAG
}

@JsonClass(generateAdapter = true)
data class MacroAction(
    val id: String,
    val actionType: MacroActionType,
    val startXNormalized: Float, // 0.0f to 1.0f relative to display width
    val startYNormalized: Float, // 0.0f to 1.0f relative to display height
    val endXNormalized: Float = startXNormalized, // For SWIPE and DRAG
    val endYNormalized: Float = startYNormalized, // For SWIPE and DRAG
    val durationMs: Long = 50L,   // Gesture duration (50ms tap, 500ms swipe/long press)
    val delayBeforeMs: Long = 100L, // Delay in ms before executing this action
    val pointerCount: Int = 1,
    val orderIndex: Int = 0,
    val note: String? = null
)
