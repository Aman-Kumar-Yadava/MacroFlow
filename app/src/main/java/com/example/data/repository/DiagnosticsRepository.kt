package com.example.data.repository

import com.example.data.local.ExecutionLogDao
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.ExecutionLogType
import kotlinx.coroutines.flow.Flow

class DiagnosticsRepository(private val logDao: ExecutionLogDao) {

    val allLogs: Flow<List<ExecutionLogEntity>> = logDao.getAllLogs()
    val successCount: Flow<Int> = logDao.getSuccessCount()
    val failedCount: Flow<Int> = logDao.getFailedCount()

    suspend fun logEvent(
        logType: ExecutionLogType,
        message: String,
        macroId: String? = null,
        macroName: String? = null,
        durationMs: Long = 0L,
        actionsExecuted: Int = 0,
        totalRepetitions: Int = 1,
        errorDetails: String? = null,
        backendUsed: String = "AccessibilityService"
    ) {
        val entity = ExecutionLogEntity(
            logType = logType,
            message = message,
            macroId = macroId,
            macroName = macroName,
            durationMs = durationMs,
            actionsExecuted = actionsExecuted,
            totalRepetitions = totalRepetitions,
            errorDetails = errorDetails,
            backendUsed = backendUsed
        )
        logDao.insertLog(entity)
    }

    suspend fun clearLogs() {
        logDao.clearAllLogs()
    }
}
