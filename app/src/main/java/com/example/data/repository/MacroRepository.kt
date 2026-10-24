package com.example.data.repository

import com.example.data.local.MacroDao
import com.example.data.model.MacroAction
import com.example.data.model.MacroCategory
import com.example.data.model.MacroEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class MacroRepository(private val macroDao: MacroDao) {

    val allMacros: Flow<List<MacroEntity>> = macroDao.getAllMacros()
    val favoriteMacros: Flow<List<MacroEntity>> = macroDao.getFavoriteMacros()
    val macroCount: Flow<Int> = macroDao.getMacroCount()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val macroAdapter = moshi.adapter(MacroEntity::class.java)

    suspend fun getMacroById(id: String): MacroEntity? = macroDao.getMacroById(id)

    fun searchMacros(query: String): Flow<List<MacroEntity>> = macroDao.searchMacros(query)

    fun getMacrosByCategory(category: MacroCategory): Flow<List<MacroEntity>> =
        macroDao.getMacrosByCategory(category)

    suspend fun saveMacro(macro: MacroEntity) {
        val calculatedDuration = calculateMacroDuration(macro.actions)
        val updated = macro.copy(
            actionCount = macro.actions.size,
            durationMs = calculatedDuration,
            updatedAt = System.currentTimeMillis()
        )
        macroDao.insertMacro(updated)
    }

    suspend fun updateMacro(macro: MacroEntity) {
        val calculatedDuration = calculateMacroDuration(macro.actions)
        val updated = macro.copy(
            actionCount = macro.actions.size,
            durationMs = calculatedDuration,
            updatedAt = System.currentTimeMillis()
        )
        macroDao.updateMacro(updated)
    }

    suspend fun deleteMacro(macro: MacroEntity) {
        macroDao.deleteMacro(macro)
    }

    suspend fun deleteMacroById(id: String) {
        macroDao.deleteMacroById(id)
    }

    suspend fun updateLastUsed(id: String) {
        macroDao.updateLastUsed(id, System.currentTimeMillis())
    }

    suspend fun duplicateMacro(macroId: String): MacroEntity? {
        val original = macroDao.getMacroById(macroId) ?: return null
        val duplicate = original.copy(
            id = UUID.randomUUID().toString(),
            name = "${original.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            lastUsedAt = null
        )
        macroDao.insertMacro(duplicate)
        return duplicate
    }

    fun exportMacroToJson(macro: MacroEntity): String {
        return macroAdapter.toJson(macro)
    }

    suspend fun importMacroFromJson(jsonString: String): Result<MacroEntity> {
        return try {
            val imported = macroAdapter.fromJson(jsonString)
                ?: return Result.failure(IllegalArgumentException("Invalid macro JSON payload"))

            // Assign new ID to avoid clashing with existing macros
            val newMacro = imported.copy(
                id = UUID.randomUUID().toString(),
                name = if (imported.name.isBlank()) "Imported Macro" else "${imported.name} (Imported)",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                lastUsedAt = null
            )
            macroDao.insertMacro(newMacro)
            Result.success(newMacro)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateMacroDuration(actions: List<MacroAction>): Long {
        if (actions.isEmpty()) return 0L
        return actions.sumOf { it.delayBeforeMs + it.durationMs }
    }
}
