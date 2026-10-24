package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MacroCategory
import com.example.data.model.MacroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MacroDao {
    @Query("SELECT * FROM macros ORDER BY updatedAt DESC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE id = :id")
    suspend fun getMacroById(id: String): MacroEntity?

    @Query("SELECT * FROM macros WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteMacros(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE category = :category ORDER BY updatedAt DESC")
    fun getMacrosByCategory(category: MacroCategory): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchMacros(query: String): Flow<List<MacroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity)

    @Update
    suspend fun updateMacro(macro: MacroEntity)

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteMacroById(id: String)

    @Query("UPDATE macros SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM macros")
    fun getMacroCount(): Flow<Int>
}
