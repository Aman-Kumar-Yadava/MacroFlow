package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MacroAction
import com.example.data.model.MacroCategory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MacroConverters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val actionListType = Types.newParameterizedType(List::class.java, MacroAction::class.java)
    private val actionListAdapter = moshi.adapter<List<MacroAction>>(actionListType)

    @TypeConverter
    fun fromActionList(actions: List<MacroAction>?): String {
        return actionListAdapter.toJson(actions ?: emptyList())
    }

    @TypeConverter
    fun toActionList(actionsJson: String?): List<MacroAction> {
        if (actionsJson.isNullOrEmpty()) return emptyList()
        return try {
            actionListAdapter.fromJson(actionsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromCategory(category: MacroCategory): String = category.name

    @TypeConverter
    fun toCategory(categoryStr: String): MacroCategory {
        return try {
            MacroCategory.valueOf(categoryStr)
        } catch (e: Exception) {
            MacroCategory.GAMING
        }
    }
}
