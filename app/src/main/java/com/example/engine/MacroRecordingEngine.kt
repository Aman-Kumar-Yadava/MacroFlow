package com.example.engine

import com.example.data.model.MacroAction
import com.example.data.model.MacroActionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

sealed class RecordingState {
    object Idle : RecordingState()
    data class Countdown(val secondsRemaining: Int) : RecordingState()
    data class Recording(
        val macroName: String,
        val elapsedTimeMs: Long,
        val actionCount: Int,
        val actions: List<MacroAction>
    ) : RecordingState()
    data class Paused(
        val macroName: String,
        val elapsedTimeMs: Long,
        val actionCount: Int,
        val actions: List<MacroAction>
    ) : RecordingState()
    data class Review(
        val macroName: String,
        val totalDurationMs: Long,
        val actions: List<MacroAction>
    ) : RecordingState()
}

class MacroRecordingEngine private constructor() {

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()

    private val recordedActions = mutableListOf<MacroAction>()
    private var currentMacroName: String = "New Macro"
    private var recordingStartTime: Long = 0L
    var lastActionTimestamp: Long = 0L

    fun startRecordingSession(macroName: String) {
        recordedActions.clear()
        currentMacroName = macroName.ifBlank { "New Macro" }
        recordingStartTime = System.currentTimeMillis()
        lastActionTimestamp = recordingStartTime

        _recordingState.value = RecordingState.Recording(
            macroName = currentMacroName,
            elapsedTimeMs = 0L,
            actionCount = 0,
            actions = emptyList()
        )
    }

    fun captureTouchAction(
        actionType: MacroActionType,
        startXNormalized: Float,
        startYNormalized: Float,
        endXNormalized: Float = startXNormalized,
        endYNormalized: Float = startYNormalized,
        durationMs: Long = 50L
    ) {
        val currentState = _recordingState.value
        if (currentState !is RecordingState.Recording) return

        val now = System.currentTimeMillis()
        val delayBefore = (now - lastActionTimestamp).coerceAtLeast(10L)
        lastActionTimestamp = now + durationMs

        val action = MacroAction(
            id = UUID.randomUUID().toString(),
            actionType = actionType,
            startXNormalized = startXNormalized.coerceIn(0f, 1f),
            startYNormalized = startYNormalized.coerceIn(0f, 1f),
            endXNormalized = endXNormalized.coerceIn(0f, 1f),
            endYNormalized = endYNormalized.coerceIn(0f, 1f),
            durationMs = durationMs,
            delayBeforeMs = delayBefore,
            orderIndex = recordedActions.size
        )

        recordedActions.add(action)

        _recordingState.value = RecordingState.Recording(
            macroName = currentMacroName,
            elapsedTimeMs = now - recordingStartTime,
            actionCount = recordedActions.size,
            actions = ArrayList(recordedActions)
        )
    }

    fun pauseRecording() {
        val currentState = _recordingState.value
        if (currentState is RecordingState.Recording) {
            _recordingState.value = RecordingState.Paused(
                macroName = currentMacroName,
                elapsedTimeMs = currentState.elapsedTimeMs,
                actionCount = recordedActions.size,
                actions = ArrayList(recordedActions)
            )
        }
    }

    fun resumeRecording() {
        val currentState = _recordingState.value
        if (currentState is RecordingState.Paused) {
            lastActionTimestamp = System.currentTimeMillis()
            _recordingState.value = RecordingState.Recording(
                macroName = currentMacroName,
                elapsedTimeMs = currentState.elapsedTimeMs,
                actionCount = recordedActions.size,
                actions = ArrayList(recordedActions)
            )
        }
    }

    fun stopRecordingSession(): List<MacroAction> {
        val totalDuration = if (recordedActions.isEmpty()) 0L else System.currentTimeMillis() - recordingStartTime
        val finalActions = ArrayList(recordedActions)

        _recordingState.value = RecordingState.Review(
            macroName = currentMacroName,
            totalDurationMs = totalDuration,
            actions = finalActions
        )
        return finalActions
    }

    fun cancelRecording() {
        recordedActions.clear()
        _recordingState.value = RecordingState.Idle
    }

    fun removeActionAt(index: Int) {
        if (index in recordedActions.indices) {
            recordedActions.removeAt(index)
            // Re-index remaining actions
            val reindexed = recordedActions.mapIndexed { idx, act -> act.copy(orderIndex = idx) }
            recordedActions.clear()
            recordedActions.addAll(reindexed)

            val currentState = _recordingState.value
            if (currentState is RecordingState.Review) {
                val totalDuration = recordedActions.sumOf { it.delayBeforeMs + it.durationMs }
                _recordingState.value = RecordingState.Review(
                    macroName = currentMacroName,
                    totalDurationMs = totalDuration,
                    actions = ArrayList(recordedActions)
                )
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MacroRecordingEngine? = null

        fun getInstance(): MacroRecordingEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MacroRecordingEngine().also { INSTANCE = it }
            }
        }
    }
}
