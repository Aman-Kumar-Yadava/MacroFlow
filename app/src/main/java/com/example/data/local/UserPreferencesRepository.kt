package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

data class UserPreferences(
    val themeMode: String = "DARK", // "DARK", "LIGHT", "SYSTEM"
    val floatingToolbarEnabled: Boolean = false,
    val floatingXRatio: Float = 0.92f,
    val floatingYRatio: Float = 0.40f,
    val floatingOpacity: Float = 0.85f,
    val floatingSize: String = "Medium", // "Small", "Medium", "Large"
    val floatingShape: String = "Circular", // "Circular", "Rounded Square"
    val floatingAccentColor: String = "#22C55E",
    val edgeSnappingEnabled: Boolean = true,
    val defaultPlaybackSpeed: Float = 1.0f,
    val defaultRepetitions: Int = 1,
    val defaultInterActionDelayMs: Long = 100L,
    val recordingCountdownSec: Int = 3,
    val stopOnError: Boolean = true,
    val useShizukuIfAvailable: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferenceKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FLOATING_ENABLED = booleanPreferencesKey("floating_enabled")
        val FLOATING_X = floatPreferencesKey("floating_x")
        val FLOATING_Y = floatPreferencesKey("floating_y")
        val FLOATING_OPACITY = floatPreferencesKey("floating_opacity")
        val FLOATING_SIZE = stringPreferencesKey("floating_size")
        val FLOATING_SHAPE = stringPreferencesKey("floating_shape")
        val FLOATING_ACCENT_COLOR = stringPreferencesKey("floating_accent_color")
        val EDGE_SNAPPING = booleanPreferencesKey("edge_snapping")
        val DEFAULT_SPEED = floatPreferencesKey("default_speed")
        val DEFAULT_REPETITIONS = intPreferencesKey("default_repetitions")
        val DEFAULT_DELAY = longPreferencesKey("default_delay")
        val RECORDING_COUNTDOWN = intPreferencesKey("recording_countdown")
        val STOP_ON_ERROR = booleanPreferencesKey("stop_on_error")
        val USE_SHIZUKU = booleanPreferencesKey("use_shizuku")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            themeMode = preferences[PreferenceKeys.THEME_MODE] ?: "DARK",
            floatingToolbarEnabled = preferences[PreferenceKeys.FLOATING_ENABLED] ?: false,
            floatingXRatio = preferences[PreferenceKeys.FLOATING_X] ?: 0.92f,
            floatingYRatio = preferences[PreferenceKeys.FLOATING_Y] ?: 0.40f,
            floatingOpacity = preferences[PreferenceKeys.FLOATING_OPACITY] ?: 0.85f,
            floatingSize = preferences[PreferenceKeys.FLOATING_SIZE] ?: "Medium",
            floatingShape = preferences[PreferenceKeys.FLOATING_SHAPE] ?: "Circular",
            floatingAccentColor = preferences[PreferenceKeys.FLOATING_ACCENT_COLOR] ?: "#22C55E",
            edgeSnappingEnabled = preferences[PreferenceKeys.EDGE_SNAPPING] ?: true,
            defaultPlaybackSpeed = preferences[PreferenceKeys.DEFAULT_SPEED] ?: 1.0f,
            defaultRepetitions = preferences[PreferenceKeys.DEFAULT_REPETITIONS] ?: 1,
            defaultInterActionDelayMs = preferences[PreferenceKeys.DEFAULT_DELAY] ?: 100L,
            recordingCountdownSec = preferences[PreferenceKeys.RECORDING_COUNTDOWN] ?: 3,
            stopOnError = preferences[PreferenceKeys.STOP_ON_ERROR] ?: true,
            useShizukuIfAvailable = preferences[PreferenceKeys.USE_SHIZUKU] ?: true
        )
    }

    suspend fun updateThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.THEME_MODE] = mode
        }
    }

    suspend fun updateFloatingToolbarEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.FLOATING_ENABLED] = enabled
        }
    }

    suspend fun updateFloatingPosition(xRatio: Float, yRatio: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.FLOATING_X] = xRatio
            preferences[PreferenceKeys.FLOATING_Y] = yRatio
        }
    }

    suspend fun updateFloatingAppearance(
        opacity: Float,
        size: String,
        shape: String,
        accentColor: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.FLOATING_OPACITY] = opacity
            preferences[PreferenceKeys.FLOATING_SIZE] = size
            preferences[PreferenceKeys.FLOATING_SHAPE] = shape
            preferences[PreferenceKeys.FLOATING_ACCENT_COLOR] = accentColor
        }
    }

    suspend fun updatePlaybackDefaults(speed: Float, repetitions: Int, delayMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.DEFAULT_SPEED] = speed
            preferences[PreferenceKeys.DEFAULT_REPETITIONS] = repetitions
            preferences[PreferenceKeys.DEFAULT_DELAY] = delayMs
        }
    }

    suspend fun updateRecordingCountdown(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.RECORDING_COUNTDOWN] = seconds
        }
    }

    suspend fun updateUseShizuku(useShizuku: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.USE_SHIZUKU] = useShizuku
        }
    }
}
