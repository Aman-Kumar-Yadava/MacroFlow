package com.example.engine

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.example.data.model.ExecutionLogType
import com.example.data.model.MacroEntity
import com.example.data.repository.DiagnosticsRepository
import com.example.data.repository.MacroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PlaybackState {
    object Idle : PlaybackState()
    data class Preparing(val macroName: String) : PlaybackState()
    data class Playing(
        val macroName: String,
        val currentRepetition: Int,
        val totalRepetitions: Int,
        val isContinuous: Boolean,
        val currentActionIndex: Int,
        val totalActions: Int,
        val playbackSpeed: Float
    ) : PlaybackState()
    object Paused : PlaybackState()
    object Stopping : PlaybackState()
    data class Completed(val macroName: String, val totalRepetitionsCompleted: Int) : PlaybackState()
    data class Failed(val macroName: String, val reason: String) : PlaybackState()
}

class MacroPlaybackEngine private constructor(
    private val context: Context,
    private val macroRepository: MacroRepository,
    private val diagnosticsRepository: DiagnosticsRepository
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var playbackJob: Job? = null

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val accessibilityBackend = AccessibilityGestureBackend()
    private val shizukuBackend = ShizukuGestureBackend()

    var forceShizukuBackend: Boolean = false

    fun startPlayback(macro: MacroEntity) {
        if (_playbackState.value is PlaybackState.Playing) {
            Log.w("MacroPlaybackEngine", "Playback already in progress. Rejecting concurrent playback request.")
            return
        }

        playbackJob?.cancel()
        playbackJob = scope.launch {
            executeMacroPlayback(macro)
        }
    }

    fun stopPlayback() {
        if (_playbackState.value is PlaybackState.Idle) return
        _playbackState.value = PlaybackState.Stopping
        playbackJob?.cancel()
        playbackJob = null
        _playbackState.value = PlaybackState.Idle
    }

    private suspend fun executeMacroPlayback(macro: MacroEntity) {
        val startTime = System.currentTimeMillis()
        _playbackState.value = PlaybackState.Preparing(macro.name)

        if (macro.actions.isEmpty()) {
            _playbackState.value = PlaybackState.Failed(macro.name, "Macro contains no touch actions.")
            diagnosticsRepository.logEvent(
                logType = ExecutionLogType.PLAYBACK_FAILED,
                message = "Failed to start '${macro.name}': No actions recorded.",
                macroId = macro.id,
                macroName = macro.name,
                errorDetails = "Action list is empty."
            )
            return
        }

        val backend = selectBackend()
        if (!backend.isAvailable()) {
            val errorMsg = if (backend is AccessibilityGestureBackend) {
                "Accessibility Service is disabled. Please enable MacroFlow in Android Settings."
            } else {
                "Shizuku service is disconnected or unauthorized."
            }
            _playbackState.value = PlaybackState.Failed(macro.name, errorMsg)
            diagnosticsRepository.logEvent(
                logType = ExecutionLogType.PLAYBACK_FAILED,
                message = "Failed to start '${macro.name}': ${backend.backendName} unavailable.",
                macroId = macro.id,
                macroName = macro.name,
                errorDetails = errorMsg,
                backendUsed = backend.backendName
            )
            return
        }

        val (displayWidth, displayHeight) = getScreenDimensions()
        val speed = macro.playbackSpeed.coerceIn(0.25f, 3.0f)
        val totalReps = if (macro.isContinuous) Int.MAX_VALUE else macro.repetitions.coerceIn(1, 999)

        var currentRep = 1
        var totalActionsExecuted = 0

        try {
            macroRepository.updateLastUsed(macro.id)

            while (currentRep <= totalReps) {
                for ((index, action) in macro.actions.withIndex()) {
                    _playbackState.value = PlaybackState.Playing(
                        macroName = macro.name,
                        currentRepetition = currentRep,
                        totalRepetitions = if (macro.isContinuous) -1 else totalReps,
                        isContinuous = macro.isContinuous,
                        currentActionIndex = index + 1,
                        totalActions = macro.actions.size,
                        playbackSpeed = speed
                    )

                    // Scaled delay before action
                    val scaledDelay = (action.delayBeforeMs / speed).toLong().coerceAtLeast(0L)
                    if (scaledDelay > 0) {
                        delay(scaledDelay)
                    }

                    // Scaled action
                    val scaledAction = action.copy(
                        durationMs = (action.durationMs / speed).toLong().coerceAtLeast(1L)
                    )

                    val success = backend.dispatchAction(
                        action = scaledAction,
                        displayWidth = displayWidth,
                        displayHeight = displayHeight
                    )

                    if (!success) {
                        _playbackState.value = PlaybackState.Failed(
                            macroName = macro.name,
                            reason = "Gesture dispatch failed at action #${index + 1} (${action.actionType})."
                        )
                        diagnosticsRepository.logEvent(
                            logType = ExecutionLogType.PLAYBACK_FAILED,
                            message = "Macro '${macro.name}' failed at action #${index + 1}",
                            macroId = macro.id,
                            macroName = macro.name,
                            durationMs = System.currentTimeMillis() - startTime,
                            actionsExecuted = totalActionsExecuted,
                            errorDetails = "Backend failed to execute gesture stroke",
                            backendUsed = backend.backendName
                        )
                        return
                    }

                    totalActionsExecuted++
                }

                currentRep++
            }

            val totalDuration = System.currentTimeMillis() - startTime
            val finalRepsCompleted = currentRep - 1

            _playbackState.value = PlaybackState.Completed(macro.name, finalRepsCompleted)
            diagnosticsRepository.logEvent(
                logType = ExecutionLogType.PLAYBACK_SUCCESS,
                message = "Macro '${macro.name}' completed ($finalRepsCompleted reps).",
                macroId = macro.id,
                macroName = macro.name,
                durationMs = totalDuration,
                actionsExecuted = totalActionsExecuted,
                totalRepetitions = finalRepsCompleted,
                backendUsed = backend.backendName
            )

        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Idle
            Log.i("MacroPlaybackEngine", "Playback cancelled or interrupted", e)
            diagnosticsRepository.logEvent(
                logType = ExecutionLogType.PLAYBACK_INTERRUPTED,
                message = "Macro '${macro.name}' stopped by user.",
                macroId = macro.id,
                macroName = macro.name,
                durationMs = System.currentTimeMillis() - startTime,
                actionsExecuted = totalActionsExecuted,
                backendUsed = backend.backendName
            )
        }
    }

    private fun selectBackend(): GesturePlaybackBackend {
        return if (forceShizukuBackend && shizukuBackend.isAvailable()) {
            shizukuBackend
        } else {
            accessibilityBackend
        }
    }

    private fun getScreenDimensions(): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }

    companion object {
        @Volatile
        private var INSTANCE: MacroPlaybackEngine? = null

        fun getInstance(
            context: Context,
            macroRepository: MacroRepository,
            diagnosticsRepository: DiagnosticsRepository
        ): MacroPlaybackEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MacroPlaybackEngine(
                    context.applicationContext,
                    macroRepository,
                    diagnosticsRepository
                ).also { INSTANCE = it }
            }
        }

        fun getInstance(): MacroPlaybackEngine? = INSTANCE
    }
}
