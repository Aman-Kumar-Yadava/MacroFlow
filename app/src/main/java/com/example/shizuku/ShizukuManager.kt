package com.example.shizuku

import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

enum class ShizukuState {
    NOT_INSTALLED,
    SERVICE_STOPPED,
    UNAUTHORIZED,
    AUTHORIZED_ADB,
    AUTHORIZED_ROOT,
    DISCONNECTED
}

class ShizukuManager private constructor() {

    private val _shizukuState = MutableStateFlow(ShizukuState.NOT_INSTALLED)
    val shizukuState: StateFlow<ShizukuState> = _shizukuState.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        _shizukuState.value = ShizukuState.DISCONNECTED
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            checkStatus()
        } else {
            _shizukuState.value = ShizukuState.UNAUTHORIZED
            _lastError.value = "Shizuku permission denied by user."
        }
    }

    fun init() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
            checkStatus()
        } catch (e: Throwable) {
            _shizukuState.value = ShizukuState.NOT_INSTALLED
            _lastError.value = "Shizuku library not initialized: ${e.message}"
        }
    }

    fun checkStatus() {
        try {
            if (!Shizuku.pingBinder()) {
                _shizukuState.value = ShizukuState.SERVICE_STOPPED
                return
            }

            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                val isRoot = Shizuku.isPreV11() || (Shizuku.getUid() == 0)
                _shizukuState.value = if (isRoot) ShizukuState.AUTHORIZED_ROOT else ShizukuState.AUTHORIZED_ADB
                _lastError.value = null
            } else {
                _shizukuState.value = ShizukuState.UNAUTHORIZED
            }
        } catch (e: Throwable) {
            _shizukuState.value = ShizukuState.SERVICE_STOPPED
            _lastError.value = e.localizedMessage
        }
    }

    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (Shizuku.pingBinder()) {
                Shizuku.requestPermission(requestCode)
            } else {
                _lastError.value = "Shizuku service is not running."
            }
        } catch (e: Throwable) {
            _lastError.value = "Request permission failed: ${e.message}"
        }
    }

    fun executeShellCommand(command: String): Boolean {
        if (_shizukuState.value != ShizukuState.AUTHORIZED_ADB && _shizukuState.value != ShizukuState.AUTHORIZED_ROOT) {
            return false
        }
        return try {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java,
                Array<String>::class.java,
                String::class.java
            )
            method.isAccessible = true
            val process = method.invoke(null, arrayOf("sh", "-c", command), null, null) as Process
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Throwable) {
            Log.e("ShizukuManager", "Shell execution error: $command", e)
            false
        }
    }

    fun destroy() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Throwable) {
            Log.e("ShizukuManager", "Error tearing down listeners", e)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ShizukuManager? = null

        fun getInstance(): ShizukuManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ShizukuManager().also { INSTANCE = it }
            }
        }
    }
}
