package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.ExecutionLogEntity
import com.example.data.model.MacroEntity

@Database(
    entities = [MacroEntity::class, ExecutionLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(MacroConverters::class)
abstract class MacroDatabase : RoomDatabase() {
    abstract fun macroDao(): MacroDao
    abstract fun executionLogDao(): ExecutionLogDao

    companion object {
        @Volatile
        private var INSTANCE: MacroDatabase? = null

        fun getDatabase(context: Context): MacroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MacroDatabase::class.java,
                    "macroflow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
