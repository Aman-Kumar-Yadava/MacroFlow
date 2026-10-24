package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent

class MacroAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        isConnected = true
        Log.i("MacroAccessibility", "MacroFlow Accessibility Service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Event monitoring if needed
    }

    override fun onInterrupt() {
        Log.w("MacroAccessibility", "MacroFlow Accessibility Service interrupted")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        isConnected = false
        Log.i("MacroAccessibility", "MacroFlow Accessibility Service unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isConnected = false
    }

    companion object {
        @Volatile
        private var instance: MacroAccessibilityService? = null

        @Volatile
        private var isConnected: Boolean = false

        fun getInstance(): MacroAccessibilityService? = instance
        fun isServiceConnected(): Boolean = isConnected && instance != null
    }
}
