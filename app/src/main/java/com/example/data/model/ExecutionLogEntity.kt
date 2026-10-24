package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ExecutionLogType {
    PLAYBACK_SUCCESS,
    PLAYBACK_FAILED,
    PLAYBACK_INTERRUPTED,
    RECORDING_SAVED,
    SERVICE_EVENT,
    SHIZUKU_EVENT
}

@Entity(tableName = "execution_logs")
data class ExecutionLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val logType: ExecutionLogType,
    val macroId: String? = null,
    val macroName: String? = null,
    val durationMs: Long = 0L,
    val actionsExecuted: Int = 0,
    val totalRepetitions: Int = 1,
    val message: String,
    val errorDetails: String? = null,
    val backendUsed: String = "AccessibilityService"
)
