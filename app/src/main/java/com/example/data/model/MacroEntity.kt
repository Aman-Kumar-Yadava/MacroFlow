package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MacroCategory {
    GAMING,
    NAVIGATION,
    AUTOMATION,
    GENERAL
}

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val category: MacroCategory = MacroCategory.GAMING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val actionCount: Int = 0,
    val durationMs: Long = 0L,
    val repetitions: Int = 1,              // 1 to 999
    val isContinuous: Boolean = false,     // Continuous repeat mode
    val playbackSpeed: Float = 1.0f,       // 0.25f to 3.0f
    val buttonOpacity: Float = 0.8f,       // 0.20f to 1.0f
    val buttonSize: String = "Medium",     // "Small", "Medium", "Large"
    val buttonShape: String = "Circular",  // "Circular", "Rounded Square"
    val buttonIcon: String = "PlayArrow",  // Icon name
    val buttonAccentColor: String = "#22C55E", // Hex color
    val showButtonLabel: Boolean = true,
    val isFavorite: Boolean = false,
    val lastUsedAt: Long? = null,
    val actions: List<MacroAction> = emptyList()
)
