package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MacroFlowApplication
import com.example.data.model.MacroEntity
import com.example.service.FloatingOverlayService
import com.example.shizuku.ShizukuManager
import com.example.shizuku.ShizukuState
import com.example.util.PermissionManager

@Composable
fun HomeScreen(
    onCreateMacro: () -> Unit,
    onOpenMacros: () -> Unit,
    onEditMacro: (String) -> Unit,
    onOpenFloatingControls: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val app = MacroFlowApplication.instance
    val macros by app.macroRepository.allMacros.collectAsState(initial = emptyList())
    val preferences by app.userPreferencesRepository.userPreferencesFlow.collectAsState(initial = com.example.data.local.UserPreferences())
    val shizukuState by ShizukuManager.getInstance().shizukuState.collectAsState()

    val isAccessibilityEnabled = PermissionManager.isAccessibilityEnabled()
    val isOverlayGranted = PermissionManager.isOverlayPermissionGranted(context)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // App Header Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MacroFlow",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF22C55E)
                    )
                    Text(
                        text = "Touch Macro Studio & Gaming Assistant",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C202B))
                        .border(1.dp, Color(0xFF2E374A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = "Logo",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Primary Action: Create Macro Button
        item {
            Button(
                onClick = onCreateMacro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E), contentColor = Color.Black)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Create New Macro", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // System Services & Permissions Status Cards
        item {
            Text(
                text = "SYSTEM READY STATUS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Accessibility Service Card
                StatusCard(
                    title = "Accessibility Gesture Service",
                    statusText = if (isAccessibilityEnabled) "Service Active & Ready" else "Permission Required",
                    isOK = isAccessibilityEnabled,
                    onClick = {
                        context.startActivity(PermissionManager.openAccessibilitySettingsIntent())
                    }
                )

                // Overlay Permission Card
                StatusCard(
                    title = "Floating Window Overlay",
                    statusText = if (isOverlayGranted) "Overlay Permitted" else "Permission Required for Floating Bar",
                    isOK = isOverlayGranted,
                    onClick = {
                        context.startActivity(PermissionManager.openOverlayPermissionIntent(context))
                    }
                )

                // Shizuku Status Card
                val shizukuText = when (shizukuState) {
                    ShizukuState.AUTHORIZED_ADB -> "Shizuku Active (ADB Mode)"
                    ShizukuState.AUTHORIZED_ROOT -> "Shizuku Active (Root Mode)"
                    ShizukuState.UNAUTHORIZED -> "Shizuku Installed (Unauthorized)"
                    else -> "Standard Mode (Shizuku Inactive)"
                }
                StatusCard(
                    title = "Shizuku Integration",
                    statusText = shizukuText,
                    isOK = shizukuState == ShizukuState.AUTHORIZED_ADB || shizukuState == ShizukuState.AUTHORIZED_ROOT,
                    onClick = onOpenSettings
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Quick Stats Overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Saved Macros",
                    value = macros.size.toString(),
                    modifier = Modifier.weight(1f),
                    onClick = onOpenMacros
                )
                MetricCard(
                    title = "Floating Toolbar",
                    value = if (preferences.floatingToolbarEnabled) "Enabled" else "Off",
                    modifier = Modifier.weight(1f),
                    onClick = onOpenFloatingControls
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Recently Used Macros Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT MACROS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "View All (${macros.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF22C55E),
                    modifier = Modifier.clickable { onOpenMacros() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (macros.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Macros Created Yet",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Create New Macro' to record your first touch routine.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(macros.take(3)) { macro ->
                RecentMacroCard(
                    macro = macro,
                    onPlay = { app.playbackEngine.startPlayback(macro) },
                    onEdit = { onEditMacro(macro.id) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Quick Start Guide Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Start Guide",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4ADE80)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Enable Accessibility Service so MacroFlow can dispatch touches.\n" +
                                "2. Enable Floating Window Overlay to show the gaming toolbar over games.\n" +
                                "3. Record your touch gesture sequence in the wizard.\n" +
                                "4. Adjust speed, repetition, and opacity in the Macro Editor.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    statusText: String,
    isOK: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isOK) Color(0xFF1A261D) else Color(0xFF261D1A)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = if (isOK) Color(0xFF22C55E).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (isOK) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isOK) Color(0xFF22C55E) else Color(0xFFEF4444),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = if (isOK) Color(0xFF4ADE80) else Color(0xFFFCA5A5)
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF22C55E)
            )
        }
    }
}

@Composable
private fun RecentMacroCard(
    macro: MacroEntity,
    onPlay: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C202B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = macro.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${macro.actionCount} actions • ${macro.playbackSpeed}x • Reps: ${if (macro.isContinuous) "Continuous" else macro.repetitions}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF22C55E))
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}
