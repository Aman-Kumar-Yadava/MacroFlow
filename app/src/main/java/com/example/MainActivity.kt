package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.MacroFlowNavGraph
import com.example.ui.theme.MacroFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = MacroFlowApplication.instance
            val userPrefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = com.example.data.local.UserPreferences()
            )

            val isDarkTheme = when (userPrefs.themeMode) {
                "LIGHT" -> false
                "SYSTEM" -> androidx.compose.foundation.isSystemInDarkTheme()
                else -> true // Default to premium dark gaming theme
            }

            MacroFlowTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    MacroFlowNavGraph(navController = navController)
                }
            }
        }
    }
}
