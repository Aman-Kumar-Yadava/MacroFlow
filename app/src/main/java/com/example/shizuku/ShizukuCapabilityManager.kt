package com.example.shizuku

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class ShizukuCapabilityState {
    object StandardMode : ShizukuCapabilityState()
    data class ShizukuAvailable(val privilegeMode: String, val canInjectInput: Boolean) : ShizukuCapabilityState()
    data class AdvancedUnavailable(val reason: String) : ShizukuCapabilityState()
}

class ShizukuCapabilityManager private constructor() {

    private val _capabilityState = MutableStateFlow<ShizukuCapabilityState>(ShizukuCapabilityState.StandardMode)
    val capabilityState: StateFlow<ShizukuCapabilityState> = _capabilityState.asStateFlow()

    fun refreshCapabilities() {
        val manager = ShizukuManager.getInstance()
        manager.checkStatus()

        when (val state = manager.shizukuState.value) {
            ShizukuState.AUTHORIZED_ADB -> {
                _capabilityState.value = ShizukuCapabilityState.ShizukuAvailable(
                    privilegeMode = "ADB Shell",
                    canInjectInput = true
                )
            }
            ShizukuState.AUTHORIZED_ROOT -> {
                _capabilityState.value = ShizukuCapabilityState.ShizukuAvailable(
                    privilegeMode = "Root Privilege",
                    canInjectInput = true
                )
            }
            ShizukuState.UNAUTHORIZED -> {
                _capabilityState.value = ShizukuCapabilityState.AdvancedUnavailable("Shizuku is running but MacroFlow is not authorized.")
            }
            ShizukuState.SERVICE_STOPPED -> {
                _capabilityState.value = ShizukuCapabilityState.AdvancedUnavailable("Shizuku service is not running.")
            }
            ShizukuState.NOT_INSTALLED -> {
                _capabilityState.value = ShizukuCapabilityState.StandardMode
            }
            ShizukuState.DISCONNECTED -> {
                _capabilityState.value = ShizukuCapabilityState.AdvancedUnavailable("Shizuku binder disconnected.")
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ShizukuCapabilityManager? = null

        fun getInstance(): ShizukuCapabilityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ShizukuCapabilityManager().also { INSTANCE = it }
            }
        }
    }
}
