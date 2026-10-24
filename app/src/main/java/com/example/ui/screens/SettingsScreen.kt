package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MacroFlowApplication
import com.example.shizuku.ShizukuManager
import com.example.shizuku.ShizukuState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val app = MacroFlowApplication.instance
    val scope = rememberCoroutineScope()
    val prefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(initial = com.example.data.local.UserPreferences())
    val shizukuState by ShizukuManager.getInstance().shizukuState.collectAsState()

    var showClearDataDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Application Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Theme Preference
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APPEARANCE THEME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DARK", "LIGHT", "SYSTEM").forEach { mode ->
                            FilterChip(
                                selected = prefs.themeMode == mode,
                                onClick = {
                                    scope.launch { app.userPreferencesRepository.updateThemeMode(mode) }
                                },
                                label = { Text(mode.lowercase().replaceFirstChar { it.uppercase() }) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF22C55E))
                            )
                        }
                    }
                }
            }
        }

        // Shizuku Onboarding Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF4ADE80))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Shizuku ADB/Root Integration", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val statusText = when (shizukuState) {
                        ShizukuState.AUTHORIZED_ADB -> "Authorized (ADB Shell Privilege)"
                        ShizukuState.AUTHORIZED_ROOT -> "Authorized (Root Privilege)"
                        ShizukuState.UNAUTHORIZED -> "Shizuku Running (Unauthorized)"
                        ShizukuState.SERVICE_STOPPED -> "Shizuku Service Stopped"
                        else -> "Shizuku Not Running / Standard Fallback"
                    }

                    Text(
                        text = "Status: $statusText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4ADE80)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Optional enhancement. When authorized, Shizuku enables low-level input injection via ADB shell. MacroFlow functions completely in Standard Mode if Shizuku is not installed.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { ShizukuManager.getInstance().checkStatus() }
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Check Connection")
                        }

                        if (shizukuState == ShizukuState.UNAUTHORIZED) {
                            Button(
                                onClick = { ShizukuManager.getInstance().requestPermission() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
                            ) {
                                Text("Authorize", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Local Storage
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF22C55E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy & Storage", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "All saved macros and touch gestures stay strictly on your local device storage. No cloud accounts, screen contents, or gesture coordinates are ever uploaded.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444))
                    ) {
                        Text("Delete All Local Macros")
                    }
                }
            }
        }

        // About & Version
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MacroFlow — Touch Macro Studio", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Version 1.0 (Package: com.macroflow.touchstudio)", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Universal Android Macro Recorder & Gaming Assistant inspired by OnePlus, OPPO, and realme Game Assistant utilities.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Macro Data?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all saved macros? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            app.database.clearAllTables()
                            Toast.makeText(context, "All macro data cleared", Toast.LENGTH_SHORT).show()
                        }
                        showClearDataDialog = false
                    }
                ) {
                    Text("Delete All", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") }
            }
        )
    }
}
