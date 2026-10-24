package com.example

import android.app.Application
import com.example.data.local.MacroDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.DiagnosticsRepository
import com.example.data.repository.MacroRepository
import com.example.engine.MacroPlaybackEngine
import com.example.shizuku.ShizukuManager

class MacroFlowApplication : Application() {

    lateinit var database: MacroDatabase
        private set
    lateinit var macroRepository: MacroRepository
        private set
    lateinit var diagnosticsRepository: DiagnosticsRepository
        private set
    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set
    lateinit var playbackEngine: MacroPlaybackEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = MacroDatabase.getDatabase(this)
        macroRepository = MacroRepository(database.macroDao())
        diagnosticsRepository = DiagnosticsRepository(database.executionLogDao())
        userPreferencesRepository = UserPreferencesRepository(this)
        playbackEngine = MacroPlaybackEngine.getInstance(this, macroRepository, diagnosticsRepository)

        // Initialize Shizuku binder listener
        ShizukuManager.getInstance().init()
    }

    companion object {
        lateinit var instance: MacroFlowApplication
            private set
    }
}
